import React, { useState } from 'react';
import { Cart, SagaStep, Order } from '../types';
import { apiClient } from '../api/client';
import { X, CheckCircle2, AlertTriangle, RefreshCw, ArrowRight, ShieldAlert, Zap, Truck } from 'lucide-react';

interface CheckoutSagaModalProps {
  isOpen: boolean;
  onClose: () => void;
  cart: Cart;
  onOrderCompleted: (order: Order) => void;
}

export const CheckoutSagaModal: React.FC<CheckoutSagaModalProps> = ({
  isOpen,
  onClose,
  cart,
  onOrderCompleted
}) => {
  const [shippingAddress, setShippingAddress] = useState('100 Silicon Way, Austin, TX 78701');
  const [customerName, setCustomerName] = useState('Alex Morgan');
  const [chaosDecline, setChaosDecline] = useState(false);
  const [isExecuting, setIsExecuting] = useState(false);
  const [executionFinished, setExecutionFinished] = useState(false);
  const [createdOrder, setCreatedOrder] = useState<Order | null>(null);

  const [steps, setSteps] = useState<SagaStep[]>([
    { name: '1. Order Service Validation', status: 'PENDING', message: 'Generate order state machine & save initial record' },
    { name: '2. Inventory Stock Reservation', status: 'PENDING', message: 'Atomic conditional SQL update preventing overselling' },
    { name: '3. Payment Gateway Authorization', status: 'PENDING', message: 'Payment adapter transaction (Stripe/PayPal simulated)' },
    { name: '4. Transactional Outbox Event', status: 'PENDING', message: 'Kafka dispatch to Fulfillment, Shipping, and Notifications' }
  ]);

  if (!isOpen) return null;

  const handleRunSaga = async () => {
    setIsExecuting(true);
    setExecutionFinished(false);

    // Helper to update a specific step
    const updateStep = (index: number, status: SagaStep['status'], message: string) => {
      setSteps(prev => {
        const next = [...prev];
        next[index] = { ...next[index], status, message, timestamp: new Date().toLocaleTimeString() };
        return next;
      });
    };

    // Reset steps
    setSteps([
      { name: '1. Order Service Validation', status: 'RUNNING', message: 'Validating payload and initializing Saga coordinator...' },
      { name: '2. Inventory Stock Reservation', status: 'PENDING', message: 'Awaiting order registration...' },
      { name: '3. Payment Gateway Authorization', status: 'PENDING', message: 'Awaiting stock reservation...' },
      { name: '4. Transactional Outbox Event', status: 'PENDING', message: 'Awaiting payment confirmation...' }
    ]);

    await new Promise(r => setTimeout(r, 600));
    updateStep(0, 'COMPLETED', 'Order registered: CREATED. Unique idempotency key verified.');

    // Step 2: Inventory
    updateStep(1, 'RUNNING', 'Executing atomic stock reservation on Inventory Service...');
    await new Promise(r => setTimeout(r, 700));
    updateStep(1, 'COMPLETED', 'Stock reserved atomically via conditional SQL update.');

    // Step 3: Payment
    updateStep(2, 'RUNNING', 'Routing authorization through Payment Provider Adapter...');
    await new Promise(r => setTimeout(r, 800));

    if (chaosDecline) {
      // Simulate failure & compensation!
      updateStep(2, 'FAILED', 'Payment simulation returned: DECLINED (Insufficient Funds / Chaos Flag).');

      await new Promise(r => setTimeout(r, 500));
      // Trigger compensation on step 2
      updateStep(1, 'COMPENSATED', 'COMPENSATING: Executing compensating inventory release. Reserved stock restored to pool.');

      updateStep(3, 'FAILED', 'Order status moved to CANCELLED. Outbox OrderCancelled event emitted.');

      const failedOrder: Order = {
        id: 'ORD-FAIL-' + Math.random().toString(36).substring(2, 7).toUpperCase(),
        customerId: cart.customerId,
        totalAmount: cart.total,
        status: 'CANCELLED',
        items: [...cart.items],
        createdAt: new Date().toISOString(),
        shippingAddress
      };
      setCreatedOrder(failedOrder);
      setIsExecuting(false);
      setExecutionFinished(true);
      return;
    }

    // Success Path
    updateStep(2, 'COMPLETED', 'Payment captured successfully: authorization approved.');

    // Step 4: Outbox & Kafka
    updateStep(3, 'RUNNING', 'Writing OrderConfirmed to Transactional Outbox and committing DB transaction...');
    await new Promise(r => setTimeout(r, 600));

    const trackingNum = 'FDX-' + Math.floor(100000000 + Math.random() * 900000000);
    updateStep(3, 'COMPLETED', `Event published to Kafka (pulsecart.order.events). Carrier Tracking: ${trackingNum}`);

    const res = await apiClient.executeCheckoutSaga(cart, shippingAddress, false);

    const successfulOrder: Order = {
      id: res.orderId,
      customerId: cart.customerId,
      totalAmount: cart.total,
      status: 'CONFIRMED',
      items: [...cart.items],
      createdAt: new Date().toISOString(),
      shippingAddress,
      trackingNumber: trackingNum,
      carrier: 'FEDEX'
    };

    setCreatedOrder(successfulOrder);
    setIsExecuting(false);
    setExecutionFinished(true);
    onOrderCompleted(successfulOrder);
  };

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 60, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '16px' }}>
      {/* Backdrop */}
      <div onClick={isExecuting ? undefined : onClose} style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0, 0, 0, 0.8)', backdropFilter: 'blur(6px)' }} />

      {/* Modal Surface */}
      <div style={{
        position: 'relative',
        width: '100%',
        maxWidth: '680px',
        backgroundColor: '#0f172a',
        borderRadius: '16px',
        border: '1px solid #334155',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.8)',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column'
      }}>
        {/* Header */}
        <div style={{ padding: '20px 24px', borderBottom: '1px solid #1e293b', display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: '#1e293b' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '36px', height: '36px', borderRadius: '8px', background: '#4f46e5', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Zap size={20} color="#ffffff" />
            </div>
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#f8fafc' }}>
                Distributed Saga Checkout Coordinator
              </h3>
              <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>
                Durable Multi-Service Transaction • Atomic Stock Check • Transactional Outbox
              </span>
            </div>
          </div>
          {!isExecuting && (
            <button onClick={onClose} style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer' }}>
              <X size={20} />
            </button>
          )}
        </div>

        {/* Content Body */}
        <div style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          {/* Shipping Details and Chaos Injection Toggle */}
          {!isExecuting && !executionFinished && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', color: '#94a3b8', marginBottom: '4px' }}>Customer Name</label>
                  <input
                    type="text"
                    value={customerName}
                    onChange={(e) => setCustomerName(e.target.value)}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', background: '#1e293b', border: '1px solid #334155', color: '#f8fafc', fontSize: '0.85rem' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', color: '#94a3b8', marginBottom: '4px' }}>Delivery Address</label>
                  <input
                    type="text"
                    value={shippingAddress}
                    onChange={(e) => setShippingAddress(e.target.value)}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', background: '#1e293b', border: '1px solid #334155', color: '#f8fafc', fontSize: '0.85rem' }}
                  />
                </div>
              </div>

              {/* Chaos Injection Banner */}
              <div style={{
                padding: '14px',
                borderRadius: '10px',
                backgroundColor: chaosDecline ? 'rgba(244, 63, 94, 0.12)' : 'rgba(30, 41, 59, 0.6)',
                border: '1px solid',
                borderColor: chaosDecline ? '#f43f5e' : '#334155',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                transition: 'all 0.2s ease'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <ShieldAlert size={20} color={chaosDecline ? '#fb7185' : '#94a3b8'} />
                  <div>
                    <span style={{ fontSize: '0.85rem', fontWeight: 600, color: chaosDecline ? '#fb7185' : '#f8fafc', display: 'block' }}>
                      Chaos Experiment: Inject Payment Decline
                    </span>
                    <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>
                      Tests Saga automated compensation: releases reserved inventory and cancels order.
                    </span>
                  </div>
                </div>
                <label style={{ position: 'relative', display: 'inline-block', width: '44px', height: '24px', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    checked={chaosDecline}
                    onChange={(e) => setChaosDecline(e.target.checked)}
                    style={{ opacity: 0, width: 0, height: 0 }}
                  />
                  <span style={{
                    position: 'absolute',
                    cursor: 'pointer',
                    inset: 0,
                    backgroundColor: chaosDecline ? '#f43f5e' : '#475569',
                    borderRadius: '24px',
                    transition: '0.2s'
                  }}>
                    <span style={{
                      position: 'absolute',
                      content: '',
                      height: '18px',
                      width: '18px',
                      left: chaosDecline ? '23px' : '3px',
                      bottom: '3px',
                      backgroundColor: 'white',
                      borderRadius: '50%',
                      transition: '0.2s'
                    }} />
                  </span>
                </label>
              </div>
            </div>
          )}

          {/* Saga Execution State Machine Timeline */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <span style={{ fontSize: '0.8rem', color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase' }}>
              Saga Execution Timeline
            </span>

            {steps.map((step, idx) => (
              <div
                key={step.name}
                style={{
                  padding: '12px 16px',
                  borderRadius: '10px',
                  backgroundColor: step.status === 'RUNNING' ? '#1e1b4b' :
                    step.status === 'COMPLETED' ? '#064e3b' :
                    step.status === 'COMPENSATED' ? '#78350f' :
                    step.status === 'FAILED' ? '#881337' : '#1e293b',
                  border: '1px solid',
                  borderColor: step.status === 'RUNNING' ? '#6366f1' :
                    step.status === 'COMPLETED' ? '#10b981' :
                    step.status === 'COMPENSATED' ? '#f59e0b' :
                    step.status === 'FAILED' ? '#f43f5e' : '#334155',
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '12px',
                  transition: 'all 0.3s ease'
                }}
              >
                <div style={{ marginTop: '2px' }}>
                  {step.status === 'RUNNING' && <RefreshCw size={18} color="#818cf8" className="animate-spin" />}
                  {step.status === 'COMPLETED' && <CheckCircle2 size={18} color="#34d399" />}
                  {step.status === 'COMPENSATED' && <RefreshCw size={18} color="#fbbf24" />}
                  {step.status === 'FAILED' && <AlertTriangle size={18} color="#fb7185" />}
                  {step.status === 'PENDING' && <div style={{ width: '18px', height: '18px', borderRadius: '50%', border: '2px solid #64748b' }} />}
                </div>

                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <span style={{ fontSize: '0.9rem', fontWeight: 600, color: '#f8fafc' }}>{step.name}</span>
                    <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#94a3b8' }}>{step.status}</span>
                  </div>
                  <p style={{ fontSize: '0.8rem', color: '#cbd5e1', marginTop: '2px' }}>{step.message}</p>
                </div>
              </div>
            ))}
          </div>

          {/* Success Banner */}
          {executionFinished && createdOrder && (
            <div style={{
              padding: '16px',
              borderRadius: '10px',
              backgroundColor: createdOrder.status === 'CONFIRMED' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(244, 63, 94, 0.15)',
              border: '1px solid',
              borderColor: createdOrder.status === 'CONFIRMED' ? '#10b981' : '#f43f5e',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <div>
                <span style={{ fontSize: '0.95rem', fontWeight: 700, color: createdOrder.status === 'CONFIRMED' ? '#34d399' : '#fb7185', display: 'block' }}>
                  {createdOrder.status === 'CONFIRMED' ? 'Order Confirmed & Orchestrated!' : 'Saga Compensation Completed!'}
                </span>
                <span style={{ fontSize: '0.8rem', color: '#cbd5e1' }}>
                  {createdOrder.status === 'CONFIRMED'
                    ? `Order ID: ${createdOrder.id} • Tracking: ${createdOrder.trackingNumber}`
                    : `Order ID: ${createdOrder.id} • Stock safely returned with 0 overselling leakage.`}
                </span>
              </div>
              <button
                onClick={onClose}
                className="btn-secondary"
                style={{ fontSize: '0.85rem' }}
              >
                Close
              </button>
            </div>
          )}
        </div>

        {/* Action Footer */}
        <div style={{ padding: '16px 24px', borderTop: '1px solid #1e293b', background: '#0a0f1d', display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
          {!executionFinished ? (
            <button
              onClick={handleRunSaga}
              disabled={isExecuting}
              className="btn-primary"
              style={{ padding: '10px 24px' }}
            >
              {isExecuting ? (
                <>
                  <RefreshCw size={18} className="animate-spin" />
                  <span>Executing Saga...</span>
                </>
              ) : (
                <>
                  <span>Commit Saga (${cart.total.toFixed(2)})</span>
                  <ArrowRight size={18} />
                </>
              )}
            </button>
          ) : (
            <button onClick={onClose} className="btn-primary" style={{ padding: '10px 24px' }}>
              Done
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
