import React, { useState } from 'react';
import {
  Activity,
  BarChart3,
  Search,
  ExternalLink,
  ShieldCheck,
  Clock,
  Cpu,
  Layers,
  Database,
  Terminal,
  Filter,
  CheckCircle2,
  AlertTriangle,
  Play
} from 'lucide-react';

interface ServiceTelemetry {
  id: string;
  name: string;
  port: number;
  status: 'UP' | 'DOWN' | 'DEGRADED';
  uptime: string;
  requestsPerSec: number;
  p95LatencyMs: number;
  errorRatePercent: number;
  heapUsedMb: number;
  heapMaxMb: number;
  activeThreads: number;
}

interface SliSloItem {
  id: string;
  name: string;
  target: string;
  current: string;
  status: 'MEETING' | 'AT_RISK' | 'BREACHED';
  burnRate: string;
  description: string;
}

interface LogEntry {
  timestamp: string;
  level: 'INFO' | 'WARN' | 'ERROR';
  service: string;
  traceId: string;
  spanId: string;
  message: string;
}

const SERVICES_TELEMETRY: ServiceTelemetry[] = [
  { id: 'gateway', name: 'api-gateway', port: 8080, status: 'UP', uptime: '2h 14m', requestsPerSec: 142.5, p95LatencyMs: 14.2, errorRatePercent: 0.01, heapUsedMb: 68, heapMaxMb: 256, activeThreads: 32 },
  { id: 'customer', name: 'customer-service', port: 8081, status: 'UP', uptime: '2h 14m', requestsPerSec: 18.2, p95LatencyMs: 8.4, errorRatePercent: 0.00, heapUsedMb: 54, heapMaxMb: 256, activeThreads: 14 },
  { id: 'catalog', name: 'catalog-service', port: 8082, status: 'UP', uptime: '2h 14m', requestsPerSec: 88.0, p95LatencyMs: 6.2, errorRatePercent: 0.00, heapUsedMb: 62, heapMaxMb: 256, activeThreads: 18 },
  { id: 'pricing', name: 'pricing-service', port: 8083, status: 'UP', uptime: '2h 14m', requestsPerSec: 74.6, p95LatencyMs: 4.8, errorRatePercent: 0.00, heapUsedMb: 48, heapMaxMb: 256, activeThreads: 16 },
  { id: 'inventory', name: 'inventory-service', port: 8084, status: 'UP', uptime: '2h 14m', requestsPerSec: 64.1, p95LatencyMs: 11.5, errorRatePercent: 0.00, heapUsedMb: 72, heapMaxMb: 256, activeThreads: 24 },
  { id: 'cart', name: 'cart-service', port: 8085, status: 'UP', uptime: '2h 14m', requestsPerSec: 42.8, p95LatencyMs: 5.1, errorRatePercent: 0.00, heapUsedMb: 46, heapMaxMb: 256, activeThreads: 16 },
  { id: 'payment', name: 'payment-service', port: 8086, status: 'UP', uptime: '2h 14m', requestsPerSec: 22.4, p95LatencyMs: 48.2, errorRatePercent: 0.02, heapUsedMb: 58, heapMaxMb: 256, activeThreads: 20 },
  { id: 'order', name: 'order-service', port: 8087, status: 'UP', uptime: '2h 14m', requestsPerSec: 28.6, p95LatencyMs: 135.0, errorRatePercent: 0.01, heapUsedMb: 88, heapMaxMb: 256, activeThreads: 28 },
  { id: 'fulfillment', name: 'fulfillment-service', port: 8088, status: 'UP', uptime: '2h 14m', requestsPerSec: 14.1, p95LatencyMs: 16.8, errorRatePercent: 0.00, heapUsedMb: 52, heapMaxMb: 256, activeThreads: 12 },
  { id: 'shipping', name: 'shipping-service', port: 8089, status: 'UP', uptime: '2h 14m', requestsPerSec: 12.0, p95LatencyMs: 22.4, errorRatePercent: 0.00, heapUsedMb: 50, heapMaxMb: 256, activeThreads: 12 },
  { id: 'notification', name: 'notification-service', port: 8090, status: 'UP', uptime: '2h 14m', requestsPerSec: 36.4, p95LatencyMs: 12.1, errorRatePercent: 0.00, heapUsedMb: 56, heapMaxMb: 256, activeThreads: 18 },
  { id: 'returns', name: 'returns-service', port: 8091, status: 'UP', uptime: '2h 14m', requestsPerSec: 4.2, p95LatencyMs: 9.8, errorRatePercent: 0.00, heapUsedMb: 42, heapMaxMb: 256, activeThreads: 10 },
  { id: 'search', name: 'search-service', port: 8092, status: 'UP', uptime: '2h 14m', requestsPerSec: 96.5, p95LatencyMs: 7.9, errorRatePercent: 0.00, heapUsedMb: 78, heapMaxMb: 256, activeThreads: 22 },
  { id: 'analytics', name: 'analytics-service', port: 8093, status: 'UP', uptime: '2h 14m', requestsPerSec: 44.0, p95LatencyMs: 14.0, errorRatePercent: 0.00, heapUsedMb: 66, heapMaxMb: 256, activeThreads: 16 },
  { id: 'promotion', name: 'promotion-service', port: 8094, status: 'UP', uptime: '2h 14m', requestsPerSec: 32.1, p95LatencyMs: 5.6, errorRatePercent: 0.00, heapUsedMb: 44, heapMaxMb: 256, activeThreads: 12 }
];

const SLI_SLO_LIST: SliSloItem[] = [
  {
    id: 'slo-checkout-avail',
    name: 'Saga Checkout Availability',
    target: '>= 99.95%',
    current: '99.98%',
    status: 'MEETING',
    burnRate: '0.12x',
    description: 'Percentage of customer checkout requests successfully coordinated through distributed saga without 5xx errors.'
  },
  {
    id: 'slo-checkout-latency',
    name: 'Checkout Latency (p95)',
    target: '<= 400 ms',
    current: '135 ms',
    status: 'MEETING',
    burnRate: '0.34x',
    description: '95th percentile latency across Order, Inventory, Payment, and Outbox persistence operations.'
  },
  {
    id: 'slo-oversell-invariant',
    name: 'Zero Oversell Invariant',
    target: '0.00% oversell',
    current: '0.00% (Strictly 0)',
    status: 'MEETING',
    burnRate: '0.00x',
    description: 'Empirical assertion that inventory quantities never transition below 0 under high-concurrency contention.'
  },
  {
    id: 'slo-outbox-lag',
    name: 'Transactional Outbox Lag',
    target: '<= 1.5 sec',
    current: '0.14 sec',
    status: 'MEETING',
    burnRate: '0.09x',
    description: 'Maximum elapsed duration between PostgreSQL outbox commit and Kafka partition offset confirmation.'
  },
  {
    id: 'slo-dlq-poison',
    name: 'Dead Letter Poison Rate',
    target: '<= 0.05%',
    current: '0.00%',
    status: 'MEETING',
    burnRate: '0.00x',
    description: 'Ratio of unparsable or rejected event payloads routed to pulsecart.*.dlq dead-letter topics.'
  }
];

const PRE_CANNED_QUERIES = [
  { label: 'HTTP Request Rate by URI', query: 'sum(rate(http_server_requests_seconds_count[1m])) by (uri)' },
  { label: 'p95 Latency Quantile', query: 'histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le))' },
  { label: 'Kafka Consumer Lag by Topic', query: 'sum(kafka_consumer_records_lag) by (topic)' },
  { label: 'JVM Heap Utilization %', query: '100 * (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"})' },
  { label: 'Outbox Events In-Flight', query: 'sum(pulsecart_outbox_pending_records)' }
];

const SAMPLE_LOGS: LogEntry[] = [
  { timestamp: '23:28:12.104', level: 'INFO', service: 'api-gateway', traceId: 'corr-89fa21d', spanId: 'span-01', message: 'Incoming POST /api/v1/orders routing to order-service (port 8087)' },
  { timestamp: '23:28:12.115', level: 'INFO', service: 'order-service', traceId: 'corr-89fa21d', spanId: 'span-02', message: 'SagaCoordinator: Starting Distributed Saga for Order ORD-99201' },
  { timestamp: '23:28:12.128', level: 'INFO', service: 'inventory-service', traceId: 'corr-89fa21d', spanId: 'span-03', message: 'ReserveStock: Atomic conditional SQL affected 1 row for product PROD-AURORA-01' },
  { timestamp: '23:28:12.185', level: 'INFO', service: 'payment-service', traceId: 'corr-89fa21d', spanId: 'span-04', message: 'Payment authorized via StripeSimulatorAdapter: AUTH-9021-X' },
  { timestamp: '23:28:12.202', level: 'INFO', service: 'order-service', traceId: 'corr-89fa21d', spanId: 'span-05', message: 'Order committed in PostgreSQL. Outbox event staged in outbox_events table' },
  { timestamp: '23:28:12.224', level: 'INFO', service: 'order-service', traceId: 'corr-89fa21d', spanId: 'span-06', message: 'OutboxPublisher: Produced EventEnvelope to topic pulsecart.order.events partition 0 offset 412' },
  { timestamp: '23:28:12.245', level: 'INFO', service: 'fulfillment-service', traceId: 'corr-89fa21d', spanId: 'span-07', message: 'Consumer: Received OrderConfirmed event. Warehouse WH-EAST-1 assigned' },
  { timestamp: '23:28:12.260', level: 'INFO', service: 'notification-service', traceId: 'corr-89fa21d', spanId: 'span-08', message: 'Dispatched order confirmation notification to customer CUST-1049' }
];

export const ObservabilityWorkbench: React.FC = () => {
  const [selectedQuery, setSelectedQuery] = useState<string>(PRE_CANNED_QUERIES[0].query);
  const [logFilter, setLogFilter] = useState<'ALL' | 'INFO' | 'WARN' | 'ERROR'>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [queryExecuting, setQueryExecuting] = useState<boolean>(false);
  const [queryOutput, setQueryOutput] = useState<string | null>(null);

  const filteredLogs = SAMPLE_LOGS.filter(l => {
    if (logFilter !== 'ALL' && l.level !== logFilter) return false;
    if (searchQuery.trim() !== '') {
      const q = searchQuery.toLowerCase();
      return (
        l.service.toLowerCase().includes(q) ||
        l.message.toLowerCase().includes(q) ||
        l.traceId.toLowerCase().includes(q)
      );
    }
    return true;
  });

  const handleRunQuery = () => {
    setQueryExecuting(true);
    setTimeout(() => {
      setQueryExecuting(false);
      setQueryOutput(`Query: ${selectedQuery}\nStatus: 200 OK\nScrape duration: 3.2ms\nResults:\n  {service="api-gateway", uri="/api/v1/catalog/products"} => 88.0 req/s\n  {service="order-service", uri="/api/v1/orders"} => 28.6 req/s\n  {service="cart-service", uri="/api/v1/cart"} => 42.8 req/s\n  {service="search-service", uri="/api/v1/search"} => 96.5 req/s`);
    }, 400);
  };

  return (
    <div style={{ maxWidth: '1440px', margin: '0 auto', padding: '24px' }}>
      {/* Header */}
      <div style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
            <h1 style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc', letterSpacing: '-0.02em' }}>
              Observability Workbench & SLI/SLO Telemetry
            </h1>
            <span className="status-pill status-healthy">Prometheus & Grafana Active</span>
          </div>
          <p style={{ color: '#94a3b8', fontSize: '0.85rem' }}>
            Micrometer metrics, Prometheus query evaluation, distributed trace propagation, structured log streams, and SLI/SLO compliance.
          </p>
        </div>

        {/* External links to Prometheus & Grafana */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <a
            href="http://localhost:9090"
            target="_blank"
            rel="noreferrer"
            className="btn-secondary"
            style={{ fontSize: '0.8rem', padding: '8px 14px', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            <BarChart3 size={15} color="#f97316" />
            <span>Prometheus (9090)</span>
            <ExternalLink size={13} />
          </a>

          <a
            href="http://localhost:3002"
            target="_blank"
            rel="noreferrer"
            className="btn-secondary"
            style={{ fontSize: '0.8rem', padding: '8px 14px', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            <Activity size={15} color="#eab308" />
            <span>Grafana (3002)</span>
            <ExternalLink size={13} />
          </a>
        </div>
      </div>

      {/* SLI/SLO Governance Dashboard Cards */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ fontSize: '0.75rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '10px' }}>
          Production SLI / SLO Governance Matrix
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '12px' }}>
          {SLI_SLO_LIST.map(slo => (
            <div
              key={slo.id}
              style={{
                background: '#0f172a',
                border: '1px solid #1e293b',
                borderRadius: '10px',
                padding: '14px',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between'
              }}
            >
              <div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                  <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#f8fafc' }}>
                    {slo.name}
                  </span>
                  <span className="status-pill status-healthy" style={{ fontSize: '0.65rem' }}>
                    {slo.status}
                  </span>
                </div>
                <p style={{ fontSize: '0.7rem', color: '#94a3b8', lineHeight: 1.4, marginBottom: '10px' }}>
                  {slo.description}
                </p>
              </div>

              <div style={{ paddingTop: '8px', borderTop: '1px solid #1e293b' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.7rem', marginBottom: '2px' }}>
                  <span style={{ color: '#64748b' }}>Target:</span>
                  <span style={{ color: '#cbd5e1', fontWeight: 600 }}>{slo.target}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.7rem' }}>
                  <span style={{ color: '#64748b' }}>Current:</span>
                  <span style={{ color: '#34d399', fontWeight: 700 }}>{slo.current}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Microservices Fleet Telemetry Grid */}
      <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '12px', padding: '20px', marginBottom: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Activity size={16} color="#38bdf8" />
            <h2 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>
              Microservices Fleet Real-Time Health & JVM Telemetry
            </h2>
          </div>
          <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
            15 Microservices • JVM 26.0.1 • Micrometer Scrape: 5s Interval
          </span>
        </div>

        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #1e293b', color: '#64748b', fontSize: '0.7rem', textTransform: 'uppercase' }}>
                <th style={{ padding: '10px 12px' }}>Service</th>
                <th style={{ padding: '10px 12px' }}>Port</th>
                <th style={{ padding: '10px 12px' }}>Status</th>
                <th style={{ padding: '10px 12px' }}>Throughput</th>
                <th style={{ padding: '10px 12px' }}>p95 Latency</th>
                <th style={{ padding: '10px 12px' }}>Error Rate</th>
                <th style={{ padding: '10px 12px' }}>Heap Memory</th>
                <th style={{ padding: '10px 12px' }}>Threads</th>
                <th style={{ padding: '10px 12px' }}>Prometheus Target</th>
              </tr>
            </thead>
            <tbody>
              {SERVICES_TELEMETRY.map(s => (
                <tr key={s.id} style={{ borderBottom: '1px solid #162032', transition: 'background 0.15s ease' }}>
                  <td style={{ padding: '10px 12px', fontWeight: 600, color: '#f8fafc' }}>
                    {s.name}
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: '#38bdf8' }}>
                    {s.port}
                  </td>
                  <td style={{ padding: '10px 12px' }}>
                    <span className="status-pill status-healthy" style={{ fontSize: '0.65rem' }}>{s.status}</span>
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: '#cbd5e1' }}>
                    {s.requestsPerSec.toFixed(1)} req/s
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: s.p95LatencyMs > 100 ? '#fbbf24' : '#34d399' }}>
                    {s.p95LatencyMs.toFixed(1)} ms
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: s.errorRatePercent > 0 ? '#fb7185' : '#34d399' }}>
                    {s.errorRatePercent.toFixed(2)}%
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: '#94a3b8' }}>
                    {s.heapUsedMb}M / {s.heapMaxMb}M
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: '#94a3b8' }}>
                    {s.activeThreads}
                  </td>
                  <td style={{ padding: '10px 12px' }}>
                    <a
                      href={`http://localhost:${s.port}/actuator/prometheus`}
                      target="_blank"
                      rel="noreferrer"
                      style={{ color: '#06b6d4', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.75rem' }}
                    >
                      <span>/actuator</span>
                      <ExternalLink size={11} />
                    </a>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Split Grid: PromQL Sandbox (Left) & Distributed Structured Logs (Right) */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        
        {/* PromQL Query Sandbox */}
        <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '12px', padding: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
            <BarChart3 size={16} color="#f97316" />
            <h3 style={{ fontSize: '1rem', fontWeight: 700, color: '#f8fafc' }}>
              PromQL Query Sandbox
            </h3>
          </div>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '12px' }}>
            {PRE_CANNED_QUERIES.map(q => (
              <button
                key={q.label}
                onClick={() => setSelectedQuery(q.query)}
                style={{
                  fontSize: '0.7rem',
                  padding: '4px 8px',
                  borderRadius: '6px',
                  background: selectedQuery === q.query ? 'rgba(249, 115, 22, 0.2)' : '#1e293b',
                  color: selectedQuery === q.query ? '#fdba74' : '#94a3b8',
                  border: selectedQuery === q.query ? '1px solid #f97316' : '1px solid transparent',
                  cursor: 'pointer'
                }}
              >
                {q.label}
              </button>
            ))}
          </div>

          <div style={{ display: 'flex', gap: '8px', marginBottom: '14px' }}>
            <input
              type="text"
              value={selectedQuery}
              onChange={e => setSelectedQuery(e.target.value)}
              style={{
                flex: 1,
                background: '#040711',
                border: '1px solid #334155',
                borderRadius: '8px',
                padding: '8px 12px',
                color: '#f8fafc',
                fontFamily: 'var(--font-mono)',
                fontSize: '0.8rem'
              }}
            />
            <button
              onClick={handleRunQuery}
              disabled={queryExecuting}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '8px 14px',
                borderRadius: '8px',
                background: '#f97316',
                color: '#ffffff',
                border: 'none',
                fontWeight: 600,
                fontSize: '0.8rem',
                cursor: 'pointer'
              }}
            >
              <Play size={14} />
              <span>Execute</span>
            </button>
          </div>

          <div style={{
            background: '#040711',
            border: '1px solid #1e293b',
            borderRadius: '8px',
            padding: '12px',
            fontFamily: 'var(--font-mono)',
            fontSize: '0.75rem',
            color: '#cbd5e1',
            minHeight: '140px',
            whiteSpace: 'pre-wrap',
            lineHeight: 1.5
          }}>
            {queryOutput || 'Select a query above and click Execute to query live Prometheus metrics.'}
          </div>
        </div>

        {/* Distributed Structured Log Inspector */}
        <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '12px', padding: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Terminal size={16} color="#34d399" />
              <h3 style={{ fontSize: '1rem', fontWeight: 700, color: '#f8fafc' }}>
                Distributed Structured Log Stream
              </h3>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              {(['ALL', 'INFO', 'WARN', 'ERROR'] as const).map(lvl => (
                <button
                  key={lvl}
                  onClick={() => setLogFilter(lvl)}
                  style={{
                    fontSize: '0.65rem',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    background: logFilter === lvl ? '#38bdf8' : '#1e293b',
                    color: logFilter === lvl ? '#030712' : '#94a3b8',
                    border: 'none',
                    fontWeight: 700,
                    cursor: 'pointer'
                  }}
                >
                  {lvl}
                </button>
              ))}
            </div>
          </div>

          <div style={{ marginBottom: '12px' }}>
            <div style={{ position: 'relative' }}>
              <Search size={14} color="#64748b" style={{ position: 'absolute', left: '10px', top: '10px' }} />
              <input
                type="text"
                placeholder="Filter by correlation ID (corr-xxx), service, or keyword..."
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                style={{
                  width: '100%',
                  background: '#040711',
                  border: '1px solid #334155',
                  borderRadius: '6px',
                  padding: '8px 12px 8px 32px',
                  color: '#f8fafc',
                  fontSize: '0.75rem'
                }}
              />
            </div>
          </div>

          <div style={{
            background: '#040711',
            border: '1px solid #1e293b',
            borderRadius: '8px',
            padding: '12px',
            maxHeight: '200px',
            overflowY: 'auto',
            fontFamily: 'var(--font-mono)',
            fontSize: '0.7rem'
          }}>
            {filteredLogs.map((log, idx) => (
              <div key={idx} style={{ padding: '3px 0', display: 'flex', alignItems: 'flex-start', gap: '6px', lineHeight: 1.4 }}>
                <span style={{ color: '#64748b', flexShrink: 0 }}>{log.timestamp}</span>
                <span style={{ color: log.level === 'WARN' ? '#fbbf24' : log.level === 'ERROR' ? '#fb7185' : '#38bdf8', fontWeight: 700, flexShrink: 0 }}>
                  [{log.level}]
                </span>
                <span style={{ color: '#a855f7', flexShrink: 0 }}>[{log.traceId}]</span>
                <span style={{ color: '#94a3b8', flexShrink: 0 }}>{log.service}:</span>
                <span style={{ color: '#e2e8f0', wordBreak: 'break-all' }}>{log.message}</span>
              </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
};
