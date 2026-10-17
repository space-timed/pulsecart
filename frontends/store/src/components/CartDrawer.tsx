import React, { useState } from 'react';
import { Cart, PromotionCoupon } from '../types';
import { AVAILABLE_COUPONS } from '../api/client';
import { X, Trash2, Plus, Minus, Tag, ArrowRight, ShieldCheck } from 'lucide-react';

interface CartDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  cart: Cart;
  onUpdateQuantity: (productId: string, delta: number) => void;
  onRemoveItem: (productId: string) => void;
  onApplyCoupon: (coupon: PromotionCoupon | null) => void;
  onStartCheckout: () => void;
}

export const CartDrawer: React.FC<CartDrawerProps> = ({
  isOpen,
  onClose,
  cart,
  onUpdateQuantity,
  onRemoveItem,
  onApplyCoupon,
  onStartCheckout
}) => {
  const [couponCode, setCouponCode] = useState('');
  const [couponError, setCouponError] = useState('');
  const [couponSuccess, setCouponSuccess] = useState('');

  if (!isOpen) return null;

  const handleApplyCoupon = (e: React.FormEvent) => {
    e.preventDefault();
    setCouponError('');
    setCouponSuccess('');

    const matched = AVAILABLE_COUPONS.find(c => c.code.toUpperCase() === couponCode.trim().toUpperCase());
    if (!matched) {
      setCouponError('Invalid coupon code. Try FLASH50 or WELCOME10.');
      return;
    }

    if (cart.subtotal < matched.minimumAmount) {
      setCouponError(`Requires minimum subtotal of $${matched.minimumAmount}.00`);
      return;
    }

    onApplyCoupon(matched);
    setCouponSuccess(`Applied: ${matched.description}`);
  };

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 50, display: 'flex', justifyContent: 'flex-end' }}>
      {/* Backdrop */}
      <div
        onClick={onClose}
        style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0, 0, 0, 0.7)', backdropFilter: 'blur(4px)' }}
      />

      {/* Slide-over Container */}
      <div style={{
        position: 'relative',
        width: '100%',
        maxWidth: '460px',
        backgroundColor: '#0f172a',
        borderLeft: '1px solid #334155',
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        boxShadow: '-10px 0 30px rgba(0, 0, 0, 0.6)'
      }}>
        {/* Header */}
        <div style={{ padding: '20px 24px', borderBottom: '1px solid #1e293b', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#f8fafc' }}>Shopping Cart</h2>
            <span style={{ fontSize: '0.8rem', color: '#94a3b8' }}>{cart.items.length} unique items selected</span>
          </div>
          <button
            onClick={onClose}
            style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: '4px' }}
          >
            <X size={20} />
          </button>
        </div>

        {/* Item List */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '20px 24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {cart.items.length === 0 ? (
            <div style={{ textAlign: 'center', margin: 'auto 0', color: '#64748b' }}>
              <p style={{ fontSize: '1.1rem', marginBottom: '8px' }}>Your cart is empty.</p>
              <p style={{ fontSize: '0.85rem' }}>Select products from the catalog to begin.</p>
            </div>
          ) : (
            cart.items.map(item => (
              <div
                key={item.productId}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '14px',
                  padding: '12px',
                  backgroundColor: '#1e293b',
                  borderRadius: '10px',
                  border: '1px solid #334155'
                }}
              >
                {item.imageUrl && (
                  <img
                    src={item.imageUrl}
                    alt={item.productName}
                    style={{ width: '56px', height: '56px', borderRadius: '8px', objectFit: 'cover' }}
                  />
                )}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <h4 style={{ fontSize: '0.9rem', fontWeight: 600, color: '#f8fafc', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {item.productName}
                  </h4>
                  <span style={{ fontSize: '0.85rem', color: '#38bdf8', fontWeight: 700 }}>
                    ${(item.price * item.quantity).toFixed(2)}
                  </span>
                  <span style={{ fontSize: '0.75rem', color: '#64748b', marginLeft: '6px' }}>
                    (${item.price.toFixed(2)} each)
                  </span>
                </div>

                {/* Quantity Controls */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', background: '#0f172a', padding: '4px', borderRadius: '6px', border: '1px solid #334155' }}>
                  <button
                    onClick={() => onUpdateQuantity(item.productId, -1)}
                    style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: '2px' }}
                  >
                    <Minus size={14} />
                  </button>
                  <span style={{ fontSize: '0.85rem', fontWeight: 600, minWidth: '18px', textAlign: 'center' }}>
                    {item.quantity}
                  </span>
                  <button
                    onClick={() => onUpdateQuantity(item.productId, 1)}
                    style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: '2px' }}
                  >
                    <Plus size={14} />
                  </button>
                </div>

                {/* Remove */}
                <button
                  onClick={() => onRemoveItem(item.productId)}
                  style={{ background: 'transparent', border: 'none', color: '#f43f5e', cursor: 'pointer', padding: '4px' }}
                >
                  <Trash2 size={16} />
                </button>
              </div>
            ))
          )}
        </div>

        {/* Coupon Code Section */}
        {cart.items.length > 0 && (
          <div style={{ padding: '16px 24px', borderTop: '1px solid #1e293b', backgroundColor: '#131d31' }}>
            <form onSubmit={handleApplyCoupon} style={{ display: 'flex', gap: '8px' }}>
              <div style={{ position: 'relative', flex: 1 }}>
                <Tag size={16} color="#64748b" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
                <input
                  type="text"
                  placeholder="Promo Code (e.g. FLASH50)"
                  value={couponCode}
                  onChange={(e) => setCouponCode(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '8px 12px 8px 36px',
                    borderRadius: '6px',
                    backgroundColor: '#1e293b',
                    border: '1px solid #334155',
                    color: '#f8fafc',
                    fontSize: '0.85rem',
                    textTransform: 'uppercase'
                  }}
                />
              </div>
              <button type="submit" className="btn-secondary" style={{ padding: '8px 14px', fontSize: '0.8rem' }}>
                Apply
              </button>
            </form>
            {couponError && <p style={{ color: '#fb7185', fontSize: '0.75rem', marginTop: '6px' }}>{couponError}</p>}
            {couponSuccess && <p style={{ color: '#34d399', fontSize: '0.75rem', marginTop: '6px' }}>{couponSuccess}</p>}
          </div>
        )}

        {/* Footer Subtotals & Checkout CTA */}
        {cart.items.length > 0 && (
          <div style={{ padding: '20px 24px', borderTop: '1px solid #1e293b', backgroundColor: '#0f172a' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '0.9rem', color: '#94a3b8' }}>
              <span>Subtotal</span>
              <span>${cart.subtotal.toFixed(2)}</span>
            </div>
            {cart.discount > 0 && (
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '0.9rem', color: '#34d399' }}>
                <span>Discount ({cart.appliedCoupon})</span>
                <span>-${cart.discount.toFixed(2)}</span>
              </div>
            )}
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px', fontSize: '1.2rem', fontWeight: 800, color: '#f8fafc' }}>
              <span>Total</span>
              <span style={{ color: '#38bdf8' }}>${cart.total.toFixed(2)}</span>
            </div>

            <button
              onClick={onStartCheckout}
              className="btn-primary"
              style={{ width: '100%', padding: '12px 20px', fontSize: '1rem' }}
            >
              <span>Launch Saga Checkout</span>
              <ArrowRight size={18} />
            </button>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', marginTop: '12px', fontSize: '0.75rem', color: '#64748b' }}>
              <ShieldCheck size={14} color="#34d399" />
              <span>Orchestrated by Order Saga Coordinator</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
