import { ServiceNode, ArchitecturePattern, KafkaTopicMetadata, DatabaseMetadata } from '../types';

export const SERVICES_REGISTRY: ServiceNode[] = [
  {
    id: 'api-gateway',
    name: 'API Gateway',
    port: 8080,
    tier: 'gateway',
    protocol: 'REST',
    database: 'Stateless (In-Memory Routing)',
    status: 'HEALTHY',
    patterns: ['API Gateway Pattern', 'Correlation ID WebFilter', 'Reactive Non-Blocking WebFlux', 'Circuit Breaker Fallback'],
    publishedEvents: [],
    consumedEvents: [],
    endpoints: ['/api/v1/**', '/fallback/service-unavailable', '/actuator/health'],
    schemaOverview: 'No persistent database; coordinates distributed trace propagation and path-based routing.'
  },
  {
    id: 'customer-service',
    name: 'Customer Service',
    port: 8081,
    tier: 'core',
    protocol: 'REST',
    database: 'PostgreSQL: pulsecart_customer',
    status: 'HEALTHY',
    patterns: ['Database-per-Service', 'Transactional Data Boundary'],
    publishedEvents: ['pulsecart.customer.events'],
    consumedEvents: [],
    endpoints: ['/api/v1/customers', '/api/v1/customers/{id}', '/api/v1/customers/{id}/tier'],
    schemaOverview: 'customers(id, email, first_name, last_name, tier, address, created_at)'
  },
  {
    id: 'catalog-service',
    name: 'Catalog Service',
    port: 8082,
    tier: 'core',
    protocol: 'Both',
    database: 'PostgreSQL: pulsecart_catalog',
    status: 'HEALTHY',
    patterns: ['Database-per-Service', 'Transactional Outbox Pattern'],
    publishedEvents: ['pulsecart.catalog.events (ProductCreated, ProductUpdated)'],
    consumedEvents: [],
    endpoints: ['/api/v1/products', '/api/v1/categories'],
    schemaOverview: 'products(id, sku, name, description, category_id, base_price, outbox_events)'
  },
  {
    id: 'pricing-service',
    name: 'Pricing Service',
    port: 8083,
    tier: 'core',
    protocol: 'REST',
    database: 'PostgreSQL: pulsecart_pricing',
    status: 'HEALTHY',
    patterns: ['Strategy Pattern (Standard, VIP, Volume, Flash-Sale)', "Banker's Rounding (HALF_EVEN)"],
    publishedEvents: ['pulsecart.pricing.events (PriceChanged)'],
    consumedEvents: [],
    endpoints: ['/api/v1/pricing/calculate', '/api/v1/pricing/products/{id}'],
    schemaOverview: 'pricing_rules(id, product_id, strategy_type, discount_pct, min_quantity, active)'
  },
  {
    id: 'inventory-service',
    name: 'Inventory Service',
    port: 8084,
    tier: 'core',
    protocol: 'Both',
    database: 'PostgreSQL: pulsecart_inventory',
    status: 'HEALTHY',
    patterns: ['Atomic Conditional SQL Updates', 'Pessimistic/Optimistic Concurrency Control', 'Zero Overselling Invariant'],
    publishedEvents: ['pulsecart.inventory.events (StockReserved, StockReleased, StockDepleted)'],
    consumedEvents: ['pulsecart.returns.events (RestockInitiated)'],
    endpoints: ['/api/v1/inventory/reserve', '/api/v1/inventory/release', '/api/v1/inventory/commit'],
    schemaOverview: 'inventory_items(product_id, available_quantity, reserved_quantity, warehouse_id)'
  },
  {
    id: 'cart-service',
    name: 'Cart Service',
    port: 8094,
    tier: 'core',
    protocol: 'REST',
    database: 'Redis 7: Session Hash & TTL (pulsecart:cart:*)',
    status: 'HEALTHY',
    patterns: ['Cache-as-Datastore', 'TTL Expiration', 'Polymorphic JSON Serialization'],
    publishedEvents: [],
    consumedEvents: [],
    endpoints: ['/api/v1/cart/{customerId}', '/api/v1/cart/{customerId}/items', '/api/v1/cart/{customerId}/clear'],
    schemaOverview: 'Redis key: pulsecart:cart:{customerId} -> TTL 7 days, JSON string array'
  },
  {
    id: 'promotion-service',
    name: 'Promotion Service',
    port: 8085,
    tier: 'checkout',
    protocol: 'REST',
    database: 'PostgreSQL: pulsecart_promotion',
    status: 'HEALTHY',
    patterns: ['Specification Pattern (Rule Engine)', 'Atomic Coupon Usage Counters'],
    publishedEvents: ['pulsecart.promotion.events'],
    consumedEvents: [],
    endpoints: ['/api/v1/promotions/validate', '/api/v1/promotions/redeem'],
    schemaOverview: 'coupons(code, discount_pct, current_redemptions, max_redemptions, valid_until)'
  },
  {
    id: 'payment-service',
    name: 'Payment Service',
    port: 8086,
    tier: 'checkout',
    protocol: 'REST',
    database: 'PostgreSQL: pulsecart_payment',
    status: 'HEALTHY',
    patterns: ['Provider Adapter Pattern', 'Factory Pattern', 'Idempotent Payment Processing', 'Chaos Injection Simulator'],
    publishedEvents: ['pulsecart.payment.events'],
    consumedEvents: [],
    endpoints: ['/api/v1/payments/authorize', '/api/v1/payments/capture', '/api/v1/payments/refund'],
    schemaOverview: 'payment_transactions(id, order_id, provider, amount, status, idempotency_key)'
  },
  {
    id: 'order-service',
    name: 'Order Service (Saga Coordinator)',
    port: 8087,
    tier: 'checkout',
    protocol: 'Both',
    database: 'PostgreSQL: pulsecart_order',
    status: 'HEALTHY',
    patterns: ['Orchestrated Distributed Saga', 'Compensating Transactions', 'Transactional Outbox Pattern'],
    publishedEvents: ['pulsecart.order.events (OrderCreated, OrderConfirmed, OrderCancelled)'],
    consumedEvents: [],
    endpoints: ['/api/v1/orders/checkout', '/api/v1/orders/{orderId}'],
    schemaOverview: 'orders(id, customer_id, total, status, shipping_address), outbox_events(id, payload, published)'
  },
  {
    id: 'fulfillment-service',
    name: 'Fulfillment Service',
    port: 8088,
    tier: 'async',
    protocol: 'Both',
    database: 'PostgreSQL: pulsecart_fulfillment',
    status: 'HEALTHY',
    patterns: ['Idempotent Consumer', 'Transactional Outbox', 'State Machine (PICKING -> PACKED)'],
    publishedEvents: ['pulsecart.fulfillment.events (FulfillmentCompleted)'],
    consumedEvents: ['pulsecart.order.events (OrderConfirmed)'],
    endpoints: ['/api/v1/fulfillments/order/{orderId}', '/api/v1/fulfillments/{id}/pack'],
    schemaOverview: 'fulfillments(id, order_id, warehouse_id, status), fulfillment_items'
  },
  {
    id: 'shipping-service',
    name: 'Shipping Service',
    port: 8089,
    tier: 'async',
    protocol: 'Both',
    database: 'PostgreSQL: pulsecart_shipping',
    status: 'HEALTHY',
    patterns: ['Carrier Adapter Pattern (FedEx, UPS, DHL)', 'Factory Pattern', 'Tracking Number Generator'],
    publishedEvents: ['pulsecart.shipping.events (ShippingDispatched)'],
    consumedEvents: ['pulsecart.fulfillment.events (FulfillmentCompleted)'],
    endpoints: ['/api/v1/shipments/dispatch', '/api/v1/shipments/tracking/{trackingNumber}'],
    schemaOverview: 'shipments(id, order_id, carrier, tracking_number, status, estimated_delivery)'
  },
  {
    id: 'notification-service',
    name: 'Notification Service',
    port: 8090,
    tier: 'async',
    protocol: 'Both',
    database: 'PostgreSQL: pulsecart_notification',
    status: 'HEALTHY',
    patterns: ['Dead Letter Queue (DLQ)', 'Poison Pill Isolation & Replay', 'Template Replacement Engine'],
    publishedEvents: [],
    consumedEvents: ['pulsecart.order.events', 'pulsecart.shipping.events', 'pulsecart.notification.events.dlq'],
    endpoints: ['/api/v1/notifications', '/api/v1/notifications/dlq', '/api/v1/notifications/dlq/{id}/replay'],
    schemaOverview: 'notifications(id, recipient, channel, content, status), dlq_messages(id, payload, exception, replayed)'
  },
  {
    id: 'returns-service',
    name: 'Returns Service',
    port: 8091,
    tier: 'async',
    protocol: 'Both',
    database: 'PostgreSQL: pulsecart_returns',
    status: 'HEALTHY',
    patterns: ['30-Day Return Window Validation', 'RMA Issuance State Machine', 'Transactional Outbox'],
    publishedEvents: ['pulsecart.returns.events (ReturnApproved, ReturnRejected, ReturnReceived)'],
    consumedEvents: [],
    endpoints: ['/api/v1/returns', '/api/v1/returns/{rmaId}', '/api/v1/returns/{rmaId}/receive'],
    schemaOverview: 'returns(id, order_id, status, refund_amount), return_items, returns_outbox_events'
  },
  {
    id: 'search-service',
    name: 'Search Service',
    port: 8092,
    tier: 'cqrs',
    protocol: 'Both',
    database: 'PostgreSQL: pulsecart_search',
    status: 'HEALTHY',
    patterns: ['CQRS Read Model Projection', 'Faceted Filtering', 'Projection Rebuild Catch-Up'],
    publishedEvents: [],
    consumedEvents: ['pulsecart.catalog.events', 'pulsecart.pricing.events', 'pulsecart.inventory.events'],
    endpoints: ['/api/v1/search', '/api/v1/search/index', '/api/v1/search/rebuild'],
    schemaOverview: 'product_search_index(product_id, sku, name, description, category_name, price, in_stock, tags)'
  },
  {
    id: 'analytics-service',
    name: 'Analytics Service',
    port: 8093,
    tier: 'cqrs',
    protocol: 'Both',
    database: 'PostgreSQL: pulsecart_analytics',
    status: 'HEALTHY',
    patterns: ['Real-Time Stream Aggregation', 'Hourly Revenue Rollup', 'Flash-Sale Velocity Scoring'],
    publishedEvents: [],
    consumedEvents: ['pulsecart.order.events'],
    endpoints: ['/api/v1/analytics/overview', '/api/v1/analytics/velocity', '/api/v1/analytics/hourly'],
    schemaOverview: 'hourly_sales_aggregates(hour_bucket, total_orders, total_revenue), product_sales_velocity'
  }
];

export const PATTERNS_REGISTRY: ArchitecturePattern[] = [
  {
    id: 'saga-orchestrator',
    name: 'Orchestrated Distributed Saga',
    category: 'Distributed Transactions',
    services: ['order-service', 'inventory-service', 'payment-service'],
    summary: 'Centralized state machine coordinating distributed multi-service checkout with deterministic compensating transactions.',
    problemSolved: 'In a microservices architecture with database-per-service boundaries, two-phase commit (2PC) is brittle, blocking, and unsupported by modern event brokers. Sagas coordinate atomic workflows asynchronously without distributed locks.',
    implementationDetails: 'The OrderSagaCoordinator advances orders from CREATED -> INVENTORY_RESERVED -> CONFIRMED. If payment authorization fails or times out, the coordinator automatically dispatches a compensating stock release transaction to revert inventory to the available pool.',
    codeSnippet: `@Transactional
public OrderCheckoutResponse executeCheckoutSaga(CheckoutRequest request) {
    Order order = createOrderRecord(request);
    boolean reserved = inventoryClient.reserveStockAtomic(order.getItems());
    if (!reserved) {
        order.setStatus(OrderStatus.CANCELLED);
        return failure("INVENTORY_EXHAUSTED");
    }
    PaymentResult payment = paymentClient.authorizePayment(order.getId(), order.getTotalAmount());
    if (!payment.isSuccess()) {
        inventoryClient.releaseStockAtomic(order.getItems()); // Compensating transaction
        order.setStatus(OrderStatus.CANCELLED);
        return failure("PAYMENT_DECLINED_COMPENSATED");
    }
    order.setStatus(OrderStatus.CONFIRMED);
    outboxPublisher.recordOutboxEvent("OrderConfirmed", order);
    return success(order);
}`
  },
  {
    id: 'transactional-outbox',
    name: 'Transactional Outbox Pattern',
    category: 'Event-Driven & Messaging',
    services: ['order-service', 'catalog-service', 'fulfillment-service', 'shipping-service', 'returns-service'],
    summary: 'Guarantees dual-write atomic consistency between local database state and Apache Kafka message emissions.',
    problemSolved: 'Emitting a message directly to Kafka inside a database transaction risks partial failures: the database might commit while the Kafka publish fails, or vice versa, creating inconsistent distributed state.',
    implementationDetails: 'Every state change and its corresponding domain event are written to the same local PostgreSQL database in a single atomic ACID transaction. A resilient background daemon continuously polls unpublished outbox records and publishes them to Kafka with at-least-once delivery semantics.',
    codeSnippet: `@Transactional
public Order saveOrderWithOutbox(Order order) {
    Order saved = orderRepository.save(order);
    OrderOutboxEvent event = new OrderOutboxEvent(
        UUID.randomUUID().toString(),
        "Order",
        saved.getId(),
        "OrderConfirmed",
        objectMapper.writeValueAsString(saved)
    );
    outboxRepository.save(event); // Same local ACID transaction!
    return saved;
}`
  },
  {
    id: 'atomic-conditional-inventory',
    name: 'Atomic Conditional Concurrency Control',
    category: 'Concurrency',
    services: ['inventory-service', 'promotion-service'],
    summary: 'Conditional database updates preventing race conditions and zero overselling under simultaneous 100+ thread flash-sale contention.',
    problemSolved: 'Reading stock into memory, checking if quantity > 0, and writing it back allows concurrent threads to interleavedly read identical stock and oversell limited inventory.',
    implementationDetails: 'Inventory updates use atomic conditional SQL updates with WHERE available_quantity >= :qty, returning row counts. If rowCount == 1, reservation succeeds. If rowCount == 0, the thread fails cleanly without locking whole tables.',
    codeSnippet: `@Modifying
@Query("UPDATE InventoryItem i SET i.availableQuantity = i.availableQuantity - :qty, " +
       "i.reservedQuantity = i.reservedQuantity + :qty " +
       "WHERE i.productId = :productId AND i.availableQuantity >= :qty")
int reserveStockAtomic(@Param("productId") String productId, @Param("qty") int qty);`
  },
  {
    id: 'dead-letter-queue',
    name: 'Dead Letter Queue (DLQ) & Poison Pill Isolation',
    category: 'Resilience & Fault Tolerance',
    services: ['notification-service'],
    summary: 'Isolates corrupted, unparseable, or fatal payloads to dedicated DLQ topics and persistence tables without blocking the consumer partition.',
    problemSolved: 'In Kafka, an unhandled runtime exception causes consumer offset commits to halt, creating an infinite retry loop that jams partition processing for all subsequent healthy messages.',
    implementationDetails: 'NotificationEventListener captures malformed payloads and routes them to pulsecart.notification.events.dlq and the dlq_messages table with full stack traces. An administrative replay API allows operations engineers to re-inject messages after downstream bug fixes.',
    codeSnippet: `@KafkaListener(topics = PulseCartTopics.NOTIFICATION_DLQ)
public void onDlqMessage(ConsumerRecord<String, String> record) {
    log.warn("Direct DLQ message received on topic {}: {}", record.topic(), record.value());
    notificationService.recordDlqMessage(
        record.topic(),
        record.partition(),
        record.offset(),
        record.value(),
        "Direct DLQ ingestion"
    );
}`
  },
  {
    id: 'cqrs-search-projection',
    name: 'CQRS Read Model Projections',
    category: 'CQRS & Read Models',
    services: ['search-service', 'analytics-service'],
    summary: 'Decoupled read models optimized for faceted text search and analytics aggregations, synchronized asynchronously via Kafka events.',
    problemSolved: 'Complex relational joins across products, categories, pricing discounts, and inventory availability in OLTP databases create high latency and database CPU spikes during flash sales.',
    implementationDetails: 'The Search Service maintains an un-normalized, flat product_search_index table. It listens to Kafka events (catalog, pricing, inventory) to update projection columns in near real-time, enabling sub-millisecond faceted queries without querying write models.',
    codeSnippet: `@KafkaListener(topics = PulseCartTopics.PRICING_EVENTS)
public void onPricingEvent(ConsumerRecord<String, String> record) {
    JsonNode payload = extractPayload(record.value());
    String productId = payload.get("productId").asText();
    BigDecimal newPrice = new BigDecimal(payload.get("price").asText());
    searchService.updatePrice(productId, newPrice); // Update read model projection!
}`
  },
  {
    id: 'carrier-adapter-pattern',
    name: 'Carrier Adapter & Factory Pattern',
    category: 'Resilience & Fault Tolerance',
    services: ['shipping-service', 'payment-service'],
    summary: 'Standardized interfaces decoupling business domain logic from third-party APIs (FedEx, UPS, DHL, Stripe, PayPal).',
    problemSolved: 'Hardcoding external vendor SDKs directly into microservice core logic leads to tight coupling, vendor lock-in, and testing difficulties during CI/CD.',
    implementationDetails: 'CarrierAdapter interface defines dispatchShipment(CarrierDispatchCommand). The CarrierAdapterFactory dynamically instantiates FedExSimulatorAdapter, UPSSimulatorAdapter, or DHLSimulatorAdapter based on order shipment preferences.',
    codeSnippet: `public interface CarrierAdapter {
    ShippingCarrier getSupportedCarrier();
    CarrierDispatchResult dispatchShipment(CarrierDispatchCommand command);
}

@Component
public class CarrierAdapterFactory {
    private final Map<ShippingCarrier, CarrierAdapter> adapters;
    public CarrierAdapter getAdapter(ShippingCarrier carrier) {
        return Optional.ofNullable(adapters.get(carrier))
            .orElseThrow(() -> new IllegalArgumentException("Unsupported carrier: " + carrier));
    }
}`
  }
];

export const KAFKA_TOPICS_METADATA: KafkaTopicMetadata[] = [
  { name: 'pulsecart.customer.events', partitions: 3, consumerGroups: ['pulsecart-analytics-group'], isDlq: false, retentionHours: 168, description: 'Customer registrations, tier changes, address updates' },
  { name: 'pulsecart.catalog.events', partitions: 3, consumerGroups: ['pulsecart-search-group'], isDlq: false, retentionHours: 168, description: 'Product creations, updates, specifications modifications' },
  { name: 'pulsecart.pricing.events', partitions: 3, consumerGroups: ['pulsecart-search-group'], isDlq: false, retentionHours: 168, description: 'Dynamic price adjustments, flash-sale promotions' },
  { name: 'pulsecart.inventory.events', partitions: 3, consumerGroups: ['pulsecart-search-group'], isDlq: false, retentionHours: 168, description: 'Stock reserved, committed, released, depleted' },
  { name: 'pulsecart.order.events', partitions: 3, consumerGroups: ['pulsecart-fulfillment-group', 'pulsecart-notification-group', 'pulsecart-analytics-group'], isDlq: false, retentionHours: 168, description: 'Central order lifecycle: OrderCreated, OrderConfirmed, OrderCancelled' },
  { name: 'pulsecart.payment.events', partitions: 3, consumerGroups: ['pulsecart-analytics-group'], isDlq: false, retentionHours: 168, description: 'Payment authorized, captured, refunded' },
  { name: 'pulsecart.fulfillment.events', partitions: 3, consumerGroups: ['pulsecart-shipping-group'], isDlq: false, retentionHours: 168, description: 'Warehouse picking & packing completion' },
  { name: 'pulsecart.shipping.events', partitions: 3, consumerGroups: ['pulsecart-notification-group'], isDlq: false, retentionHours: 168, description: 'Carrier shipment dispatched with tracking number' },
  { name: 'pulsecart.returns.events', partitions: 3, consumerGroups: ['pulsecart-inventory-group', 'pulsecart-payment-group'], isDlq: false, retentionHours: 168, description: 'RMA return issued, goods received at warehouse' },
  { name: 'pulsecart.notification.events.dlq', partitions: 1, consumerGroups: ['pulsecart-notification-dlq-consumer'], isDlq: true, retentionHours: 720, description: 'Dead Letter Queue for malformed and poison-pill notification events' },
  { name: 'pulsecart.order.events.dlq', partitions: 1, consumerGroups: [], isDlq: true, retentionHours: 720, description: 'Dead Letter Queue for unhandled order saga events' }
];

export const DATABASES_METADATA: DatabaseMetadata[] = [
  { serviceId: 'customer-service', dbName: 'pulsecart_customer', schema: 'pulsecart_customer', engine: 'PostgreSQL 16', primaryTables: ['customers'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'catalog-service', dbName: 'pulsecart_catalog', schema: 'pulsecart_catalog', engine: 'PostgreSQL 16', primaryTables: ['products', 'categories', 'outbox_events'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'pricing-service', dbName: 'pulsecart_pricing', schema: 'pulsecart_pricing', engine: 'PostgreSQL 16', primaryTables: ['pricing_rules', 'price_history'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'inventory-service', dbName: 'pulsecart_inventory', schema: 'pulsecart_inventory', engine: 'PostgreSQL 16', primaryTables: ['inventory_items', 'inventory_transactions'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'cart-service', dbName: 'pulsecart-redis', schema: 'Redis DB 0', engine: 'Redis 7', primaryTables: ['pulsecart:cart:*'], isolationGuarantee: 'Key-Namespace Isolation' },
  { serviceId: 'promotion-service', dbName: 'pulsecart_promotion', schema: 'pulsecart_promotion', engine: 'PostgreSQL 16', primaryTables: ['coupons', 'coupon_redemptions'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'payment-service', dbName: 'pulsecart_payment', schema: 'pulsecart_payment', engine: 'PostgreSQL 16', primaryTables: ['payment_transactions', 'idempotency_keys'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'order-service', dbName: 'pulsecart_order', schema: 'pulsecart_order', engine: 'PostgreSQL 16', primaryTables: ['orders', 'order_items', 'outbox_events'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'fulfillment-service', dbName: 'pulsecart_fulfillment', schema: 'pulsecart_fulfillment', engine: 'PostgreSQL 16', primaryTables: ['fulfillments', 'fulfillment_items', 'fulfillment_outbox_events'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'shipping-service', dbName: 'pulsecart_shipping', schema: 'pulsecart_shipping', engine: 'PostgreSQL 16', primaryTables: ['shipments', 'shipping_outbox_events'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'notification-service', dbName: 'pulsecart_notification', schema: 'pulsecart_notification', engine: 'PostgreSQL 16', primaryTables: ['notifications', 'dlq_messages'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'returns-service', dbName: 'pulsecart_returns', schema: 'pulsecart_returns', engine: 'PostgreSQL 16', primaryTables: ['returns', 'return_items', 'returns_outbox_events'], isolationGuarantee: 'Database-per-Service' },
  { serviceId: 'search-service', dbName: 'pulsecart_search', schema: 'pulsecart_search', engine: 'PostgreSQL 16', primaryTables: ['product_search_index'], isolationGuarantee: 'Database-per-Service (CQRS Read Model)' },
  { serviceId: 'analytics-service', dbName: 'pulsecart_analytics', schema: 'pulsecart_analytics', engine: 'PostgreSQL 16', primaryTables: ['hourly_sales_aggregates', 'product_sales_velocity'], isolationGuarantee: 'Database-per-Service (Telemetry Read Model)' }
];
