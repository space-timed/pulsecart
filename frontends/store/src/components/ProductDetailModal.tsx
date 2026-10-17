import React, { useState } from 'react';
import { Product } from '../types';
import { X, ShoppingCart, ShieldCheck, Zap, Package, RefreshCw } from 'lucide-react';

interface ProductDetailModalProps {
  product: Product | null;
  isOpen: boolean;
  onClose: () => void;
  onAddToCart: (product: Product, quantity: number) => void;
}

export const ProductDetailModal: React.FC<ProductDetailModalProps> = ({
  product,
  isOpen,
  onClose,
  onAddToCart
}) => {
  const [qty, setQty] = useState(1);

  if (!isOpen || !product) return null;

  const handleAdd = () => {
    onAddToCart(product, qty);
    onClose();
  };

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 65, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '16px' }}>
      <div onClick={onClose} style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.8)', backdropFilter: 'blur(5px)' }} />

      <div style={{
        position: 'relative',
        width: '100%',
        maxWidth: '720px',
        backgroundColor: '#0f172a',
        borderRadius: '16px',
        border: '1px solid #334155',
        overflow: 'hidden',
        boxShadow: '0 25px 50px rgba(0,0,0,0.8)',
        display: 'grid',
        gridTemplateColumns: '1fr 1fr'
      }}>
        {/* Left: Image preview */}
        <div style={{ position: 'relative', backgroundColor: '#020617', height: '100%', minHeight: '340px' }}>
          <img
            src={product.imageUrl}
            alt={product.name}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
          <div style={{ position: 'absolute', top: '16px', left: '16px' }}>
            <span className="badge badge-indigo">{product.categoryName}</span>
          </div>
        </div>

        {/* Right: Details & Action */}
        <div style={{ padding: '24px', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
            <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600 }}>SKU: {product.sku}</span>
            <button onClick={onClose} style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer' }}>
              <X size={20} />
            </button>
          </div>

          <h3 style={{ fontSize: '1.25rem', fontWeight: 800, color: '#f8fafc', marginBottom: '8px', lineHeight: 1.25 }}>
            {product.name}
          </h3>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
            <span style={{ fontSize: '1.5rem', fontWeight: 800, color: '#38bdf8' }}>
              ${product.price.toFixed(2)}
            </span>
            {product.inStock ? (
              <span className="badge badge-emerald">{product.stockQuantity} in stock</span>
            ) : (
              <span className="badge badge-rose">Out of stock</span>
            )}
          </div>

          <p style={{ fontSize: '0.85rem', color: '#94a3b8', marginBottom: '20px', lineHeight: 1.5 }}>
            {product.description}
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '24px', fontSize: '0.8rem', color: '#cbd5e1' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ShieldCheck size={16} color="#34d399" />
              <span>Protected against flash-sale concurrency race conditions</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Package size={16} color="#818cf8" />
              <span>Fulfilled via PulseCart WH-EAST-1 / WH-WEST-1 Hubs</span>
            </div>
          </div>

          <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ display: 'flex', alignItems: 'center', background: '#1e293b', border: '1px solid #334155', borderRadius: '8px', padding: '6px 12px' }}>
              <span style={{ fontSize: '0.8rem', color: '#94a3b8', marginRight: '8px' }}>Qty:</span>
              <select
                value={qty}
                onChange={(e) => setQty(Number(e.target.value))}
                style={{ background: 'transparent', border: 'none', color: '#f8fafc', fontWeight: 700, fontSize: '0.9rem', outline: 'none', cursor: 'pointer' }}
              >
                {[1, 2, 3, 4, 5].map(n => (
                  <option key={n} value={n} style={{ background: '#0f172a' }}>{n}</option>
                ))}
              </select>
            </div>

            <button
              onClick={handleAdd}
              disabled={!product.inStock}
              className="btn-primary"
              style={{ flex: 1, padding: '12px 20px' }}
            >
              <ShoppingCart size={18} />
              <span>Add to Cart</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
