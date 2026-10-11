package com.wallet.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.wallet.ledger.api.TransferRequest;
import com.wallet.ledger.domain.AccountType;
import com.wallet.ledger.domain.User;
import com.wallet.ledger.domain.Wallet;
import com.wallet.ledger.repository.UserRepository;
import com.wallet.ledger.repository.WalletRepository;
import com.wallet.ledger.service.TransferService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class TransferConcurrencyIntegrationTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void dataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransferService transferService;

    @Test
    void stampedeNeverOverdrawsWalletAcrossRepeatedRuns() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID destinationUserId = UUID.randomUUID();
        userRepository.save(new User(userId, userId + "@example.test"));
        userRepository.save(new User(destinationUserId, destinationUserId + "@example.test"));
        Wallet source = walletRepository.save(new Wallet(userId, AccountType.USER, "INR", 500));
        Wallet destination = walletRepository.save(new Wallet(destinationUserId, AccountType.USER, "INR", 0));
        ExecutorService executor = Executors.newFixedThreadPool(20);
        try {
            for (int run = 0; run < 20; run++) {
                if (run > 0) {
                    walletRepository.adjustBalance(source.getAccountId(), 500);
                }
                List<Future<Boolean>> results = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    results.add(executor.submit(() -> {
                        try {
                            transferService.transfer(userId, new TransferRequest(
                                    source.getAccountId(), destination.getAccountId(), 10, "INR", null));
                            return true;
                        } catch (RuntimeException exception) {
                            return false;
                        }
                    }));
                }
                long successful = 0;
                for (Future<Boolean> result : results) {
                    if (result.get()) {
                        successful++;
                    }
                }
                walletRepository.flush();
                assertThat(successful).isEqualTo(50);
                assertThat(walletRepository.findById(source.getAccountId()).orElseThrow().getBalance()).isEqualTo(0);
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
