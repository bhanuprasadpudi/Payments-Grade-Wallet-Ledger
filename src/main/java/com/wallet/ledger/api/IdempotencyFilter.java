package com.wallet.ledger.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.ledger.domain.IdempotencyKey;
import com.wallet.ledger.service.IdempotencyService;
import com.wallet.ledger.service.LedgerException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyService service;
    private final ObjectMapper objectMapper;

    public IdempotencyFilter(IdempotencyService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        byte[] body = request.getInputStream().readAllBytes();
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        try {
            UUID userId = parseRequired(request.getHeader("X-User-Id"), "USER_ID_REQUIRED");
            UUID key = parseRequired(request.getHeader("Idempotency-Key"), "IDEMPOTENCY_KEY_REQUIRED");
            String hash = service.hash(body);
            IdempotencyKey existing = service.reserve(userId, key, hash);
            if (!service.sameBody(existing, hash)) {
                writeError(response, 422, "IDEMPOTENCY_KEY_REUSED", "Key was used with a different request body");
                return;
            }
            if (existing.getResponseStatus() != null) {
                response.setStatus(existing.getResponseStatus());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(existing.getResponseBody());
                return;
            }
            filterChain.doFilter(new ReplayableRequest(request, body), cachedResponse);
            byte[] responseBody = cachedResponse.getContentAsByteArray();
            service.complete(existing, cachedResponse.getStatus(),
                    new String(responseBody, response.getCharacterEncoding()));
            cachedResponse.copyBodyToResponse();
        } catch (LedgerException exception) {
            writeError(response, 422, exception.getCode(), exception.getMessage());
        } finally {
            if (!response.isCommitted()) {
                cachedResponse.copyBodyToResponse();
            }
        }
    }

    private UUID parseRequired(String value, String code) {
        if (value == null) {
            throw new LedgerException(code, "Required request header is missing");
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new LedgerException(code, "Request header must be a UUID");
        }
    }

    private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                new ApiError(code, message, UUID.randomUUID().toString())));
    }

    private static final class ReplayableRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        private ReplayableRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public jakarta.servlet.ServletInputStream getInputStream() {
            return new jakarta.servlet.ServletInputStream() {
                private int index;
                public int read() { return index < body.length ? body[index++] & 0xff : -1; }
                public boolean isFinished() { return index >= body.length; }
                public boolean isReady() { return true; }
                public void setReadListener(jakarta.servlet.ReadListener listener) { }
            };
        }
    }
}
