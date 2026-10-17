import React, { useState } from 'react';
import { Order, ShipmentTracking } from '../types';
import { apiClient } from '../api/client';
import { Package, Truck, ArrowLeft, RotateCcw, Clock, MapPin, CheckCircle } from 'lucide-react';

interface OrderHistoryViewProps {
  orders: Order[];
  onBackToStore: () => void;
  onInitiateReturn: (order: Order) => void;
}

export const OrderHistoryView: React.FC<OrderHistoryViewProps> = ({
  orders,
  onBackToStore,
  onInitiateReturn
}) => {
  const [selectedTracking, setSelectedTracking] = useState<ShipmentTracking | null>(null);
  const [loadingTracking, setLoadingTracking] = useState(false);

  const handleTrack = async (order: Order) => {
    if (!order.trackingNumber) return;
    setLoadingTracking(true);
    const tracking = await apiClient.fetchTracking(order.trackingNumber);
    setSelectedTracking(tracking);
    setLoadingTracking(false);
  };

  return (
    <div style={{ maxWidth: '1000px', margin: '30px auto', padding: '0 20px' }}>
      {/* Top Bar */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px' }}>
        <button onClick={onBackToStore} className="btn-secondary" style={{ fontSize: '0.85rem' }}>
          <ArrowLeft size={16} />
          <span>Back to Catalog</span>
        </button>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#f8fafc' }}>
          My Orders & Shipment Tracking
        </h2>
        <span className="badge badge-indigo">{orders.length} Total Orders</span>
      </div>

      {/* Orders List */}
      {orders.length === 0 ? (
        <div className="card-surface" style={{ padding: '60px', textAlign: 'center', color: '#64748b' }}>
          <Package size={48} style={{ margin: '0 auto 16px', opacity: 0.5 }} />
          <h3 style={{ fontSize: '1.2rem', color: '#f8fafc', marginBottom: '8px' }}>No orders placed yet</h3>
          <p style={{ fontSize: '0.9rem', marginBottom: '20px' }}>Explore the catalog and launch a Saga checkout to test the system.</p>
          <button onClick={onBackToStore} className="btn-primary">Browse Catalog</button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {orders.map(order => (
            <div key={order.id} className="card-surface" style={{ padding: '24px' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #1e293b', paddingBottom: '16px', marginBottom: '16px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span style={{ fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>Order {order.id}</span>
                    <span className={order.status === 'CONFIRMED' ? 'badge badge-emerald' : order.status === 'CANCELLED' ? 'badge badge-rose' : 'badge badge-indigo'}>
                      {order.status}
                    </span>
                  </div>
                  <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>
                    Placed on {new Date(order.createdAt).toLocaleString()} • Ships to: {order.shippingAddress}
                  </span>
                </div>

                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block' }}>Total Amount</span>
                  <span style={{ fontSize: '1.25rem', fontWeight: 800, color: '#38bdf8' }}>${order.totalAmount.toFixed(2)}</span>
                </div>
              </div>

              {/* Order Items */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '16px' }}>
                {order.items.map((item, idx) => (
                  <div key={idx} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                    <span style={{ color: '#cbd5e1' }}>
                      {item.quantity}x {item.productName}
                    </span>
                    <span style={{ color: '#94a3b8' }}>${(item.price * item.quantity).toFixed(2)}</span>
                  </div>
                ))}
              </div>

              {/* Actions Footer */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: '16px', borderTop: '1px solid #1e293b' }}>
                {order.trackingNumber ? (
                  <button
                    onClick={() => handleTrack(order)}
                    className="btn-secondary"
                    style={{ fontSize: '0.85rem' }}
                  >
                    <Truck size={16} />
                    <span>Track {order.carrier} ({order.trackingNumber})</span>
                  </button>
                ) : (
                  <span style={{ fontSize: '0.8rem', color: '#64748b' }}>No carrier assigned yet</span>
                )}

                {order.status === 'CONFIRMED' && (
                  <button
                    onClick={() => onInitiateReturn(order)}
                    className="btn-secondary"
                    style={{ fontSize: '0.85rem', borderColor: '#e11d48', color: '#fb7185' }}
                  >
                    <RotateCcw size={15} />
                    <span>Request RMA Return</span>
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Shipment Tracking Modal */}
      {selectedTracking && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 70, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '16px' }}>
          <div onClick={() => setSelectedTracking(null)} style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.8)', backdropFilter: 'blur(4px)' }} />
          <div style={{ position: 'relative', width: '100%', maxWidth: '580px', backgroundColor: '#0f172a', borderRadius: '16px', border: '1px solid #334155', padding: '24px', zIndex: 10 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Truck size={22} color="#38bdf8" />
                <div>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: '#f8fafc' }}>
                    {selectedTracking.carrier} Tracking: {selectedTracking.trackingNumber}
                  </h3>
                  <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Estimated: {selectedTracking.estimatedDelivery}</span>
                </div>
              </div>
              <button onClick={() => setSelectedTracking(null)} style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer' }}>
                ✕
              </button>
            </div>

            {/* Milestones */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginBottom: '20px' }}>
              {selectedTracking.history.map((h, i) => (
                <div key={i} style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
                  <div style={{ marginTop: '2px' }}>
                    <CheckCircle size={16} color="#34d399" />
                  </div>
                  <div>
                    <span style={{ fontSize: '0.85rem', fontWeight: 600, color: '#f8fafc', display: 'block' }}>{h.event}</span>
                    <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>{h.location} • {new Date(h.timestamp).toLocaleString()}</span>
                  </div>
                </div>
              ))}
            </div>

            <button onClick={() => setSelectedTracking(null)} className="btn-primary" style={{ width: '100%' }}>
              Close Tracker
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
