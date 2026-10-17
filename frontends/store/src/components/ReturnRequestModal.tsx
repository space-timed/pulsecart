import React, { useState } from 'react';
import { Order, ReturnRecord } from '../types';
import { apiClient } from '../api/client';
import { X, RotateCcw, AlertCircle, CheckCircle2, ShieldCheck } from 'lucide-react';

interface ReturnRequestModalProps {
  order: Order | null;
  isOpen: boolean;
  onClose: () => void;
  onReturnSubmitted: (record: ReturnRecord) => void;
}

export const ReturnRequestModal: React.FC<ReturnRequestModalProps> = ({
  order,
  isOpen,
  onClose,
  onReturnSubmitted
}) => {
  const [reason, setReason] = useState('Defective Item / Performance Issue');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<ReturnRecord | null>(null);

  if (!isOpen || !order) return null;

  // 30-day return eligibility check
  const orderDate = new Date(order.createdAt);
  const daysElapsed = Math.floor((Date.now() - orderDate.getTime()) / (1000 * 60 * 60 * 24));
  const isEligible = daysElapsed <= 30;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isEligible) return;

    setSubmitting(true);
    const rma = await apiClient.submitReturnRequest(order.id, order.customerId, reason, order.items);
    setResult(rma);
    setSubmitting(false);
    onReturnSubmitted(rma);
  };

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 80, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '16px' }}>
      <div onClick={onClose} style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.8)', backdropFilter: 'blur(5px)' }} />

      <div style={{
        position: 'relative',
        width: '100%',
        maxWidth: '540px',
        backgroundColor: '#0f172a',
        borderRadius: '16px',
        border: '1px solid #334155',
        overflow: 'hidden',
        boxShadow: '0 20px 40px rgba(0,0,0,0.8)'
      }}>
        {/* Header */}
        <div style={{ padding: '20px 24px', borderBottom: '1px solid #1e293b', display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: '#1e293b' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <RotateCcw size={20} color="#fb7185" />
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#f8fafc' }}>
              Initiate RMA Return (Order {order.id})
            </h3>
          </div>
          <button onClick={onClose} style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>

        {/* Content */}
        <div style={{ padding: '24px' }}>
          {result ? (
            <div style={{ textAlign: 'center', padding: '20px 0' }}>
              <CheckCircle2 size={48} color="#34d399" style={{ margin: '0 auto 12px' }} />
              <h4 style={{ fontSize: '1.2rem', fontWeight: 700, color: '#f8fafc', marginBottom: '8px' }}>
                RMA Authorization Issued!
              </h4>
              <p style={{ fontSize: '0.9rem', color: '#94a3b8', marginBottom: '16px' }}>
                Your Return Merchandise Authorization code is:
              </p>
              <div style={{
                background: '#1e293b',
                padding: '12px',
                borderRadius: '8px',
                fontFamily: 'monospace',
                fontSize: '1.3rem',
                fontWeight: 700,
                color: '#38bdf8',
                marginBottom: '20px'
              }}>
                {result.id}
              </div>
              <p style={{ fontSize: '0.8rem', color: '#64748b', marginBottom: '24px' }}>
                Estimated Refund: ${result.refundAmount.toFixed(2)} • Return Approved under 30-Day Policy.
              </p>
              <button onClick={onClose} className="btn-primary" style={{ width: '100%' }}>Done</button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {/* Policy Check Banner */}
              <div style={{
                padding: '12px 16px',
                borderRadius: '8px',
                backgroundColor: isEligible ? 'rgba(16, 185, 129, 0.1)' : 'rgba(244, 63, 94, 0.1)',
                border: '1px solid',
                borderColor: isEligible ? '#10b981' : '#f43f5e',
                display: 'flex',
                alignItems: 'center',
                gap: '10px'
              }}>
                {isEligible ? <ShieldCheck size={18} color="#34d399" /> : <AlertCircle size={18} color="#fb7185" />}
                <div style={{ fontSize: '0.8rem' }}>
                  <span style={{ fontWeight: 600, color: isEligible ? '#34d399' : '#fb7185', display: 'block' }}>
                    {isEligible ? 'Eligible for Return' : 'Ineligible: Exceeded 30-Day Return Policy'}
                  </span>
                  <span style={{ color: '#94a3b8' }}>
                    Placed {daysElapsed} days ago. Policy requires returns within 30 days of delivery.
                  </span>
                </div>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: '#cbd5e1', marginBottom: '6px', fontWeight: 500 }}>
                  Select Return Reason
                </label>
                <select
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '10px 14px',
                    borderRadius: '8px',
                    backgroundColor: '#1e293b',
                    border: '1px solid #334155',
                    color: '#f8fafc',
                    fontSize: '0.9rem'
                  }}
                >
                  <option value="Defective Item / Performance Issue">Defective Item / Performance Issue</option>
                  <option value="Wrong Item or Color Shipped">Wrong Item or Color Shipped</option>
                  <option value="Unsatisfied with Quality">Unsatisfied with Quality</option>
                  <option value="Accidental Order">Accidental Order</option>
                </select>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '12px' }}>
                <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
                <button type="submit" disabled={!isEligible || submitting} className="btn-primary">
                  {submitting ? 'Issuing RMA...' : 'Submit RMA Request'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
