import React, { useState } from 'react';
import { TraceSpan } from '../types';
import { Activity, Search, Clock, Server, ArrowRight, CheckCircle2, AlertTriangle, Layers } from 'lucide-react';

export const RequestXRay: React.FC = () => {
  const [lookupId, setLookupId] = useState('ORD-DEMO-100');
  const [activeSpan, setActiveSpan] = useState<TraceSpan | null>(null);

  const sampleSpans: TraceSpan[] = [
    {
      id: 'span-01',
      serviceId: 'api-gateway',
      serviceName: 'API Gateway',
      operation: 'POST /api/v1/orders/checkout',
      durationMs: 4,
      status: 'SUCCESS',
      timestamp: '14:32:01.102',
      correlationId: 'corr-88f2a1b9-37c4',
      details: 'INCOMING HTTP POST: Correlation ID generated. WebFilter attached X-Correlation-ID to downstream Netty client.'
    },
    {
      id: 'span-02',
      serviceId: 'order-service',
      serviceName: 'Order Service',
      operation: 'OrderSagaCoordinator.start()',
      durationMs: 14,
      status: 'SUCCESS',
      timestamp: '14:32:01.107',
      correlationId: 'corr-88f2a1b9-37c4',
      details: 'Initialized Saga state machine. Order state saved as CREATED. Idempotency key verified: idemp-1792258900.'
    },
    {
      id: 'span-03',
      serviceId: 'inventory-service',
      serviceName: 'Inventory Service',
      operation: 'reserveStockAtomic()',
      durationMs: 9,
      status: 'SUCCESS',
      timestamp: '14:32:01.122',
      correlationId: 'corr-88f2a1b9-37c4',
      details: 'Executed conditional SQL update: UPDATE inventory_items SET available_quantity = available_quantity - 1 WHERE available_quantity >= 1. Row count = 1.'
    },
    {
      id: 'span-04',
      serviceId: 'payment-service',
      serviceName: 'Payment Service',
      operation: 'authorizePayment(StripeAdapter)',
      durationMs: 32,
      status: 'SUCCESS',
      timestamp: '14:32:01.132',
      correlationId: 'corr-88f2a1b9-37c4',
      details: 'Routed via StripeSimulatorAdapter. Authorization code: auth_99x_stripe. Idempotency verified in payment_transactions.'
    },
    {
      id: 'span-05',
      serviceId: 'order-service',
      serviceName: 'Order Service',
      operation: 'TransactionalOutbox.record()',
      durationMs: 6,
      status: 'SUCCESS',
      timestamp: '14:32:01.165',
      correlationId: 'corr-88f2a1b9-37c4',
      details: 'Order state transitioned to CONFIRMED. Event OrderConfirmed saved into outbox_events table within same ACID transaction.'
    },
    {
      id: 'span-06',
      serviceId: 'kafka-bus',
      serviceName: 'Apache Kafka Bus',
      operation: 'Publish to pulsecart.order.events',
      durationMs: 2,
      status: 'SUCCESS',
      timestamp: '14:32:01.172',
      correlationId: 'corr-88f2a1b9-37c4',
      details: 'EventEnvelope published to partition 0 with key ORD-DEMO-100.'
    },
    {
      id: 'span-07',
      serviceId: 'fulfillment-service',
      serviceName: 'Fulfillment Service',
      operation: 'OrderEventListener.onOrderConfirmed()',
      durationMs: 11,
      status: 'SUCCESS',
      timestamp: '14:32:01.175',
      correlationId: 'corr-88f2a1b9-37c4',
      details: 'Idempotent consumer picked up order event. Warehouse WH-EAST-1 assigned. Status: PICKING. FulfillmentCompleted written to outbox.'
    },
    {
      id: 'span-08',
      serviceId: 'shipping-service',
      serviceName: 'Shipping Service',
      operation: 'FulfillmentEventListener.onFulfillment()',
      durationMs: 15,
      status: 'SUCCESS',
      timestamp: '14:32:01.187',
      correlationId: 'corr-88f2a1b9-37c4',
      details: 'Routed to FedExSimulatorAdapter. Tracking generated: FDX-748921849. Outbox event ShippingDispatched recorded.'
    },
    {
      id: 'span-09',
      serviceId: 'notification-service',
      serviceName: 'Notification Service',
      operation: 'NotificationEventListener.onOrderConfirmed()',
      durationMs: 8,
      status: 'SUCCESS',
      timestamp: '14:32:01.203',
      correlationId: 'corr-88f2a1b9-37c4',
      details: 'Rendered email template ORDER_CONFIRMED. Simulated dispatch to cust-01@pulsecart.io. Notification status: SENT.'
    }
  ];

  const totalDuration = sampleSpans.reduce((sum, s) => sum + s.durationMs, 0);

  return (
    <div style={{ padding: '24px', maxWidth: '1440px', margin: '0 auto' }}>
      {/* Header & Search Bar */}
      <div style={{
        background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.9) 0%, rgba(3, 7, 18, 0.95) 100%)',
        border: '1px solid #1e293b',
        borderRadius: '16px',
        padding: '24px 30px',
        marginBottom: '28px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
              <span className="badge badge-indigo">Distributed Tracing</span>
              <span className="badge badge-emerald">W3C TraceContext</span>
            </div>
            <h2 style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc' }}>
              Request X-Ray & Trace Timeline
            </h2>
            <p style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
              Reconstruct complete end-to-end distributed execution paths across synchronous REST calls and asynchronous Kafka events.
            </p>
          </div>

          <div style={{ textAlign: 'right' }}>
            <span style={{ fontSize: '1.6rem', fontWeight: 800, color: '#38bdf8', display: 'block' }}>{totalDuration} ms</span>
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>End-to-End Latency</span>
          </div>
        </div>

        {/* Search input */}
        <div style={{ display: 'flex', gap: '10px', maxWidth: '600px' }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <Search size={18} color="#64748b" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              value={lookupId}
              onChange={(e) => setLookupId(e.target.value)}
              placeholder="Enter Order ID or Correlation ID..."
              style={{
                width: '100%',
                padding: '10px 14px 10px 38px',
                borderRadius: '8px',
                background: '#1e293b',
                border: '1px solid #334155',
                color: '#f8fafc',
                fontSize: '0.9rem',
                fontFamily: 'monospace'
              }}
            />
          </div>
          <button className="btn-primary" style={{ padding: '10px 20px', fontSize: '0.85rem' }}>
            Lookup Trace
          </button>
        </div>
      </div>

      {/* Main Timeline and Details Split */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '24px' }}>
        
        {/* Timeline Spans List */}
        <div style={{ background: '#0b1120', padding: '20px', borderRadius: '14px', border: '1px solid #1e293b' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc' }}>
              Execution Spans ({sampleSpans.length})
            </h3>
            <span className="font-mono" style={{ fontSize: '0.75rem', color: '#64748b' }}>
              Correlation: corr-88f2a1b9-37c4
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {sampleSpans.map((span, idx) => {
              const isSelected = activeSpan?.id === span.id;
              const widthPct = Math.max(12, Math.min(100, (span.durationMs / 40) * 100));

              return (
                <div
                  key={span.id}
                  onClick={() => setActiveSpan(span)}
                  style={{
                    padding: '12px 14px',
                    borderRadius: '8px',
                    background: isSelected ? '#1e293b' : '#0f172a',
                    border: '1px solid',
                    borderColor: isSelected ? '#38bdf8' : '#1e293b',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span className="font-mono" style={{ fontSize: '0.75rem', color: '#64748b' }}>#{idx + 1}</span>
                      <span style={{ fontSize: '0.85rem', fontWeight: 700, color: '#f8fafc' }}>{span.serviceName}</span>
                      <span className="font-mono" style={{ fontSize: '0.75rem', color: '#38bdf8' }}>{span.operation}</span>
                    </div>
                    <span className="font-mono" style={{ fontSize: '0.8rem', fontWeight: 700, color: '#34d399' }}>
                      {span.durationMs}ms
                    </span>
                  </div>

                  {/* Latency Bar visualization */}
                  <div style={{ height: '4px', background: '#1e293b', borderRadius: '2px', overflow: 'hidden' }}>
                    <div style={{
                      height: '100%',
                      width: `${widthPct}%`,
                      background: 'linear-gradient(90deg, #38bdf8, #818cf8)',
                      borderRadius: '2px'
                    }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Selected Span Inspector */}
        <div style={{ background: '#0b1120', padding: '20px', borderRadius: '14px', border: '1px solid #1e293b' }}>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', marginBottom: '16px' }}>
            Span Telemetry Inspector
          </h3>

          {activeSpan ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ background: '#080c16', padding: '14px', borderRadius: '8px', border: '1px solid #1e293b' }}>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', marginBottom: '4px' }}>Target Service</span>
                <span style={{ fontSize: '1.1rem', fontWeight: 700, color: '#38bdf8' }}>{activeSpan.serviceName}</span>
              </div>

              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', marginBottom: '4px' }}>Operation</span>
                <div className="font-mono" style={{ background: '#080c16', padding: '10px', borderRadius: '6px', fontSize: '0.85rem', color: '#f8fafc' }}>
                  {activeSpan.operation}
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div>
                  <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', marginBottom: '4px' }}>Execution Time</span>
                  <div className="font-mono" style={{ background: '#080c16', padding: '8px', borderRadius: '6px', fontSize: '0.85rem', color: '#34d399' }}>
                    {activeSpan.durationMs} milliseconds
                  </div>
                </div>
                <div>
                  <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', marginBottom: '4px' }}>Status</span>
                  <div className="font-mono" style={{ background: '#080c16', padding: '8px', borderRadius: '6px', fontSize: '0.85rem', color: '#38bdf8' }}>
                    {activeSpan.status}
                  </div>
                </div>
              </div>

              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', marginBottom: '4px' }}>Correlation ID</span>
                <div className="font-mono" style={{ background: '#080c16', padding: '8px', borderRadius: '6px', fontSize: '0.8rem', color: '#94a3b8' }}>
                  {activeSpan.correlationId}
                </div>
              </div>

              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', marginBottom: '4px' }}>Detailed Execution Log</span>
                <div className="code-box">
                  {activeSpan.details}
                </div>
              </div>
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '60px 20px', color: '#64748b' }}>
              <Layers size={36} style={{ margin: '0 auto 12px', opacity: 0.5 }} />
              <p style={{ fontSize: '0.9rem' }}>Select any span in the timeline to view its telemetry details.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
