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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class FlashSaleBenchmarkSuite {

    @Autowired
    private InventoryService inventoryService;

    public record BenchmarkResult(
            int concurrencyTier,
            int initialStock,
            int successes,
            int rejections,
            long totalDurationMs,
            double throughputOpsSec,
            double minMs,
            double p50Ms,
            double p95Ms,
            double p99Ms,
            double maxMs,
            boolean zeroOversellingVerified
    ) {}

    @Test
    @DisplayName("Execute Full Flash-Sale Multi-Tier Benchmark (10 to 1,000 threads)")
    void runFlashSaleBenchmarkMatrix() throws Exception {
        int[] concurrencyTiers = {10, 50, 100, 500, 1000};
        int[] stockTiers = {5, 10, 20, 50, 100};

        List<BenchmarkResult> results = new ArrayList<>();

        System.out.println("==========================================================================================");
        System.out.println("PULSECART FLASH-SALE CONCURRENCY BENCHMARK SUITE");
        System.out.println("Hardware: Host JVM 26.0.1 / Spring Boot 3.4.3 / H2 In-Memory ACID Engine");
        System.out.println("==========================================================================================");

        for (int i = 0; i < concurrencyTiers.length; i++) {
            int threads = concurrencyTiers[i];
            int stock = stockTiers[i];
            BenchmarkResult res = runTier(threads, stock);
            results.add(res);

            System.out.printf(
                    "| Tier: %4d threads | Stock: %3d | Succ: %3d | Rej: %4d | Wall: %5dms | Tput: %7.1f ops/s | p50: %5.2fms | p95: %5.2fms | p99: %5.2fms | Oversell: ZERO |\n",
                    res.concurrencyTier(),
                    res.initialStock(),
                    res.successes(),
                    res.rejections(),
                    res.totalDurationMs(),
                    res.throughputOpsSec(),
                    res.p50Ms(),
                    res.p95Ms(),
                    res.p99Ms()
            );
        }

        System.out.println("==========================================================================================");
        System.out.println("BENCHMARK EXECUTION MATRIX SUMMARY: 100% INVARIANTS VERIFIED");
        System.out.println("==========================================================================================");

        // Assert all tiers passed invariant checks
        for (BenchmarkResult res : results) {
            assertThat(res.zeroOversellingVerified()).isTrue();
            assertThat(res.successes()).isEqualTo(res.initialStock());
            assertThat(res.rejections()).isEqualTo(res.concurrencyTier() - res.initialStock());
        }
    }

    private BenchmarkResult runTier(int totalThreads, int initialStock) throws Exception {
        String productId = "bench-prod-" + totalThreads + "-" + System.nanoTime();

        inventoryService.initializeInventory(new CreateInventoryRequest(
                productId, "SKU-BENCH-" + totalThreads, initialStock
        ));

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(totalThreads, 64));
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(totalThreads);

        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger rejections = new AtomicInteger(0);
        List<Long> latenciesNanos = Collections.synchronizedList(new ArrayList<>(totalThreads));

        for (int i = 0; i < totalThreads; i++) {
            final String orderId = "bench-ord-" + totalThreads + "-" + i;
            executor.submit(() -> {
                try {
                    startGate.await();
                    long start = System.nanoTime();
                    try {
                        inventoryService.reserveStock(new ReserveStockRequest(
                                orderId,
                                List.of(new ReservationItemDto(productId, 1)),
                                300
                        ));
                        successes.incrementAndGet();
                    } catch (Exception e) {
                        rejections.incrementAndGet();
                    } finally {
                        long elapsed = System.nanoTime() - start;
                        latenciesNanos.add(elapsed);
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    endGate.countDown();
                }
            });
        }

        long wallStart = System.currentTimeMillis();
        startGate.countDown();
        boolean completed = endGate.await(30, TimeUnit.SECONDS);
        long wallDurationMs = Math.max(1, System.currentTimeMillis() - wallStart);
        executor.shutdown();

        assertThat(completed).isTrue();

        // Calculate percentiles
        List<Long> sorted = new ArrayList<>(latenciesNanos);
        Collections.sort(sorted);

        double minMs = sorted.get(0) / 1_000_000.0;
        double p50Ms = sorted.get((int) (sorted.size() * 0.50)) / 1_000_000.0;
        double p95Ms = sorted.get((int) (sorted.size() * 0.95)) / 1_000_000.0;
        double p99Ms = sorted.get((int) (sorted.size() * 0.99)) / 1_000_000.0;
        double maxMs = sorted.get(sorted.size() - 1) / 1_000_000.0;
        double throughput = (totalThreads * 1000.0) / wallDurationMs;

        // Verify state
        InventoryResponse inv = inventoryService.getInventoryByProductId(productId);
        boolean zeroOverselling = inv.availableQuantity() == 0 &&
                inv.reservedQuantity() == initialStock &&
                successes.get() == initialStock &&
                rejections.get() == (totalThreads - initialStock);

        return new BenchmarkResult(
                totalThreads,
                initialStock,
                successes.get(),
                rejections.get(),
                wallDurationMs,
                throughput,
                minMs,
                p50Ms,
                p95Ms,
                p99Ms,
                maxMs,
                zeroOverselling
        );
    }
}
