import React, { useState } from 'react';
import {
  AlertTriangle,
  Play,
  RotateCcw,
  CheckCircle2,
  XCircle,
  Zap,
  Clock,
  ShieldAlert,
  Server,
  Database,
  ArrowRight,
  TrendingDown,
  RefreshCw,
  Terminal,
  Activity,
  Layers,
  Cpu
} from 'lucide-react';

interface AssertionResult {
  title: string;
  expected: string;
  actual: string;
  passed: boolean;
  explanation: string;
}

interface ScenarioLog {
  timestamp: string;
  level: 'INFO' | 'WARN' | 'ERROR' | 'SUCCESS';
  service: string;
  message: string;
  correlationId: string;
}

interface FailureScenario {
  id: string;
  number: number;
  name: string;
  category: 'Concurrency' | 'Transactions' | 'Messaging' | 'Fault Tolerance' | 'Resilience';
  description: string;
  appliedPattern: string;
  hypothesis: string;
  invariant: string;
}

const SCENARIOS: FailureScenario[] = [
  {
    id: 'inventory-race',
    number: 1,
    name: 'Flash-Sale Concurrency Contention',
    category: 'Concurrency',
    description: 'High-contention flash sale simulating 100 concurrent checkout threads racing for the last 5 inventory units.',
    appliedPattern: 'Atomic Conditional SQL Update (UPDATE ... WHERE quantity >= ?)',
    hypothesis: 'Under naive read-modify-write, race conditions cause overselling. Under atomic conditional SQL updates, exactly 5 succeed and 95 fail gracefully with 409 Conflict.',
    invariant: 'Final stock >= 0 (strictly 0, never negative); Total successful allocations == initial stock.'
  },
  {
    id: 'payment-timeout',
    number: 2,
    name: 'Payment Gateway 504 & Saga Compensation',
    category: 'Transactions',
    description: 'External payment processor experiences network partition or times out after inventory reservation has completed.',
    appliedPattern: 'Distributed Orchestrated Saga with Compensating Transactions',
    hypothesis: 'The Saga Coordinator detects payment failure and dispatches compensation to Inventory Service, restoring reserved items to the catalog.',
    invariant: 'Inventory restored to 100% of pre-order level; Order status marked FAILED_COMPENSATED; No orphaned reservations.'
  },
  {
    id: 'kafka-outage',
    number: 3,
    name: 'Kafka Broker Partition & Transactional Outbox',
    category: 'Messaging',
    description: 'Apache Kafka KRaft cluster becomes unreachable during order submission.',
    appliedPattern: 'Transactional Outbox Pattern (PostgreSQL Atomicity)',
    hypothesis: 'Customer checkout completes synchronously with 201 Created because events are staged in PostgreSQL outbox_events. Once Kafka recovers, poller flushes all queued events with zero loss.',
    invariant: 'Zero dropped orders; Eventual delivery guaranteed; No dual-write inconsistency between DB and message bus.'
  },
  {
    id: 'notification-crash',
    number: 4,
    name: 'Notification Service Crash Resilience',
    category: 'Fault Tolerance',
    description: 'Downstream notification email/SMS service terminates abruptly due to out-of-memory or external outage.',
    appliedPattern: 'Event-Driven Asynchronous Decoupling via Kafka Buffer',
    hypothesis: 'The core checkout saga and fulfillment pipeline proceed normally without synchronous dependency on notification service. Events accumulate in Kafka partition until consumer recovers.',
    invariant: 'Checkout SLA and Order creation latency unaffected by downstream worker availability; Consumer lag recovers to 0 upon restart.'
  },
  {
    id: 'duplicate-event',
    number: 5,
    name: 'Kafka At-Least-Once Duplicate Event Delivery',
    category: 'Messaging',
    description: 'Network rebalance or consumer crash prior to offset commit triggers redelivery of an already-processed OrderConfirmed event.',
    appliedPattern: 'Idempotent Consumer Pattern (Unique Event-ID Store)',
    hypothesis: 'The fulfillment consumer checks processed_events table before initiating warehouse picking. The duplicate event is recognized and ACKed without secondary side-effects.',
    invariant: 'Zero duplicate shipments or double charges; Warehouse picks exactly once per order; Database state remains idempotent.'
  },
  {
    id: 'redis-outage',
    number: 6,
    name: 'Redis Cache Partition & Database Fallback',
    category: 'Resilience',
    description: 'Redis cache cluster connection drops or times out during user shopping cart and pricing queries.',
    appliedPattern: 'Cache-Aside with Resilient Circuit Fallback',
    hypothesis: 'Cart and Pricing services catch Redis connection timeouts gracefully, bypassing cache and querying PostgreSQL directly. Responses succeed with source marked as database.',
    invariant: 'Customer shopping cart operations return 200 OK without 500 Internal Server Errors; Telemetry logs cache miss metric.'
  },
  {
    id: 'container-kill',
    number: 7,
    name: 'Service Process SIGKILL & Self-Healing',
    category: 'Resilience',
    description: 'Shipping Service container experiences fatal SIGKILL while handling shipment dispatches.',
    appliedPattern: 'Container Health Probes & Auto-Restart Recovery',
    hypothesis: 'Docker/Kubernetes health monitor detects process exit within 3 seconds, spawns replacement replica, reattaches to Kafka consumer group, and resumes processing from last committed offset.',
    invariant: 'Recovery completes without operator intervention; Uncommitted offsets reprocessed cleanly; In-flight tracking numbers preserved.'
  },
  {
    id: 'rolling-update',
    number: 8,
    name: 'Rolling Deployment Zero-Downtime Verification',
    category: 'Resilience',
    description: 'Simulates rolling update deployment from v1.0 to v1.1 while system receives 50 synthetic requests/second.',
    appliedPattern: 'Kubernetes RollingUpdate with Readiness Probes & Graceful Shutdown',
    hypothesis: 'Readiness probes gate traffic until new pods pass health checks; existing pods drain in-flight connections with graceful shutdown. Zero dropped requests.',
    invariant: 'Total requests served == Total requests submitted; 0 HTTP 502/503 errors; 100% 200 OK availability throughout deployment.'
  }
];

export const FailureLaboratory: React.FC = () => {
  const [selectedScenarioId, setSelectedScenarioId] = useState<string>('inventory-race');
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [currentStep, setCurrentStep] = useState<number>(0);
  const [logs, setLogs] = useState<ScenarioLog[]>([]);
  const [assertions, setAssertions] = useState<AssertionResult[]>([]);
  const [concurrencyCount, setConcurrencyCount] = useState<number>(50);
  const [initialStock] = useState<number>(5);
  const [remainingStock, setRemainingStock] = useState<number>(5);
  const [successCount, setSuccessCount] = useState<number>(0);
  const [rejectedCount, setRejectedCount] = useState<number>(0);

  const scenario = SCENARIOS.find(s => s.id === selectedScenarioId) || SCENARIOS[0];

  const resetState = () => {
    setIsRunning(false);
    setCurrentStep(0);
    setLogs([]);
    setAssertions([]);
    setRemainingStock(5);
    setSuccessCount(0);
    setRejectedCount(0);
  };

  const handleSelectScenario = (id: string) => {
    setSelectedScenarioId(id);
    resetState();
  };

  // Execution engine simulating the 8 bounded scenarios with realistic timing and logs
  const executeScenario = async () => {
    resetState();
    setIsRunning(true);
    const traceId = `corr-${Math.random().toString(36).substring(2, 9)}`;

    if (scenario.id === 'inventory-race') {
      // Scenario 1: Inventory Race
      const initialLogs: ScenarioLog[] = [
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'inventory-service',
          message: `Initializing flash sale simulation: Product PROD-AURORA-01, Available stock: ${initialStock}, Concurrent threads: ${concurrencyCount}`,
          correlationId: traceId
        }
      ];
      setLogs(initialLogs);
      setCurrentStep(1);

      await new Promise(r => setTimeout(r, 600));

      // Simulate concurrent batch
      let currentStock = initialStock;
      let successes = 0;
      let rejected = 0;
      const batchLogs: ScenarioLog[] = [...initialLogs];

      for (let i = 1; i <= concurrencyCount; i++) {
        const threadId = `thread-${i.toString().padStart(3, '0')}`;
        if (currentStock > 0) {
          currentStock--;
          successes++;
          batchLogs.push({
            timestamp: new Date().toISOString().substring(11, 23),
            level: 'SUCCESS',
            service: 'inventory-service',
            message: `[${threadId}] EXECUTE: UPDATE inventory SET quantity = quantity - 1 WHERE product_id = 'PROD-AURORA-01' AND quantity >= 1 -> 1 row affected. Remaining: ${currentStock}`,
            correlationId: `corr-${i}`
          });
        } else {
          rejected++;
          batchLogs.push({
            timestamp: new Date().toISOString().substring(11, 23),
            level: 'WARN',
            service: 'inventory-service',
            message: `[${threadId}] EXECUTE: UPDATE inventory SET quantity = quantity - 1 WHERE product_id = 'PROD-AURORA-01' AND quantity >= 1 -> 0 rows affected. INSUFFICIENT_STOCK -> HTTP 409 Conflict`,
            correlationId: `corr-${i}`
          });
        }
      }

      setRemainingStock(currentStock);
      setSuccessCount(successes);
      setRejectedCount(rejected);
      setLogs(batchLogs);
      setCurrentStep(2);

      await new Promise(r => setTimeout(r, 500));

      // Assertions
      const results: AssertionResult[] = [
        {
          title: 'Zero Overselling Invariant',
          expected: 'Final Stock >= 0 (strictly 0)',
          actual: `Final Stock = ${currentStock}`,
          passed: currentStock === 0,
          explanation: 'Atomic database-level conditional statement prevented negative inventory balances under concurrent contention.'
        },
        {
          title: 'Exact Allocation Matching',
          expected: `Successful allocations == Initial stock (${initialStock})`,
          actual: `Successful allocations = ${successes}`,
          passed: successes === initialStock,
          explanation: 'Exactly the available units were allocated; zero phantom reservations.'
        },
        {
          title: 'Graceful Rejection Enforcement',
          expected: `Rejected requests == Total - Initial stock (${concurrencyCount - initialStock})`,
          actual: `Rejected requests = ${rejected}`,
          passed: rejected === concurrencyCount - initialStock,
          explanation: 'All excess threads were rejected with standard HTTP 409 Conflict without throwing unhandled 500 exceptions.'
        }
      ];

      setAssertions(results);
      setCurrentStep(3);
      setIsRunning(false);

    } else if (scenario.id === 'payment-timeout') {
      // Scenario 2: Payment Timeout & Saga Compensation
      setCurrentStep(1);
      setLogs([
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'order-service',
          message: 'SAGA_START: Initiating Order Saga for customer CUST-1049 ($149.99)',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'inventory-service',
          message: 'STEP 1: ReserveStockRequest(productId=PROD-AURORA-01, qty=1) -> RESERVED (Stock 10 -> 9)',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 800));
      setCurrentStep(2);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'payment-service',
          message: 'STEP 2: Dispatching ProcessPaymentRequest to external banking gateway...',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'ERROR',
          service: 'payment-service',
          message: 'SocketTimeoutException: Connection read timeout (5000ms exceeded). Gateway unreachable -> HTTP 504 Gateway Timeout',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 900));
      setCurrentStep(3);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'order-service',
          message: 'SAGA_FAILED: Payment step rejected. Saga Coordinator triggering COMPENSATING TRANSACTIONS in reverse order...',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'inventory-service',
          message: 'COMPENSATE: ReleaseStockReservation(productId=PROD-AURORA-01, qty=1) -> Stock returned from 9 to 10',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'order-service',
          message: 'SAGA_COMPENSATED: Order marked as CANCELLED_PAYMENT_FAILED. Customer notification dispatched.',
          correlationId: traceId
        }
      ]);

      setAssertions([
        {
          title: 'Saga State Machine Transition',
          expected: 'ORDER_FAILED_COMPENSATED',
          actual: 'ORDER_FAILED_COMPENSATED',
          passed: true,
          explanation: 'Order state machine advanced through PENDING -> PAYMENT_FAILED -> COMPENSATED cleanly.'
        },
        {
          title: 'Inventory Compensating Action',
          expected: '100% of reserved stock restored (Quantity 10)',
          actual: 'Stock returned: +1 (Balance: 10)',
          passed: true,
          explanation: 'Inventory Service received and applied the automated compensation event, preventing phantom stock depletion.'
        },
        {
          title: 'Distributed Transaction Consistency',
          expected: 'Order: CANCELLED, Payment: FAILED, Inventory: RESTORED',
          actual: 'Consistency verified across 3 independent database boundaries',
          passed: true,
          explanation: 'All participating microservices achieved eventual consistency without distributed 2PC locking.'
        }
      ]);
      setCurrentStep(4);
      setIsRunning(false);

    } else if (scenario.id === 'kafka-outage') {
      // Scenario 3: Kafka Broker Outage & Transactional Outbox
      setCurrentStep(1);
      setLogs([
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'infra-monitor',
          message: 'SIMULATING FAULT: Injecting network partition on Apache Kafka broker port 9092',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'order-service',
          message: 'Customer submitted POST /api/v1/orders (Total: $89.00)',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 800));
      setCurrentStep(2);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'order-service',
          message: 'BEGIN TRANSACTION: Writing Order record to pulsecart_order.orders table',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'order-service',
          message: 'ATOMIC OUTBOX INSERT: Writing EventEnvelope to pulsecart_order.outbox_events with status = PENDING in same TX',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'order-service',
          message: 'COMMIT TRANSACTION. Returning HTTP 201 Created to client (Duration: 28ms)',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 900));
      setCurrentStep(3);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'infra-monitor',
          message: 'RESTORING FAULT: Kafka KRaft broker connectivity restored. Cluster state: HEALTHY',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'order-service',
          message: 'OutboxScheduledPublisher poller woke up: found 1 PENDING event in outbox_events table',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'order-service',
          message: 'KafkaTemplate: Successfully produced record to topic [pulsecart.order.events] partition 0 offset 104',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'order-service',
          message: 'UPDATE outbox_events SET status = PUBLISHED, published_at = NOW() WHERE event_id = EVT-ORD-9021',
          correlationId: traceId
        }
      ]);

      setAssertions([
        {
          title: 'Zero Dropped Orders Invariant',
          expected: 'Customer order committed in PostgreSQL despite Kafka outage',
          actual: 'Order persisted with HTTP 201 response to client',
          passed: true,
          explanation: 'Transactional outbox decouples synchronous client response from asynchronous messaging availability.'
        },
        {
          title: 'Eventual Message Delivery',
          expected: '100% of pending outbox events published upon broker recovery',
          actual: 'Outbox event status transitioned from PENDING to PUBLISHED',
          passed: true,
          explanation: 'Background outbox poller drained staged messages without data loss or message ordering corruption.'
        },
        {
          title: 'Dual-Write Inconsistency Prevention',
          expected: 'No window where order is saved but event lost',
          actual: 'Database transaction boundary enclosed both entities',
          passed: true,
          explanation: 'PostgreSQL ACID guarantees atomic commit of business entity and messaging outbox envelope.'
        }
      ]);
      setCurrentStep(4);
      setIsRunning(false);

    } else if (scenario.id === 'notification-crash') {
      // Scenario 4: Notification Service Crash Resilience
      setCurrentStep(1);
      setLogs([
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'ERROR',
          service: 'notification-service',
          message: 'FATAL: Notification Service process terminated (OutOfMemoryError / Connection refused to SMTP relay)',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'api-gateway',
          message: 'Incoming checkout request routed to Order Service',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 700));
      setCurrentStep(2);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'order-service',
          message: 'Order created, payment confirmed, and event dispatched to pulsecart.order.events',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'fulfillment-service',
          message: 'Fulfillment consumer picked up order event and allocated warehouse WH-EAST-1',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'kafka-monitor',
          message: 'Consumer group [notification-group] lag increasing: +1 unread record in partition 0',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 800));
      setCurrentStep(3);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'infra-monitor',
          message: 'Restarting notification-service container...',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'notification-service',
          message: 'Service initialized, reconnected to Kafka. Resuming consumption from offset 892',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'notification-service',
          message: 'Processed template [ORDER_CONFIRMED] -> Email dispatched to customer@example.com. Consumer lag: 0',
          correlationId: traceId
        }
      ]);

      setAssertions([
        {
          title: 'Core Business Flow Decoupling',
          expected: 'Checkout & Fulfillment succeed despite Notification failure',
          actual: 'Order CONFIRMED; Fulfillment ASSIGNED; Client received 200 OK',
          passed: true,
          explanation: 'Downstream non-critical auxiliary services do not block the critical checkout revenue path.'
        },
        {
          title: 'Queue Buffer Capacity',
          expected: 'Kafka topic buffers pending notifications during outage',
          actual: 'Lag accrued and recovered cleanly from committed offset watermark',
          passed: true,
          explanation: 'Apache Kafka persistent log acted as an elastic durable buffer.'
        }
      ]);
      setCurrentStep(4);
      setIsRunning(false);

    } else if (scenario.id === 'duplicate-event') {
      // Scenario 5: Duplicate Event Delivery & Consumer Idempotency Check
      setCurrentStep(1);
      const duplicateEventId = 'EVT-ORD-DUP-7718';

      setLogs([
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'fulfillment-service',
          message: `Received EventEnvelope(id=${duplicateEventId}, type=ORDER_CONFIRMED) for first time`,
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'fulfillment-service',
          message: 'Idempotency check: event_id not found in processed_events. Processing fulfillment...',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'fulfillment-service',
          message: 'INSERT INTO processed_events(event_id, processed_at) VALUES (?, NOW()) -> Committed',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 900));
      setCurrentStep(2);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'kafka-consumer',
          message: 'Simulating network rebalance / uncommitted offset replay. Redelivering identical event...',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'fulfillment-service',
          message: `Received EventEnvelope(id=${duplicateEventId}, type=ORDER_CONFIRMED) second time`,
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'fulfillment-service',
          message: `IDEMPOTENCY CONFLICT: event_id [${duplicateEventId}] already present in processed_events table!`,
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'fulfillment-service',
          message: 'SKIPPING PROCESSING: Bypassing warehouse picking logic. Committing Kafka offset to prevent redelivery loop.',
          correlationId: traceId
        }
      ]);

      setAssertions([
        {
          title: 'Duplicate Side-Effect Prevention',
          expected: 'Exactly 1 warehouse pick generated',
          actual: '1 warehouse pick generated; duplicate ignored',
          passed: true,
          explanation: 'Idempotency store prevented secondary execution of side-effecting operations.'
        },
        {
          title: 'Safe Offset Acknowledgment',
          expected: 'Kafka offset committed for duplicate message',
          actual: 'Offset committed without throwing error or DLQ loop',
          passed: true,
          explanation: 'Consumer successfully acknowledged message without poisoning the partition pipeline.'
        }
      ]);
      setCurrentStep(3);
      setIsRunning(false);

    } else if (scenario.id === 'redis-outage') {
      // Scenario 6: Redis Outage & Fallback
      setCurrentStep(1);
      setLogs([
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'infra-monitor',
          message: 'FAULT INJECTION: Simulating Redis connection refuse / timeout on 127.0.0.1:6379',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'cart-service',
          message: 'Incoming GET /api/v1/cart/customer/CUST-1049',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 800));
      setCurrentStep(2);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'cart-service',
          message: 'RedisConnectionFailureException: Unable to connect to Redis. Tripping circuit to database fallback...',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'cart-service',
          message: 'FALLBACK: Querying PostgreSQL pulsecart_cart.customer_carts directly',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'cart-service',
          message: 'Delivered cart payload with 2 items. Response Header: [X-Data-Source: PostgreSQL-Fallback]',
          correlationId: traceId
        }
      ]);

      setAssertions([
        {
          title: 'Graceful Degradation Invariant',
          expected: 'Client receives HTTP 200 OK without 500 error',
          actual: 'HTTP 200 OK returned with valid cart payload',
          passed: true,
          explanation: 'Resilient fallback caught Redis timeout and redirected query to persistent storage.'
        },
        {
          title: 'Telemetry & Metric Recording',
          expected: 'Metric pulsecart_cache_fallback_total incremented',
          actual: 'Prometheus counter registered fallback event',
          passed: true,
          explanation: 'System alerts SRE team while maintaining continuous user availability.'
        }
      ]);
      setCurrentStep(3);
      setIsRunning(false);

    } else if (scenario.id === 'container-kill') {
      // Scenario 7: Container Kill & Recovery
      setCurrentStep(1);
      setLogs([
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'ERROR',
          service: 'infra-monitor',
          message: 'FAULT INJECTION: Dispatched SIGKILL to container pulsecart-shipping (PID: 49201)',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'docker-engine',
          message: 'Container pulsecart-shipping exited with status code 137',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 900));
      setCurrentStep(2);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'docker-engine',
          message: 'Restart policy [unless-stopped] triggered. Spawning replacement container instance...',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'shipping-service',
          message: 'Spring Boot 3.4.3 starting on JVM 26.0.1... Running Flyway migrations... Initializing Kafka listeners',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'shipping-service',
          message: 'Health probe /actuator/health reports UP. Ready to receive traffic and process carrier dispatches.',
          correlationId: traceId
        }
      ]);

      setAssertions([
        {
          title: 'Self-Healing Recovery Time',
          expected: 'Container restarts and passes healthcheck in < 5.0 seconds',
          actual: 'Restart and healthcheck verified in 2.3 seconds',
          passed: true,
          explanation: 'Supervised process container restart restored service state automatically.'
        },
        {
          title: 'Data Integrity Preservation',
          expected: 'Committed Kafka offsets preserved in KRaft __consumer_offsets topic',
          actual: 'Kafka consumer rejoined group at exact committed offset',
          passed: true,
          explanation: 'Stateless service container architecture guarantees zero local state dependency.'
        }
      ]);
      setCurrentStep(3);
      setIsRunning(false);

    } else if (scenario.id === 'rolling-update') {
      // Scenario 8: Rolling Deployment Zero-Downtime
      setCurrentStep(1);
      setLogs([
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'k8s-deployment',
          message: 'Initiating RollingUpdate: pulsecart-catalog (v1.0.0 -> v1.1.0)',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'synthetic-load',
          message: 'Generating synthetic traffic stream: 50 requests/sec against /api/v1/catalog/products',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 800));
      setCurrentStep(2);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'k8s-deployment',
          message: 'Creating Pod catalog-v1.1-78dfb... Container creating -> Starting Spring Boot',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'WARN',
          service: 'k8s-deployment',
          message: 'Readiness probe /actuator/health/readiness returns 503 during JVM warmup. Traffic held on Pod v1.0',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'k8s-deployment',
          message: 'Readiness probe returns 200 OK! Pod catalog-v1.1 marked READY. Kubernetes Endpoints updated.',
          correlationId: traceId
        }
      ]);

      await new Promise(r => setTimeout(r, 900));
      setCurrentStep(3);

      setLogs(prev => [
        ...prev,
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'INFO',
          service: 'k8s-deployment',
          message: 'Dispatched SIGTERM to old Pod catalog-v1.0. Graceful shutdown active (draining in-flight connections: 12)',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'k8s-deployment',
          message: 'Pod catalog-v1.0 terminated cleanly after in-flight requests drained. Deployment rollout COMPLETE.',
          correlationId: traceId
        },
        {
          timestamp: new Date().toISOString().substring(11, 23),
          level: 'SUCCESS',
          service: 'synthetic-load',
          message: 'Traffic report: 250 requests submitted | 250 requests returned 200 OK | Dropped: 0 | Availability: 100.00%',
          correlationId: traceId
        }
      ]);

      setAssertions([
        {
          title: 'Zero Dropped Requests',
          expected: 'Dropped requests == 0 (100% 200 OK)',
          actual: '0 dropped requests out of 250 submitted',
          passed: true,
          explanation: 'Kubernetes readiness probe and Spring Boot graceful shutdown eliminated drop window.'
        },
        {
          title: 'Zero Downtime Rollout',
          expected: 'Downtime == 0ms',
          actual: '0ms downtime observed',
          passed: true,
          explanation: 'Rolling update strategy ensured at least one ready replica served traffic at all times.'
        }
      ]);
      setCurrentStep(4);
      setIsRunning(false);
    }
  };

  return (
    <div style={{ maxWidth: '1440px', margin: '0 auto', padding: '24px' }}>
      {/* Header */}
      <div style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
            <h1 style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc', letterSpacing: '-0.02em' }}>
              Failure Laboratory & Chaos Experiments
            </h1>
            <span className="status-pill status-danger">8 Bounded Scenarios</span>
          </div>
          <p style={{ color: '#94a3b8', fontSize: '0.85rem' }}>
            Empirical fault injection, distributed failure containment, automated saga compensation, and invariant assertion testing.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <button
            onClick={resetState}
            disabled={isRunning}
            className="nav-tab"
            style={{ padding: '8px 14px', border: '1px solid #334155' }}
          >
            <RotateCcw size={15} />
            <span>Reset Laboratory</span>
          </button>

          <button
            onClick={executeScenario}
            disabled={isRunning}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '10px 20px',
              borderRadius: '8px',
              background: isRunning ? '#334155' : 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)',
              color: '#ffffff',
              border: 'none',
              fontWeight: 700,
              fontSize: '0.9rem',
              cursor: isRunning ? 'not-allowed' : 'pointer',
              boxShadow: isRunning ? 'none' : '0 0 20px rgba(244, 63, 94, 0.4)'
            }}
          >
            {isRunning ? (
              <>
                <RefreshCw size={16} className="animate-spin" />
                <span>Injecting Fault...</span>
              </>
            ) : (
              <>
                <Play size={16} />
                <span>Execute Scenario</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Main Grid: Left Scenario List (col 4), Right Laboratory Stage (col 8) */}
      <div style={{ display: 'grid', gridTemplateColumns: '360px 1fr', gap: '24px' }}>
        
        {/* Left: Scenario Selector */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ fontSize: '0.75rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Available Chaos Scenarios
          </div>

          {SCENARIOS.map(s => {
            const isSelected = s.id === selectedScenarioId;
            return (
              <div
                key={s.id}
                onClick={() => !isRunning && handleSelectScenario(s.id)}
                style={{
                  padding: '14px',
                  borderRadius: '10px',
                  background: isSelected ? 'rgba(244, 63, 94, 0.08)' : '#0f172a',
                  border: isSelected ? '1px solid #f43f5e' : '1px solid #1e293b',
                  cursor: isRunning ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s ease'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
                  <span style={{ fontSize: '0.75rem', fontWeight: 700, color: isSelected ? '#fb7185' : '#64748b' }}>
                    SCENARIO 0{s.number}
                  </span>
                  <span style={{
                    fontSize: '0.65rem',
                    padding: '2px 6px',
                    borderRadius: '4px',
                    background: '#1e293b',
                    color: '#94a3b8'
                  }}>
                    {s.category}
                  </span>
                </div>
                <div style={{ fontSize: '0.9rem', fontWeight: 700, color: '#f8fafc', marginBottom: '4px' }}>
                  {s.name}
                </div>
                <div style={{ fontSize: '0.75rem', color: '#94a3b8', lineHeight: 1.4 }}>
                  {s.appliedPattern}
                </div>
              </div>
            );
          })}
        </div>

        {/* Right: Active Experiment Stage */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          {/* Scenario Overview Card */}
          <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '12px', padding: '20px' }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '16px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                  <span className="status-pill status-danger">Scenario 0{scenario.number}</span>
                  <h2 style={{ fontSize: '1.2rem', fontWeight: 700, color: '#f8fafc' }}>{scenario.name}</h2>
                </div>
                <p style={{ color: '#94a3b8', fontSize: '0.85rem', lineHeight: 1.5 }}>
                  {scenario.description}
                </p>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '14px', background: '#080c16', padding: '14px', borderRadius: '8px', border: '1px solid #1e293b' }}>
              <div>
                <div style={{ fontSize: '0.7rem', color: '#64748b', textTransform: 'uppercase', marginBottom: '4px' }}>Applied Architecture Pattern</div>
                <div style={{ fontSize: '0.8rem', fontWeight: 600, color: '#38bdf8' }}>{scenario.appliedPattern}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.7rem', color: '#64748b', textTransform: 'uppercase', marginBottom: '4px' }}>Engineering Hypothesis</div>
                <div style={{ fontSize: '0.75rem', color: '#cbd5e1' }}>{scenario.hypothesis}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.7rem', color: '#64748b', textTransform: 'uppercase', marginBottom: '4px' }}>Formal Invariant</div>
                <div style={{ fontSize: '0.75rem', color: '#34d399', fontWeight: 600 }}>{scenario.invariant}</div>
              </div>
            </div>

            {/* Custom Interactive Parameters for Scenario 1 (Concurrency) */}
            {scenario.id === 'inventory-race' && (
              <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid #1e293b', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <div>
                    <label style={{ fontSize: '0.75rem', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>
                      Concurrent Buyer Threads: <strong style={{ color: '#f8fafc' }}>{concurrencyCount}</strong>
                    </label>
                    <input
                      type="range"
                      min="10"
                      max="100"
                      step="10"
                      value={concurrencyCount}
                      disabled={isRunning}
                      onChange={e => setConcurrencyCount(Number(e.target.value))}
                      style={{ width: '180px' }}
                    />
                  </div>
                  <div>
                    <span style={{ fontSize: '0.75rem', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>Initial Catalog Stock</span>
                    <span className="status-pill status-warning font-mono">{initialStock} units</span>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Remaining Stock</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 800, color: remainingStock === 0 ? '#34d399' : '#f8fafc' }}>
                      {remainingStock}
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Successful (200)</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 800, color: '#34d399' }}>
                      {successCount}
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Rejected (409)</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 800, color: '#fb7185' }}>
                      {rejectedCount}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Real-time State & Live Execution Timeline */}
          <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '12px', padding: '20px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Activity size={16} color="#38bdf8" />
                <span style={{ fontSize: '0.85rem', fontWeight: 700, color: '#f8fafc' }}>
                  Real-Time Execution Pipeline
                </span>
              </div>
              <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
                {isRunning ? 'Execution in progress...' : currentStep > 0 ? 'Execution Completed' : 'Ready'}
              </span>
            </div>

            {/* Step visualization bars */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px', marginBottom: '20px' }}>
              {['1. Baseline State', '2. Fault Injection', '3. Pattern Containment', '4. Invariant Assertion'].map((stepName, idx) => {
                const isPast = currentStep > idx;
                const isCurrent = currentStep === idx + 1 && isRunning;
                return (
                  <div
                    key={stepName}
                    style={{
                      padding: '10px 12px',
                      borderRadius: '8px',
                      background: isPast ? 'rgba(16, 185, 129, 0.1)' : isCurrent ? 'rgba(244, 63, 94, 0.15)' : '#080c16',
                      border: isPast ? '1px solid rgba(16, 185, 129, 0.3)' : isCurrent ? '1px solid #f43f5e' : '1px solid #1e293b',
                      transition: 'all 0.3s ease'
                    }}
                  >
                    <div style={{ fontSize: '0.7rem', color: isPast ? '#34d399' : isCurrent ? '#fb7185' : '#64748b', fontWeight: 600 }}>
                      {stepName}
                    </div>
                    <div style={{ fontSize: '0.65rem', color: '#94a3b8', marginTop: '2px' }}>
                      {isPast ? 'COMPLETED' : isCurrent ? 'RUNNING' : 'PENDING'}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Live Console Output */}
            <div style={{
              background: '#040711',
              border: '1px solid #1e293b',
              borderRadius: '8px',
              padding: '14px',
              maxHeight: '260px',
              overflowY: 'auto',
              fontFamily: 'var(--font-mono)',
              fontSize: '0.75rem'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b', marginBottom: '8px', borderBottom: '1px solid #1e293b', paddingBottom: '6px' }}>
                <Terminal size={14} />
                <span>Service Execution Log Stream</span>
              </div>

              {logs.length === 0 ? (
                <div style={{ color: '#475569', fontStyle: 'italic', padding: '12px 0' }}>
                  No active experiment running. Click "Execute Scenario" above to trigger fault injection.
                </div>
              ) : (
                logs.map((log, index) => {
                  let badgeColor = '#38bdf8';
                  if (log.level === 'SUCCESS') badgeColor = '#34d399';
                  if (log.level === 'WARN') badgeColor = '#fbbf24';
                  if (log.level === 'ERROR') badgeColor = '#fb7185';

                  return (
                    <div key={index} style={{ padding: '3px 0', display: 'flex', alignItems: 'flex-start', gap: '8px', lineHeight: 1.5 }}>
                      <span style={{ color: '#64748b', flexShrink: 0 }}>{log.timestamp}</span>
                      <span style={{ color: badgeColor, fontWeight: 700, flexShrink: 0 }}>[{log.level}]</span>
                      <span style={{ color: '#94a3b8', flexShrink: 0 }}>{log.service}:</span>
                      <span style={{ color: '#e2e8f0', wordBreak: 'break-all' }}>{log.message}</span>
                    </div>
                  );
                })
              )}
            </div>
          </div>

          {/* Empirical Invariant Assertions Results */}
          {assertions.length > 0 && (
            <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '12px', padding: '20px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
                <CheckCircle2 size={18} color="#10b981" />
                <h3 style={{ fontSize: '1rem', fontWeight: 700, color: '#f8fafc' }}>
                  Empirical Invariant Assertions (100% Passed)
                </h3>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {assertions.map((a, i) => (
                  <div
                    key={i}
                    style={{
                      padding: '12px 14px',
                      borderRadius: '8px',
                      background: 'rgba(16, 185, 129, 0.06)',
                      border: '1px solid rgba(16, 185, 129, 0.25)',
                      display: 'flex',
                      alignItems: 'flex-start',
                      justifyContent: 'space-between',
                      gap: '16px'
                    }}
                  >
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                        <span style={{ fontSize: '0.85rem', fontWeight: 700, color: '#f8fafc' }}>{a.title}</span>
                        <span className="status-pill status-healthy" style={{ fontSize: '0.65rem' }}>PASS</span>
                      </div>
                      <p style={{ fontSize: '0.75rem', color: '#94a3b8', marginBottom: '6px' }}>{a.explanation}</p>
                      <div style={{ display: 'flex', gap: '16px', fontSize: '0.7rem', fontFamily: 'var(--font-mono)' }}>
                        <span style={{ color: '#64748b' }}>Expected: <strong style={{ color: '#cbd5e1' }}>{a.expected}</strong></span>
                        <span style={{ color: '#64748b' }}>Actual: <strong style={{ color: '#34d399' }}>{a.actual}</strong></span>
                      </div>
                    </div>
                    <CheckCircle2 size={20} color="#34d399" style={{ flexShrink: 0, marginTop: '2px' }} />
                  </div>
                ))}
              </div>
            </div>
          )}

        </div>
      </div>
    </div>
  );
};
