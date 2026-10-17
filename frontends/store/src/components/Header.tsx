import React from 'react';
import { ShoppingBag, Search, Package, ShieldCheck, Activity, SlidersHorizontal } from 'lucide-react';

interface HeaderProps {
  cartItemCount: number;
  onOpenCart: () => void;
  onOpenOrders: () => void;
  onToggleAdmin: () => void;
  isAdminOpen: boolean;
  searchQuery: string;
  onSearchChange: (q: string) => void;
  activeCategory: string;
  onSelectCategory: (cat: string) => void;
}

export const Header: React.FC<HeaderProps> = ({
  cartItemCount,
  onOpenCart,
  onOpenOrders,
  onToggleAdmin,
  isAdminOpen,
  searchQuery,
  onSearchChange,
  activeCategory,
  onSelectCategory
}) => {
  const categories = ['All', 'Electronics', 'Audio', 'Office'];

  return (
    <header className="sticky top-0 z-40 glass-header">
      <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '16px 24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '20px' }}>
          
          {/* Brand Logo */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }} onClick={() => onSelectCategory('All')}>
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              background: 'linear-gradient(135deg, #4f46e5 0%, #ec4899 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 0 15px rgba(99, 102, 241, 0.5)'
            }}>
              <Activity size={22} color="#ffffff" />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontSize: '1.25rem', fontWeight: 800, letterSpacing: '-0.02em', color: '#f8fafc' }}>
                  Pulse<span style={{ color: '#818cf8' }}>Cart</span>
                </span>
                <span className="badge badge-emerald" style={{ fontSize: '0.65rem' }}>15 Services Active</span>
              </div>
              <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Distributed Commerce & Architecture Lab</span>
            </div>
          </div>

          {/* Search Bar */}
          <div style={{ flex: 1, maxWidth: '500px', position: 'relative' }}>
            <Search size={18} color="#94a3b8" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
              placeholder="Search catalog by SKU, name, tags (e.g. keyboard, anc)..."
              style={{
                width: '100%',
                padding: '10px 16px 10px 42px',
                borderRadius: '8px',
                background: '#1e293b',
                border: '1px solid #334155',
                color: '#f8fafc',
                fontSize: '0.9rem',
                outline: 'none'
              }}
            />
          </div>

          {/* Nav Actions */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <button
              onClick={onOpenOrders}
              className="btn-secondary"
              style={{ fontSize: '0.85rem' }}
              title="View your orders and track live shipments"
            >
              <Package size={17} />
              <span>Orders</span>
            </button>

            <button
              onClick={onToggleAdmin}
              className="btn-secondary"
              style={{
                fontSize: '0.85rem',
                borderColor: isAdminOpen ? '#818cf8' : '#334155',
                background: isAdminOpen ? '#312e81' : '#1f2937'
              }}
              title="Toggle Seller Operations and Telemetry"
            >
              <SlidersHorizontal size={17} />
              <span>Ops Console</span>
            </button>

            <button
              onClick={onOpenCart}
              className="btn-primary"
              style={{ position: 'relative', padding: '10px 16px' }}
            >
              <ShoppingBag size={18} />
              <span>Cart</span>
              {cartItemCount > 0 && (
                <span style={{
                  background: '#f43f5e',
                  color: '#ffffff',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  padding: '2px 7px',
                  borderRadius: '9999px',
                  marginLeft: '4px'
                }}>
                  {cartItemCount}
                </span>
              )}
            </button>
          </div>
        </div>

        {/* Category Filters Bar */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '16px', paddingTop: '12px', borderTop: '1px solid #1e293b' }}>
          <span style={{ fontSize: '0.8rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Categories:</span>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => onSelectCategory(cat)}
              style={{
                background: activeCategory === cat ? '#4f46e5' : '#1e293b',
                color: activeCategory === cat ? '#ffffff' : '#94a3b8',
                border: '1px solid',
                borderColor: activeCategory === cat ? '#6366f1' : '#334155',
                padding: '5px 14px',
                borderRadius: '6px',
                fontSize: '0.85rem',
                fontWeight: 500,
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              {cat}
            </button>
          ))}
          <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: '#94a3b8' }}>
            <ShieldCheck size={14} color="#34d399" />
            <span>Zero-Overselling Concurrency Protection Active</span>
          </div>
        </div>
      </div>
    </header>
  );
};
