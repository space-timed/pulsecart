import React, { useState } from 'react';
import { ServiceNode } from '../types';
import { SERVICES_REGISTRY } from '../data/metadata';
import { Server, Database, Radio, ArrowRight, X, ExternalLink, ShieldCheck, Activity } from 'lucide-react';

export const ArchitectureExplorer: React.FC = () => {
  const [selectedService, setSelectedService] = useState<ServiceNode | null>(null);
  const [filterTier, setFilterTier] = useState<string>('all');

  const tiers: { id: string; label: string; desc: string }[] = [
    { id: 'gateway', label: '1. Ingress & Edge', desc: 'Reactive WebFlux Spring Cloud Gateway' },
    { id: 'core', label: '2. Core Commerce Tier', desc: 'Customer, Catalog, Pricing, Inventory, Cart' },
    { id: 'checkout', label: '3. Checkout & Transactions', desc: 'Saga Coordinator, Payment, Promotions' },
    { id: 'async', label: '4. Asynchronous Commerce', desc: 'Fulfillment, Shipping, Notifications, Returns' },
    { id: 'cqrs', label: '5. CQRS & Analytics', desc: 'Search Projections & Stream Aggregations' }
  ];

  const getTierServices = (tierId: string) => {
    return SERVICES_REGISTRY.filter(s => s.tier === tierId);
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1440px', margin: '0 auto' }}>
      {/* Overview Banner */}
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
            <span className="badge badge-emerald">Interactive Topology</span>
            <span className="badge badge-indigo">15 Java Microservices</span>
            <span className="badge badge-amber">Database-per-Service</span>
          </div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc', letterSpacing: '-0.02em' }}>
            System Architecture Explorer
          </h2>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8', marginTop: '4px' }}>
            Explore service boundaries, communication protocols (REST vs Kafka KRaft), database ownership, and applied design patterns.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '20px', textAlign: 'right' }}>
          <div>
            <span style={{ fontSize: '1.4rem', fontWeight: 800, color: '#38bdf8', display: 'block' }}>15</span>
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Active Services</span>
          </div>
          <div>
            <span style={{ fontSize: '1.4rem', fontWeight: 800, color: '#34d399', display: 'block' }}>14</span>
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Kafka Topics</span>
          </div>
          <div>
            <span style={{ fontSize: '1.4rem', fontWeight: 800, color: '#a855f7', display: 'block' }}>18</span>
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Patterns</span>
          </div>
        </div>
      </div>

      {/* Hierarchical Tiers Layout */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
        {tiers.map(tier => {
          const servicesInTier = getTierServices(tier.id);
          return (
            <div key={tier.id} style={{ background: '#0b1120', padding: '20px', borderRadius: '14px', border: '1px solid #1e293b' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>{tier.label}</h3>
                  <span style={{ fontSize: '0.75rem', color: '#64748b' }}>{tier.desc}</span>
                </div>
                <span className="status-pill status-healthy">{servicesInTier.length} Services</span>
              </div>

              {/* Service Cards Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '14px' }}>
                {servicesInTier.map(service => (
                  <div
                    key={service.id}
                    className={`service-node ${selectedService?.id === service.id ? 'selected' : ''}`}
                    onClick={() => setSelectedService(service)}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Server size={16} color="#38bdf8" />
                        <span style={{ fontSize: '0.9rem', fontWeight: 700, color: '#f8fafc' }}>{service.name}</span>
                      </div>
                      <span className="status-pill status-healthy" style={{ fontSize: '0.65rem' }}>:{service.port}</span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: '#94a3b8', marginBottom: '8px' }}>
                      <Database size={13} color="#a855f7" />
                      <span className="font-mono" style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {service.database.split(':')[1]?.trim() || service.database}
                      </span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: '#94a3b8', marginBottom: '12px' }}>
                      <Radio size={13} color={service.protocol === 'Both' ? '#34d399' : '#38bdf8'} />
                      <span>Protocol: <strong>{service.protocol}</strong></span>
                    </div>

                    {/* Applied Patterns Badges */}
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                      {service.patterns.slice(0, 2).map((p, idx) => (
                        <span key={idx} style={{
                          background: '#1e293b',
                          color: '#cbd5e1',
                          padding: '2px 6px',
                          borderRadius: '4px',
                          fontSize: '0.65rem',
                          fontFamily: 'monospace'
                        }}>
                          {p}
                        </span>
                      ))}
                      {service.patterns.length > 2 && (
                        <span style={{ fontSize: '0.65rem', color: '#64748b', alignSelf: 'center' }}>
                          +{service.patterns.length - 2} more
                        </span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>

      {/* Slide-out Service Inspector Drawer */}
      {selectedService && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 60, display: 'flex', justifyContent: 'flex-end' }}>
          <div onClick={() => setSelectedService(null)} style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(4px)' }} />

          <div style={{
            position: 'relative',
            width: '100%',
            maxWidth: '520px',
            backgroundColor: '#0b1120',
            borderLeft: '1px solid #1e293b',
            display: 'flex',
            flexDirection: 'column',
            height: '100%',
            boxShadow: '-10px 0 30px rgba(0,0,0,0.8)',
            zIndex: 10
          }}>
            {/* Header */}
            <div style={{ padding: '20px 24px', borderBottom: '1px solid #1e293b', background: '#0f172a', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <span className="status-pill status-healthy" style={{ marginBottom: '6px' }}>PORT {selectedService.port}</span>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 800, color: '#f8fafc' }}>{selectedService.name}</h3>
                <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>ID: {selectedService.id}</span>
              </div>
              <button onClick={() => setSelectedService(null)} style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {/* Content Details */}
            <div style={{ flex: 1, overflowY: 'auto', padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
              {/* Database Ownership */}
              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', display: 'block', marginBottom: '6px' }}>
                  Database Boundary (No Cross-Service Access)
                </span>
                <div className="code-box">
                  <div style={{ color: '#38bdf8', fontWeight: 600 }}>{selectedService.database}</div>
                  <div style={{ color: '#94a3b8', marginTop: '4px', fontSize: '0.75rem' }}>{selectedService.schemaOverview}</div>
                </div>
              </div>

              {/* Applied Patterns */}
              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', display: 'block', marginBottom: '6px' }}>
                  Applied Architecture Patterns
                </span>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  {selectedService.patterns.map((p, idx) => (
                    <div key={idx} style={{ padding: '8px 12px', background: '#1e293b', borderRadius: '6px', fontSize: '0.8rem', color: '#e2e8f0', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <ShieldCheck size={14} color="#34d399" />
                      <span>{p}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Event Contracts */}
              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', display: 'block', marginBottom: '6px' }}>
                  Published Kafka Topics
                </span>
                {selectedService.publishedEvents.length === 0 ? (
                  <span style={{ fontSize: '0.8rem', color: '#64748b' }}>None (Direct synchronous responder)</span>
                ) : (
                  selectedService.publishedEvents.map((e, idx) => (
                    <div key={idx} className="code-box" style={{ marginBottom: '6px', color: '#34d399' }}>
                      {e}
                    </div>
                  ))
                )}
              </div>

              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', display: 'block', marginBottom: '6px' }}>
                  Consumed Kafka Topics
                </span>
                {selectedService.consumedEvents.length === 0 ? (
                  <span style={{ fontSize: '0.8rem', color: '#64748b' }}>None</span>
                ) : (
                  selectedService.consumedEvents.map((e, idx) => (
                    <div key={idx} className="code-box" style={{ marginBottom: '6px', color: '#fbbf24' }}>
                      {e}
                    </div>
                  ))
                )}
              </div>

              {/* REST Endpoints */}
              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', display: 'block', marginBottom: '6px' }}>
                  REST Endpoints
                </span>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  {selectedService.endpoints.map((ep, idx) => (
                    <div key={idx} className="font-mono" style={{ fontSize: '0.75rem', color: '#94a3b8', background: '#080c16', padding: '6px 10px', borderRadius: '4px' }}>
                      {ep}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
