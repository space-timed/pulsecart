import React, { useState } from 'react';
import { ArchitectureExplorer } from './components/ArchitectureExplorer';
import { RequestXRay } from './components/RequestXRay';
import { PatternAcademy } from './components/PatternAcademy';
import { DataAndKafkaExplorer } from './components/DataAndKafkaExplorer';
import { FailureLaboratory } from './components/FailureLaboratory';
import { ObservabilityWorkbench } from './components/ObservabilityWorkbench';
import { KubernetesExplorer } from './components/KubernetesExplorer';
import { Activity, Layers, Radio, BookOpen, Database, ExternalLink, ShieldAlert, BarChart3, Server } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'architecture' | 'xray' | 'patterns' | 'datakafka' | 'failurelab' | 'observability' | 'kubernetes'>('architecture');

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#030712' }}>
      {/* Studio Top Navigation Bar */}
      <header style={{
        position: 'sticky',
        top: 0,
        zIndex: 50,
        backgroundColor: 'rgba(3, 7, 18, 0.95)',
        backdropFilter: 'blur(12px)',
        borderBottom: '1px solid #1e293b'
      }}>
        <div style={{ maxWidth: '1440px', margin: '0 auto', padding: '14px 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          
          {/* Logo & Lab identity */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div style={{
              width: '38px',
              height: '38px',
              borderRadius: '10px',
              background: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 0 15px rgba(6, 182, 212, 0.4)'
            }}>
              <Activity size={22} color="#030712" />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontSize: '1.2rem', fontWeight: 800, letterSpacing: '-0.02em', color: '#f8fafc' }}>
                  PulseCart <span style={{ color: '#38bdf8' }}>Studio</span>
                </span>
                <span className="badge badge-emerald" style={{ fontSize: '0.65rem' }}>Lab Engine v1.0</span>
              </div>
              <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
                Microservices Architecture Laboratory & X-Ray Explorer
              </span>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <button
              onClick={() => setActiveTab('architecture')}
              className={`nav-tab ${activeTab === 'architecture' ? 'active' : ''}`}
            >
              <Layers size={16} />
              <span>Architecture</span>
            </button>

            <button
              onClick={() => setActiveTab('xray')}
              className={`nav-tab ${activeTab === 'xray' ? 'active' : ''}`}
            >
              <Activity size={16} />
              <span>Request X-Ray</span>
            </button>

            <button
              onClick={() => setActiveTab('patterns')}
              className={`nav-tab ${activeTab === 'patterns' ? 'active' : ''}`}
            >
              <BookOpen size={16} />
              <span>Pattern Academy</span>
            </button>

            <button
              onClick={() => setActiveTab('datakafka')}
              className={`nav-tab ${activeTab === 'datakafka' ? 'active' : ''}`}
            >
              <Database size={16} />
              <span>Data & Kafka</span>
            </button>

            <button
              onClick={() => setActiveTab('failurelab')}
              className={`nav-tab ${activeTab === 'failurelab' ? 'active' : ''}`}
              style={{ color: activeTab === 'failurelab' ? '#fb7185' : undefined }}
            >
              <ShieldAlert size={16} color={activeTab === 'failurelab' ? '#fb7185' : undefined} />
              <span>Failure Lab</span>
            </button>

            <button
              onClick={() => setActiveTab('observability')}
              className={`nav-tab ${activeTab === 'observability' ? 'active' : ''}`}
              style={{ color: activeTab === 'observability' ? '#f59e0b' : undefined }}
            >
              <BarChart3 size={16} color={activeTab === 'observability' ? '#f59e0b' : undefined} />
              <span>Observability</span>
            </button>

            <button
              onClick={() => setActiveTab('kubernetes')}
              className={`nav-tab ${activeTab === 'kubernetes' ? 'active' : ''}`}
              style={{ color: activeTab === 'kubernetes' ? '#38bdf8' : undefined }}
            >
              <Server size={16} color={activeTab === 'kubernetes' ? '#38bdf8' : undefined} />
              <span>Kubernetes</span>
            </button>
          </nav>

          {/* Right Action: Link to Store */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <a
              href="http://localhost:3000"
              target="_blank"
              rel="noreferrer"
              className="btn-secondary"
              style={{ fontSize: '0.8rem', padding: '8px 14px', textDecoration: 'none' }}
              title="Open customer store application"
            >
              <span>Launch Store App</span>
              <ExternalLink size={14} />
            </a>
          </div>
        </div>
      </header>

      {/* Main View Area */}
      <main style={{ flex: 1, paddingBottom: '60px' }}>
        {activeTab === 'architecture' && <ArchitectureExplorer />}
        {activeTab === 'xray' && <RequestXRay />}
        {activeTab === 'patterns' && <PatternAcademy />}
        {activeTab === 'datakafka' && <DataAndKafkaExplorer />}
        {activeTab === 'failurelab' && <FailureLaboratory />}
        {activeTab === 'observability' && <ObservabilityWorkbench />}
        {activeTab === 'kubernetes' && <KubernetesExplorer />}
      </main>

      {/* Footer */}
      <footer style={{ borderTop: '1px solid #1e293b', padding: '16px 24px', background: '#020617', textAlign: 'center', fontSize: '0.75rem', color: '#64748b' }}>
        PulseCart Studio • 15 Microservices • Java 26 / Spring Boot 3.4.3 • PostgreSQL 16 • Apache Kafka KRaft • React 19 • Docker Isolated Project
      </footer>
    </div>
  );
};
