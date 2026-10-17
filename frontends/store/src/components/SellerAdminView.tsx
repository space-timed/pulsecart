import React, { useState } from 'react';
import { Product } from '../types';
import { Activity, PlusCircle, RefreshCw, TrendingUp, CheckCircle, ShieldAlert } from 'lucide-react';

interface SellerAdminViewProps {
  products: Product[];
  onUpdateProductStock: (productId: string, newStock: number) => void;
  onUpdateProductPrice: (productId: string, newPrice: number) => void;
  onTriggerRebuild: () => Promise<void>;
}

export const SellerAdminView: React.FC<SellerAdminViewProps> = ({
  products,
  onUpdateProductStock,
  onUpdateProductPrice,
  onTriggerRebuild
}) => {
  const [rebuilding, setRebuilding] = useState(false);
  const [rebuildSuccess, setRebuildSuccess] = useState(false);

  const handleRebuild = async () => {
    setRebuilding(true);
    setRebuildSuccess(false);
    await onTriggerRebuild();
    setRebuilding(false);
    setRebuildSuccess(true);
    setTimeout(() => setRebuildSuccess(false), 3000);
  };

  return (
    <div className="card-surface" style={{ padding: '24px', margin: '24px auto', maxWidth: '1280px', backgroundColor: '#0f172a' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #1e293b', paddingBottom: '16px', marginBottom: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ width: '32px', height: '32px', borderRadius: '8px', background: '#3b82f6', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Activity size={18} color="#ffffff" />
          </div>
          <div>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: '#f8fafc' }}>
              Operations & Inventory Telemetry Console
            </h3>
            <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>
              Real-time stock controls, flash-sale pricing, and CQRS projection synchronization
            </span>
          </div>
        </div>

        <button
          onClick={handleRebuild}
          disabled={rebuilding}
          className="btn-secondary"
          style={{ fontSize: '0.85rem' }}
        >
          <RefreshCw size={15} className={rebuilding ? 'animate-spin' : ''} />
          <span>{rebuilding ? 'Rebuilding...' : 'Rebuild Search Projection'}</span>
        </button>
      </div>

      {rebuildSuccess && (
        <div style={{ padding: '10px 16px', borderRadius: '8px', background: 'rgba(16, 185, 129, 0.15)', border: '1px solid #10b981', color: '#34d399', fontSize: '0.85rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <CheckCircle size={16} />
          <span>Eventual consistency catch-up verified: Search read projection synchronized with latest catalog events.</span>
        </div>
      )}

      {/* Product Inventory & Price Controls Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '16px' }}>
        {products.map(p => (
          <div key={p.id} style={{ background: '#1e293b', padding: '16px', borderRadius: '10px', border: '1px solid #334155' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600 }}>{p.sku}</span>
                <h4 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#f8fafc' }}>{p.name}</h4>
              </div>
              <span className={p.inStock ? 'badge badge-emerald' : 'badge badge-rose'}>
                {p.inStock ? `${p.stockQuantity} in stock` : 'Out of Stock'}
              </span>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '10px', paddingTop: '10px', borderTop: '1px solid #334155' }}>
              {/* Stock controls */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Restock:</span>
                <button
                  onClick={() => onUpdateProductStock(p.id, p.stockQuantity + 10)}
                  className="btn-secondary"
                  style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                >
                  +10 Units
                </button>
                <button
                  onClick={() => onUpdateProductStock(p.id, p.stockQuantity + 50)}
                  className="btn-secondary"
                  style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                >
                  +50 Units
                </button>
              </div>

              {/* Flash sale trigger */}
              <button
                onClick={() => onUpdateProductPrice(p.id, Math.round(p.price * 0.8 * 100) / 100)}
                className="btn-secondary"
                style={{ padding: '4px 10px', fontSize: '0.75rem', borderColor: '#f59e0b', color: '#fbbf24' }}
                title="Drop price by 20% to test price change event propagation"
              >
                -20% Flash Sale
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
