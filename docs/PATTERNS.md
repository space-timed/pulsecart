# Distributed Systems Design Patterns in PulseCart

PulseCart serves as a reference implementation of **18 distributed systems design patterns**. Each pattern is implemented with production Java code and verifiable assertions.

---

## Pattern Catalog Matrix

| # | Pattern Name | Target Challenge | Primary Implementation Location |
|---|---|---|---|
| **1** | **Database per Service** | Prevents tight database coupling and shared schema collisions. | 15 logical PostgreSQL schemas (`pulsecart_*`) & Redis cache. |
| **2** | **Saga Pattern (Orchestration)** | Multi-service transaction coordination across independent DBs. | `OrderSagaCoordinator.java` in `services/order-service`. |
| **3** | **Compensating Transactions** | Automated rollback when a downstream saga step fails. | `releaseStock()` in `inventory-service` called on payment failure. |
| **4** | **Transactional Outbox** | Eliminates dual-write inconsistency between DB and message broker. | `outbox_events` table & `OutboxScheduledPublisher.java`. |
| **5** | **Idempotent Consumer** | Handles Kafka at-least-once message redelivery safely. | `processed_events` table in `fulfillment`, `shipping`, `notification`. |
| **6** | **Dead Letter Queue (DLQ)** | Isolates poison pill messages without halting stream processing. | `dlq_messages` table & `.dlq` topics with re-drive API. |
| **7** | **CQRS** | Segregates write-heavy relational models from search projections. | `search-service` projecting `catalog` events to searchable index. |
| **8** | **Event Sourcing Projections** | Real-time analytics rollups computed from event logs. | `analytics-service` aggregating hourly revenue & velocity scores. |
| **9** | **API Gateway Pattern** | Unified edge entry point, routing, correlation, and fallback. | `api-gateway` built on Spring Cloud Gateway WebFlux. |
| **10**| **Distributed Tracing & Correlation** | End-to-end trace propagation across HTTP and Kafka headers. | `libraries/observability` with MDC and W3C `traceparent`. |
| **11**| **Atomic Conditional Update** | Prevents flash-sale race conditions without distributed locks. | `UPDATE inventory SET ... WHERE quantity >= ?` in `inventory-service`. |
| **12**| **Strategy Pattern** | Pluggable pricing algorithms (Base, Loyalty, Flash Sale). | `PricingStrategyFactory.java` in `pricing-service`. |
| **13**| **Adapter Pattern** | Uniform carrier abstraction for diverse shipment APIs. | `CarrierAdapter` (`FedExSimulator`, `UPSSimulator`, `DHLSimulator`). |
| **14**| **Factory Pattern** | Extensible payment gateway selection. | `PaymentProviderFactory` (`StripeSimulator`, `AdyenSimulator`). |
| **15**| **Specification Pattern** | Composable promotion validation rules. | `PromotionSpecification` in `promotion-service`. |
| **16**| **Cache-Aside Pattern** | Sub-millisecond cart retrieval with TTL eviction. | `CartService.java` with Redis TTL and JSON serialization. |
| **17**| **Circuit Breaker / Fallback** | Prevents cascading failures when dependencies are unavailable. | Gateway reactive fallbacks and cache-to-DB fallback paths. |
| **18**| **Bulkhead Pattern** | Thread pool isolation to prevent single slow dependency starvation. | Bounded `ExecutorService` pools in Saga and Benchmark runners. |

---

## Detailed Pattern Analyses

### Pattern 1: Database per Service
Each microservice is bound strictly to its own dedicated PostgreSQL schema:
- `pulsecart_customer`
- `pulsecart_catalog`
- `pulsecart_pricing`
- `pulsecart_inventory`
- `pulsecart_cart`
- `pulsecart_payment`
- `pulsecart_order`
- `pulsecart_fulfillment`
- `pulsecart_shipping`
- `pulsecart_notification`
- `pulsecart_returns`
- `pulsecart_search`
- `pulsecart_analytics`
- `pulsecart_promotion`
No service has credentials or permissions to query another service's tables. All cross-boundary data transfer occurs through HTTP REST contracts or Apache Kafka events.

### Pattern 2 & 3: Orchestrated Saga & Compensating Transactions
Distributed transaction management uses the Orchestration approach:
1. `OrderService` acts as the Saga Coordinator.
2. It synchronously commands `InventoryService.reserveStock()`.
3. If reservation succeeds, it commands `PaymentService.processPayment()`.
4. If payment fails (e.g. insufficient funds, gateway timeout), the coordinator dispatches `InventoryService.releaseStock()` to roll back the reserved items.
5. The order transitions to `CANCELLED_PAYMENT_FAILED` and the customer is informed immediately.

### Pattern 4: Transactional Outbox Pattern
To prevent dual-write vulnerabilities where a database record is committed but the corresponding message publication fails:
```java
@Transactional
public Order createOrder(...) {
    Order order = orderRepository.save(newOrder);
    
    OutboxEvent outbox = new OutboxEvent(
        UUID.randomUUID().toString(),
        "ORDER_CREATED",
        objectMapper.writeValueAsString(new OrderCreatedPayload(order)),
        "PENDING"
    );
    outboxRepository.save(outbox);
    return order;
}
```
A scheduled background poller reads pending outbox entries, publishes them to Kafka with record confirmation, and marks the outbox status as `PUBLISHED`.

### Pattern 5: Idempotent Consumer Pattern
To guarantee safe execution under Kafka's at-least-once delivery semantics:
```java
@KafkaListener(topics = "pulsecart.order.events")
public void handleOrderConfirmed(EventEnvelope envelope) {
    if (processedEventRepository.existsById(envelope.eventId())) {
        log.warn("Duplicate event {} detected. Skipping execution.", envelope.eventId());
        return;
    }
    // Execute business logic...
    processedEventRepository.save(new ProcessedEvent(envelope.eventId(), Instant.now()));
}
```
