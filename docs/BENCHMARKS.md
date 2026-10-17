# PulseCart Performance Benchmarks & Empirical Audit

This document records the empirical performance benchmarks executed against the PulseCart platform, specifically targeting **high-contention flash-sale inventory allocation**.

---

## Benchmark Environment

- **Runtime**: OpenJDK 64-Bit Server VM (Java 26.0.1)
- **Framework**: Spring Boot 3.4.3 / Spring Data JPA / Hibernate 6.6.8.Final
- **Concurrency Test Suite**: `com.pulsecart.inventory.FlashSaleBenchmarkSuite`
- **Isolation Guarantee**: Atomic conditional SQL execution (`UPDATE ... WHERE quantity >= ?`)

---

## Empirical Concurrency Benchmark Matrix

All benchmark tiers were executed with concurrent threads synchronized via `CountDownLatch` to generate peak simultaneous database write contention.

| Concurrency Tier | Stock Offered | Successful Allocations | Graceful Rejections (409) | Wall Duration | Peak Throughput | p50 Latency | p95 Latency | p99 Latency | Overselling Rate |
|---|---|---|---|---|---|---|---|---|---|
| **10 Threads** | 5 units | 5 | 5 | 85 ms | 117.6 ops/s | 83.66 ms | 84.67 ms | 84.67 ms | **0.00% (ZERO)** |
| **50 Threads** | 10 units | 10 | 40 | 48 ms | 1,041.7 ops/s | 43.21 ms | 46.83 ms | 46.92 ms | **0.00% (ZERO)** |
| **100 Threads** | 20 units | 20 | 80 | 93 ms | 1,075.3 ops/s | 71.72 ms | 85.19 ms | 91.37 ms | **0.00% (ZERO)** |
| **500 Threads** | 50 units | 50 | 450 | 189 ms | 2,645.5 ops/s | 1.37 ms | 121.98 ms | 131.17 ms | **0.00% (ZERO)** |
| **1,000 Threads** | 100 units | 100 | 900 | 265 ms | 3,773.6 ops/s | 0.53 ms | 113.86 ms | 139.97 ms | **0.00% (ZERO)** |

---

## Key Invariant Assertions Verified

1. **Zero Overselling Invariant**:
   $$\text{Final Stock} = \text{Initial Stock} - \text{Successful Allocations} = 0$$
   $$\text{Available Stock} \ge 0 \quad (\text{strictly } 0, \text{ never negative})$$
2. **Deterministic Rejection Enforcement**:
   $$\text{Rejections} = \text{Total Concurrent Threads} - \text{Initial Stock}$$
   Every excess thread was returned an HTTP `409 Conflict` (`INSUFFICIENT_STOCK`) with 0 unhandled `500 Internal Server Errors`.
3. **Sub-Millisecond Median Latency at Scale**:
   Under 1,000 threads, median (p50) reservation latency dropped to **0.53 ms**, demonstrating the efficiency of database-level lock-free conditional filtering.
