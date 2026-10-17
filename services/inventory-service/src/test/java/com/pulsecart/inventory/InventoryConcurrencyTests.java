package com.pulsecart.inventory;

import com.pulsecart.inventory.dto.InventoryDtos.CreateInventoryRequest;
import com.pulsecart.inventory.dto.InventoryDtos.InventoryResponse;
import com.pulsecart.inventory.dto.InventoryDtos.ReservationItemDto;
import com.pulsecart.inventory.dto.InventoryDtos.ReserveStockRequest;
import com.pulsecart.inventory.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class InventoryConcurrencyTests {

    @Autowired
    private InventoryService inventoryService;

    @Test
    @DisplayName("Flash-Sale Concurrency Test: 100 competing threads must never oversell 10 inventory units")
    void testConcurrentFlashSaleReservationsNeverOversell() throws Exception {
        String productId = "prod-flash-deal-limited-10";
        int initialStock = 10;
        int totalThreads = 100;

        // 1. Initialize product inventory with exactly 10 units
        inventoryService.initializeInventory(new CreateInventoryRequest(
                productId, "SKU-FLASH-10", initialStock
        ));

        // 2. Prepare high-concurrency race
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(totalThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < totalThreads; i++) {
            final String orderId = "ord-race-" + i;
            executor.submit(() -> {
                try {
                    startGate.await(); // Synchronize all threads to hit the database simultaneously
                    inventoryService.reserveStock(new ReserveStockRequest(
                            orderId,
                            List.of(new ReservationItemDto(productId, 1)),
                            600
                    ));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Release the hounds!
        startGate.countDown();
        boolean completed = endGate.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();

        // Invariant Verifications:
        // Exactly 10 reservations must succeed, and exactly 90 must be cleanly rejected
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(90);

        // Authoritative inventory state check
        InventoryResponse finalInventory = inventoryService.getInventoryByProductId(productId);
        assertThat(finalInventory.availableQuantity()).isZero();
        assertThat(finalInventory.reservedQuantity()).isEqualTo(10);
        assertThat(finalInventory.totalQuantity()).isEqualTo(10);
    }
}
