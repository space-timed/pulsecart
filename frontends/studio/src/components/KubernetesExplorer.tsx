import React, { useState } from 'react';
import {
  Server,
  Cpu,
  Layers,
  Activity,
  CheckCircle2,
  RefreshCw,
  Play,
  RotateCcw,
  ArrowRight,
  ExternalLink,
  ShieldCheck,
  Zap,
  Info
} from 'lucide-react';

interface K8sDeployment {
  name: string;
  category: 'Gateway' | 'Core Commerce' | 'Async Commerce' | 'CQRS & Projections' | 'Frontend';
  replicas: number;
  readyReplicas: number;
  version: string;
  port: number;
  cpuRequest: string;
  memoryRequest: string;
  readinessPath: string;
  livenessPath: string;
}

const DEPLOYMENTS: K8sDeployment[] = [
  { name: 'api-gateway', category: 'Gateway', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8080, cpuRequest: '100m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'customer-service', category: 'Core Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8081, cpuRequest: '50m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'catalog-service', category: 'Core Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8082, cpuRequest: '100m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'pricing-service', category: 'Core Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8083, cpuRequest: '50m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'inventory-service', category: 'Core Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8084, cpuRequest: '100m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'cart-service', category: 'Core Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8085, cpuRequest: '50m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'payment-service', category: 'Core Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8086, cpuRequest: '100m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'order-service', category: 'Core Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8087, cpuRequest: '100m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'fulfillment-service', category: 'Async Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8088, cpuRequest: '50m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'shipping-service', category: 'Async Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8089, cpuRequest: '50m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'notification-service', category: 'Async Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8090, cpuRequest: '50m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'returns-service', category: 'Async Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8091, cpuRequest: '50m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'search-service', category: 'CQRS & Projections', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8092, cpuRequest: '100m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'analytics-service', category: 'CQRS & Projections', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8093, cpuRequest: '50m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'promotion-service', category: 'Core Commerce', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 8094, cpuRequest: '50m', memoryRequest: '256Mi', readinessPath: '/actuator/health/readiness', livenessPath: '/actuator/health/liveness' },
  { name: 'store-ui', category: 'Frontend', replicas: 2, readyReplicas: 2, version: '1.0.0', port: 3000, cpuRequest: '25m', memoryRequest: '64Mi', readinessPath: '/', livenessPath: '/' },
  { name: 'studio-ui', category: 'Frontend', replicas: 1, readyReplicas: 1, version: '1.0.0', port: 3001, cpuRequest: '25m', memoryRequest: '64Mi', readinessPath: '/', livenessPath: '/' }
];

export const KubernetesExplorer: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [isRolling, setIsRolling] = useState<boolean>(false);
  const [rolloutStep, setRolloutStep] = useState<number>(0);
  const [targetDeployment, setTargetDeployment] = useState<string>('order-service');

  const filteredDeployments = selectedCategory === 'ALL'
    ? DEPLOYMENTS
    : DEPLOYMENTS.filter(d => d.category === selectedCategory);

  const triggerRollingUpdate = () => {
    setIsRolling(true);
    setRolloutStep(1);

    setTimeout(() => {
      setRolloutStep(2); // New Pod creating
      setTimeout(() => {
        setRolloutStep(3); // Readiness probe running
        setTimeout(() => {
          setRolloutStep(4); // New Pod Ready, traffic routed
          setTimeout(() => {
            setRolloutStep(5); // Old Pod draining in-flight requests
            setTimeout(() => {
              setRolloutStep(6); // Old Pod terminated, update complete
              setIsRolling(false);
            }, 800);
          }, 800);
        }, 800);
      }, 800);
    }, 800);
  };

  const resetRollout = () => {
    setIsRolling(false);
    setRolloutStep(0);
  };

  return (
    <div style={{ maxWidth: '1440px', margin: '0 auto', padding: '24px' }}>
      {/* Header */}
      <div style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
            <h1 style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc', letterSpacing: '-0.02em' }}>
              Kubernetes Cluster & Helm Orchestration
            </h1>
            <span className="status-pill status-healthy">Namespace: pulsecart</span>
          </div>
          <p style={{ color: '#94a3b8', fontSize: '0.85rem' }}>
            Production-grade Helm chart manifests, RollingUpdate zero-downtime deployments, liveness/readiness probes, and HPA autoscaling.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span className="status-pill status-healthy" style={{ fontSize: '0.8rem', padding: '6px 12px' }}>
            17 Deployments • 33 Pods Ready • 0 CrashLoop
          </span>
        </div>
      </div>

      {/* Cluster Resource Overview Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '14px', marginBottom: '24px' }}>
        <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '10px', padding: '16px' }}>
          <div style={{ fontSize: '0.7rem', color: '#64748b', textTransform: 'uppercase', marginBottom: '6px' }}>Cluster Topology</div>
          <div style={{ fontSize: '1.2rem', fontWeight: 800, color: '#f8fafc', marginBottom: '2px' }}>3 Nodes (Ready)</div>
          <div style={{ fontSize: '0.75rem', color: '#38bdf8' }}>control-plane, worker-01, worker-02</div>
        </div>

        <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '10px', padding: '16px' }}>
          <div style={{ fontSize: '0.7rem', color: '#64748b', textTransform: 'uppercase', marginBottom: '6px' }}>CPU Allocation</div>
          <div style={{ fontSize: '1.2rem', fontWeight: 800, color: '#34d399', marginBottom: '2px' }}>1.05 / 4.0 Cores</div>
          <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>26.2% Cluster Request Utilization</div>
        </div>

        <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '10px', padding: '16px' }}>
          <div style={{ fontSize: '0.7rem', color: '#64748b', textTransform: 'uppercase', marginBottom: '6px' }}>Memory Allocation</div>
          <div style={{ fontSize: '1.2rem', fontWeight: 800, color: '#38bdf8', marginBottom: '2px' }}>4.2 / 16.0 GiB</div>
          <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>26.2% Cluster Request Utilization</div>
        </div>

        <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '10px', padding: '16px' }}>
          <div style={{ fontSize: '0.7rem', color: '#64748b', textTransform: 'uppercase', marginBottom: '6px' }}>Helm Release</div>
          <div style={{ fontSize: '1.2rem', fontWeight: 800, color: '#f8fafc', marginBottom: '2px' }}>pulsecart-prod</div>
          <div style={{ fontSize: '0.75rem', color: '#10b981' }}>Chart v1.0.0 • AppVersion 1.0.0</div>
        </div>
      </div>

      {/* Interactive Rolling Update Simulator */}
      <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '12px', padding: '20px', marginBottom: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
              <RefreshCw size={16} color="#38bdf8" />
              <h2 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>
                Zero-Downtime Rolling Update Simulator
              </h2>
            </div>
            <p style={{ color: '#94a3b8', fontSize: '0.8rem' }}>
              Simulate Kubernetes RollingUpdate with Spring Boot readiness probes, zero request dropping, and graceful termination.
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <select
              value={targetDeployment}
              onChange={e => setTargetDeployment(e.target.value)}
              disabled={isRolling}
              style={{
                background: '#040711',
                border: '1px solid #334155',
                borderRadius: '8px',
                padding: '8px 12px',
                color: '#f8fafc',
                fontSize: '0.8rem'
              }}
            >
              <option value="order-service">order-service (v1.0 -&gt; v1.1)</option>
              <option value="catalog-service">catalog-service (v1.0 -&gt; v1.1)</option>
              <option value="inventory-service">inventory-service (v1.0 -&gt; v1.1)</option>
            </select>

            <button
              onClick={triggerRollingUpdate}
              disabled={isRolling}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '8px 16px',
                borderRadius: '8px',
                background: isRolling ? '#334155' : '#06b6d4',
                color: '#030712',
                border: 'none',
                fontWeight: 700,
                fontSize: '0.8rem',
                cursor: isRolling ? 'not-allowed' : 'pointer'
              }}
            >
              {isRolling ? <RefreshCw size={14} className="animate-spin" /> : <Play size={14} />}
              <span>{isRolling ? 'Rollout in Progress...' : 'Trigger RollingUpdate'}</span>
            </button>

            {rolloutStep > 0 && !isRolling && (
              <button
                onClick={resetRollout}
                className="nav-tab"
                style={{ padding: '8px 12px', border: '1px solid #334155' }}
              >
                <RotateCcw size={14} />
              </button>
            )}
          </div>
        </div>

        {/* Rolling Update Stage Timeline */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '10px' }}>
          {[
            { step: 1, label: '1. Desired State', desc: 'Deploy spec updated to image v1.1' },
            { step: 2, label: '2. Pod Spawning', desc: 'New ReplicaSet spins up Pod v1.1' },
            { step: 3, label: '3. Readiness Probe', desc: 'HTTP /actuator/health/readiness check' },
            { step: 4, label: '4. Traffic Shift', desc: 'K8s Service shifts ingress to v1.1' },
            { step: 5, label: '5. Graceful Drain', desc: 'Old Pod drains in-flight requests' }
          ].map(s => {
            const isCompleted = rolloutStep >= s.step;
            const isCurrent = rolloutStep === s.step && isRolling;
            return (
              <div
                key={s.step}
                style={{
                  padding: '12px',
                  borderRadius: '8px',
                  background: isCompleted ? 'rgba(16, 185, 129, 0.08)' : isCurrent ? 'rgba(6, 182, 212, 0.15)' : '#080c16',
                  border: isCompleted ? '1px solid rgba(16, 185, 129, 0.3)' : isCurrent ? '1px solid #06b6d4' : '1px solid #1e293b'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                  <span style={{ fontSize: '0.75rem', fontWeight: 700, color: isCompleted ? '#34d399' : isCurrent ? '#38bdf8' : '#64748b' }}>
                    {s.label}
                  </span>
                  {isCompleted && <CheckCircle2 size={13} color="#34d399" />}
                </div>
                <div style={{ fontSize: '0.7rem', color: '#94a3b8' }}>{s.desc}</div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Workloads Table */}
      <div style={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: '12px', padding: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Server size={16} color="#38bdf8" />
            <h2 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>
              PulseCart Workloads & Pod Health Status
            </h2>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            {['ALL', 'Gateway', 'Core Commerce', 'Async Commerce', 'CQRS & Projections', 'Frontend'].map(cat => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                style={{
                  fontSize: '0.7rem',
                  padding: '4px 10px',
                  borderRadius: '6px',
                  background: selectedCategory === cat ? '#38bdf8' : '#1e293b',
                  color: selectedCategory === cat ? '#030712' : '#94a3b8',
                  border: 'none',
                  fontWeight: 600,
                  cursor: 'pointer'
                }}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #1e293b', color: '#64748b', fontSize: '0.7rem', textTransform: 'uppercase' }}>
                <th style={{ padding: '10px 12px' }}>Deployment</th>
                <th style={{ padding: '10px 12px' }}>Category</th>
                <th style={{ padding: '10px 12px' }}>Replicas</th>
                <th style={{ padding: '10px 12px' }}>Container Port</th>
                <th style={{ padding: '10px 12px' }}>Requests (CPU/RAM)</th>
                <th style={{ padding: '10px 12px' }}>Readiness Probe</th>
                <th style={{ padding: '10px 12px' }}>Liveness Probe</th>
                <th style={{ padding: '10px 12px' }}>Strategy</th>
              </tr>
            </thead>
            <tbody>
              {filteredDeployments.map(d => (
                <tr key={d.name} style={{ borderBottom: '1px solid #162032' }}>
                  <td style={{ padding: '10px 12px', fontWeight: 600, color: '#f8fafc' }}>
                    {d.name}
                  </td>
                  <td style={{ padding: '10px 12px', color: '#94a3b8', fontSize: '0.75rem' }}>
                    {d.category}
                  </td>
                  <td style={{ padding: '10px 12px' }}>
                    <span className="status-pill status-healthy" style={{ fontSize: '0.7rem' }}>
                      {d.readyReplicas} / {d.replicas} Ready
                    </span>
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: '#38bdf8' }}>
                    {d.port}
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: '#cbd5e1' }}>
                    {d.cpuRequest} / {d.memoryRequest}
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: '#34d399', fontSize: '0.7rem' }}>
                    GET {d.readinessPath}
                  </td>
                  <td style={{ padding: '10px 12px', fontFamily: 'var(--font-mono)', color: '#38bdf8', fontSize: '0.7rem' }}>
                    GET {d.livenessPath}
                  </td>
                  <td style={{ padding: '10px 12px', fontSize: '0.7rem', color: '#94a3b8' }}>
                    RollingUpdate (0 unavail)
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
