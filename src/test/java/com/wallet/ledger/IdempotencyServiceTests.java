package com.wallet.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.wallet.ledger.repository.IdempotencyKeyRepository;
import com.wallet.ledger.service.IdempotencyService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class IdempotencyServiceTests {

    private final IdempotencyService service = new IdempotencyService(mock(IdempotencyKeyRepository.class));

    @Test
    void hashesRequestBodyWithSha256() {
        assertThat(service.hash("wallet-transfer".getBytes(StandardCharsets.UTF_8)))
                .isEqualTo("876a4d8f3aea84711bc71731f672561e9db30a503843cf764a26400d1f9f432b");
    }
}
