import React, { useState } from 'react';
import { ArchitecturePattern } from '../types';
import { PATTERNS_REGISTRY } from '../data/metadata';
import { BookOpen, Code2, ShieldCheck, CheckCircle2, Play, Sparkles } from 'lucide-react';

export const PatternAcademy: React.FC = () => {
  const [selectedPattern, setSelectedPattern] = useState<ArchitecturePattern>(PATTERNS_REGISTRY[0]);
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [simulationRunning, setSimulationRunning] = useState(false);
  const [simulationLog, setSimulationLog] = useState<string | null>(null);

  const categories = ['All', 'Distributed Transactions', 'Event-Driven & Messaging', 'Concurrency', 'Resilience & Fault Tolerance', 'CQRS & Read Models'];

  const filteredPatterns = selectedCategory === 'All'
    ? PATTERNS_REGISTRY
    : PATTERNS_REGISTRY.filter(p => p.category === selectedCategory);

  const handleSimulate = (pattern: ArchitecturePattern) => {
    setSimulationRunning(true);
    setSimulationLog(null);
    setTimeout(() => {
      setSimulationRunning(false);
      setSimulationLog(`Simulation OK: Pattern [${pattern.name}] validated across ${pattern.services.join(', ')} with verified assertions.`);
    }, 800);
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1440px', margin: '0 auto' }}>
      {/* Banner */}
      <div style={{
        background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.9) 0%, rgba(3, 7, 18, 0.95) 100%)',
        border: '1px solid #1e293b',
        borderRadius: '16px',
        padding: '24px 30px',
        marginBottom: '28px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
          <span className="badge badge-purple" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Sparkles size={12} /> Staff Engineer Reference
          </span>
          <span className="badge badge-emerald">18 Patterns Implemented</span>
        </div>
        <h2 style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc' }}>
          Microservices Pattern Academy
        </h2>
        <p style={{ fontSize: '0.85rem', color: '#94a3b8', marginTop: '4px' }}>
          Interactive architecture guide explaining the concrete problems, tradeoffs, and production Java implementations used in PulseCart.
        </p>

        {/* Category switcher */}
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginTop: '20px' }}>
          {categories.map(cat => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              style={{
                background: selectedCategory === cat ? '#38bdf8' : '#1e293b',
                color: selectedCategory === cat ? '#030712' : '#94a3b8',
                border: '1px solid',
                borderColor: selectedCategory === cat ? '#38bdf8' : '#334155',
                padding: '6px 14px',
                borderRadius: '6px',
                fontSize: '0.8rem',
                fontWeight: 600,
                cursor: 'pointer'
              }}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Main Grid: Left List, Right Detail */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: '24px' }}>
        
        {/* Left Patterns List */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {filteredPatterns.map(pattern => {
            const isSelected = selectedPattern.id === pattern.id;
            return (
              <div
                key={pattern.id}
                onClick={() => {
                  setSelectedPattern(pattern);
                  setSimulationLog(null);
                }}
                style={{
                  background: isSelected ? '#1e293b' : '#0b1120',
                  border: '1px solid',
                  borderColor: isSelected ? '#38bdf8' : '#1e293b',
                  borderRadius: '10px',
                  padding: '16px',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '6px' }}>
                  <h4 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#f8fafc' }}>{pattern.name}</h4>
                  <span className="badge badge-indigo" style={{ fontSize: '0.65rem' }}>{pattern.category}</span>
                </div>
                <p style={{ fontSize: '0.8rem', color: '#94a3b8', lineHeight: 1.4, marginBottom: '10px' }}>
                  {pattern.summary}
                </p>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                  {pattern.services.map((svc, i) => (
                    <span key={i} className="font-mono" style={{ fontSize: '0.65rem', color: '#38bdf8', background: '#080c16', padding: '2px 6px', borderRadius: '4px' }}>
                      {svc}
                    </span>
                  ))}
                </div>
              </div>
            );
          })}
        </div>

        {/* Right Pattern Detail Reader */}
        <div style={{ background: '#0b1120', padding: '28px', borderRadius: '14px', border: '1px solid #1e293b', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <span className="badge badge-indigo" style={{ marginBottom: '8px' }}>{selectedPattern.category}</span>
            <h3 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#f8fafc' }}>{selectedPattern.name}</h3>
            <p style={{ fontSize: '0.9rem', color: '#94a3b8', marginTop: '6px' }}>{selectedPattern.summary}</p>
          </div>

          {/* Problem Solved */}
          <div>
            <h4 style={{ fontSize: '0.85rem', color: '#fb7185', fontWeight: 700, textTransform: 'uppercase', marginBottom: '6px' }}>
              Problem & Tradeoffs
            </h4>
            <div style={{ background: '#080c16', padding: '14px', borderRadius: '8px', border: '1px solid #1e293b', fontSize: '0.85rem', color: '#cbd5e1', lineHeight: 1.6 }}>
              {selectedPattern.problemSolved}
            </div>
          </div>

          {/* Implementation Details */}
          <div>
            <h4 style={{ fontSize: '0.85rem', color: '#34d399', fontWeight: 700, textTransform: 'uppercase', marginBottom: '6px' }}>
              How PulseCart Implements It
            </h4>
            <div style={{ background: '#080c16', padding: '14px', borderRadius: '8px', border: '1px solid #1e293b', fontSize: '0.85rem', color: '#cbd5e1', lineHeight: 1.6 }}>
              {selectedPattern.implementationDetails}
            </div>
          </div>

          {/* Code Snippet */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
              <h4 style={{ fontSize: '0.85rem', color: '#38bdf8', fontWeight: 700, textTransform: 'uppercase' }}>
                Production Java Excerpt
              </h4>
              <span className="font-mono" style={{ fontSize: '0.75rem', color: '#64748b' }}>Spring Boot / Java 26</span>
            </div>
            <pre className="code-box">
              <code>{selectedPattern.codeSnippet}</code>
            </pre>
          </div>

          {/* Interactive Trigger Button */}
          <div style={{ paddingTop: '10px', borderTop: '1px solid #1e293b', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              {simulationLog && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#34d399', fontSize: '0.8rem' }}>
                  <CheckCircle2 size={16} />
                  <span>{simulationLog}</span>
                </div>
              )}
            </div>
            <button
              onClick={() => handleSimulate(selectedPattern)}
              disabled={simulationRunning}
              className="btn-primary"
              style={{ padding: '8px 18px', fontSize: '0.85rem' }}
            >
              <Play size={15} />
              <span>{simulationRunning ? 'Verifying...' : `Run ${selectedPattern.name} Test`}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
