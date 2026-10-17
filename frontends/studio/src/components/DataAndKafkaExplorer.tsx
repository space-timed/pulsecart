import React, { useState } from 'react';
import { DATABASES_METADATA, KAFKA_TOPICS_METADATA } from '../data/metadata';
import { Database, Radio, ShieldCheck, AlertTriangle, RefreshCw, Send, CheckCircle2 } from 'lucide-react';

export const DataAndKafkaExplorer: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'databases' | 'kafka'>('databases');
  const [poisonSending, setPoisonSending] = useState(false);
  const [poisonResult, setPoisonResult] = useState<string | null>(null);

  const handleSendPoisonPill = async () => {
    setPoisonSending(true);
    setPoisonResult(null);
    setTimeout(() => {
      setPoisonSending(false);
      setPoisonResult('Poison pill emitted to pulsecart.order.events. Notification service intercepted corrupted payload, captured error trace, and isolated it to DLQ without halting partition!');
    }, 700);
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1440px', margin: '0 auto' }}>
      {/* Header & Mode Switcher */}
      <div style={{
        background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.9) 0%, rgba(3, 7, 18, 0.95) 100%)',
        border: '1px solid #1e293b',
        borderRadius: '16px',
        padding: '24px 30px',
        marginBottom: '28px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <span className="badge badge-indigo">Persistence & Messaging Infrastructure</span>
            <span className="badge badge-emerald">PostgreSQL 16 • Redis 7 • Kafka KRaft</span>
          </div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc' }}>
            Data & Event Bus Explorer
          </h2>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8', marginTop: '4px' }}>
            Inspect independent database boundaries per microservice and authoritative Kafka topic topologies.
          </p>
        </div>

        {/* Tab switch */}
        <div style={{ display: 'flex', gap: '6px', background: '#080c16', padding: '4px', borderRadius: '8px', border: '1px solid #1e293b' }}>
          <button
            onClick={() => setActiveTab('databases')}
            style={{
              padding: '8px 16px',
              borderRadius: '6px',
              fontSize: '0.85rem',
              fontWeight: 600,
              border: 'none',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              background: activeTab === 'databases' ? '#38bdf8' : 'transparent',
              color: activeTab === 'databases' ? '#030712' : '#94a3b8'
            }}
          >
            <Database size={15} />
            <span>15 Databases</span>
          </button>

          <button
            onClick={() => setActiveTab('kafka')}
            style={{
              padding: '8px 16px',
              borderRadius: '6px',
              fontSize: '0.85rem',
              fontWeight: 600,
              border: 'none',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              background: activeTab === 'kafka' ? '#38bdf8' : 'transparent',
              color: activeTab === 'kafka' ? '#030712' : '#94a3b8'
            }}
          >
            <Radio size={15} />
            <span>14 Kafka Topics</span>
          </button>
        </div>
      </div>

      {/* Content based on Active Tab */}
      {activeTab === 'databases' ? (
        /* Database per service list */
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '16px' }}>
          {DATABASES_METADATA.map(db => (
            <div key={db.serviceId} style={{ background: '#0b1120', padding: '20px', borderRadius: '12px', border: '1px solid #1e293b' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                <span className="font-mono" style={{ fontSize: '0.95rem', fontWeight: 700, color: '#38bdf8' }}>{db.dbName}</span>
                <span className="badge badge-emerald" style={{ fontSize: '0.65rem' }}>{db.engine}</span>
              </div>
              <span style={{ fontSize: '0.8rem', color: '#94a3b8', display: 'block', marginBottom: '12px' }}>
                Owner Service: <strong style={{ color: '#f8fafc' }}>{db.serviceId}</strong>
              </span>

              <div style={{ marginBottom: '12px' }}>
                <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', display: 'block', marginBottom: '4px' }}>
                  Managed Tables / Keys
                </span>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                  {db.primaryTables.map((t, idx) => (
                    <span key={idx} className="font-mono" style={{ background: '#1e293b', color: '#e2e8f0', padding: '3px 8px', borderRadius: '4px', fontSize: '0.75rem' }}>
                      {t}
                    </span>
                  ))}
                </div>
              </div>

              <div style={{ paddingTop: '10px', borderTop: '1px solid #1e293b', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: '#34d399' }}>
                <ShieldCheck size={14} />
                <span>{db.isolationGuarantee}</span>
              </div>
            </div>
          ))}
        </div>
      ) : (
        /* Kafka Topics List */
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          {/* DLQ Injection Experiment Banner */}
          <div style={{
            background: 'rgba(244, 63, 94, 0.1)',
            border: '1px solid #f43f5e',
            borderRadius: '12px',
            padding: '16px 20px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <AlertTriangle size={22} color="#fb7185" />
              <div>
                <span style={{ fontSize: '0.9rem', fontWeight: 700, color: '#fb7185', display: 'block' }}>
                  Dead Letter Queue (DLQ) Injection Simulator
                </span>
                <span style={{ fontSize: '0.75rem', color: '#cbd5e1' }}>
                  Emit corrupted JSON poison pill into Kafka to test dead-letter isolation without stopping partition consumers.
                </span>
              </div>
            </div>
            <button
              onClick={handleSendPoisonPill}
              disabled={poisonSending}
              className="btn-secondary"
              style={{ borderColor: '#f43f5e', color: '#fb7185', fontSize: '0.8rem' }}
            >
              <Send size={14} />
              <span>{poisonSending ? 'Emitting...' : 'Emit Poison Pill'}</span>
            </button>
          </div>

          {poisonResult && (
            <div style={{ background: 'rgba(16, 185, 129, 0.15)', border: '1px solid #10b981', color: '#34d399', padding: '12px 16px', borderRadius: '8px', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CheckCircle2 size={16} />
              <span>{poisonResult}</span>
            </div>
          )}

          {/* Topics Table */}
          <div style={{ background: '#0b1120', borderRadius: '12px', border: '1px solid #1e293b', overflow: 'hidden' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
              <thead>
                <tr style={{ background: '#080c16', borderBottom: '1px solid #1e293b', color: '#94a3b8', fontSize: '0.75rem', textTransform: 'uppercase' }}>
                  <th style={{ padding: '14px 18px' }}>Topic Name</th>
                  <th style={{ padding: '14px 18px' }}>Partitions</th>
                  <th style={{ padding: '14px 18px' }}>Subscribed Consumer Groups</th>
                  <th style={{ padding: '14px 18px' }}>Type</th>
                  <th style={{ padding: '14px 18px' }}>Description</th>
                </tr>
              </thead>
              <tbody>
                {KAFKA_TOPICS_METADATA.map(t => (
                  <tr key={t.name} style={{ borderBottom: '1px solid #1e293b' }}>
                    <td style={{ padding: '14px 18px', fontWeight: 600, color: t.isDlq ? '#fb7185' : '#38bdf8' }} className="font-mono">
                      {t.name}
                    </td>
                    <td style={{ padding: '14px 18px', color: '#f8fafc' }} className="font-mono">
                      {t.partitions}
                    </td>
                    <td style={{ padding: '14px 18px' }}>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                        {t.consumerGroups.length === 0 ? (
                          <span style={{ color: '#64748b' }}>None</span>
                        ) : (
                          t.consumerGroups.map((cg, i) => (
                            <span key={i} className="font-mono" style={{ background: '#1e293b', color: '#cbd5e1', padding: '2px 6px', borderRadius: '4px', fontSize: '0.7rem' }}>
                              {cg}
                            </span>
                          ))
                        )}
                      </div>
                    </td>
                    <td style={{ padding: '14px 18px' }}>
                      {t.isDlq ? (
                        <span className="badge badge-rose">Dead Letter Queue</span>
                      ) : (
                        <span className="badge badge-emerald">Domain Topic</span>
                      )}
                    </td>
                    <td style={{ padding: '14px 18px', color: '#94a3b8', fontSize: '0.8rem' }}>
                      {t.description}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
