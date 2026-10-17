import React from 'react';
import { Product } from '../types';
import { ShoppingCart, Check, Zap } from 'lucide-react';

interface ProductCardProps {
  product: Product;
  onAddToCart: (product: Product) => void;
  onViewDetails: (product: Product) => void;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product, onAddToCart, onViewDetails }) => {
  const [added, setAdded] = React.useState(false);

  const handleAdd = (e: React.MouseEvent) => {
    e.stopPropagation();
    onAddToCart(product);
    setAdded(true);
    setTimeout(() => setAdded(false), 1200);
  };

  return (
    <div
      className="card-surface"
      onClick={() => onViewDetails(product)}
      style={{
        overflow: 'hidden',
        cursor: 'pointer',
        display: 'flex',
        flexDirection: 'column',
        height: '100%'
      }}
    >
      {/* Product Image Frame */}
      <div style={{ position: 'relative', width: '100%', height: '200px', backgroundColor: '#0f172a', overflow: 'hidden' }}>
        <img
          src={product.imageUrl}
          alt={product.name}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            transition: 'transform 0.4s ease'
          }}
          onMouseEnter={(e) => (e.currentTarget.style.transform = 'scale(1.05)')}
          onMouseLeave={(e) => (e.currentTarget.style.transform = 'scale(1)')}
        />
        <div style={{ position: 'absolute', top: '12px', left: '12px', display: 'flex', gap: '6px' }}>
          <span className="badge badge-indigo">{product.categoryName}</span>
          {product.price > 200 && (
            <span className="badge badge-amber" style={{ display: 'flex', alignItems: 'center', gap: '3px' }}>
              <Zap size={11} /> Flagship
            </span>
          )}
        </div>
        <div style={{ position: 'absolute', bottom: '10px', right: '12px' }}>
          {product.inStock ? (
            <span className="badge badge-emerald">{product.stockQuantity} in stock</span>
          ) : (
            <span className="badge badge-rose">Out of stock</span>
          )}
        </div>
      </div>

      {/* Product Info */}
      <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', flex: 1 }}>
        <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, marginBottom: '4px' }}>
          SKU: {product.sku}
        </div>
        <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', marginBottom: '8px', lineHeight: 1.3 }}>
          {product.name}
        </h3>
        <p style={{
          fontSize: '0.85rem',
          color: '#94a3b8',
          marginBottom: '16px',
          flex: 1,
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden'
        }}>
          {product.description}
        </p>

        {/* Price & Add to Cart button */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 'auto', paddingTop: '12px', borderTop: '1px solid #1e293b' }}>
          <div>
            <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block' }}>Unit Price</span>
            <span style={{ fontSize: '1.35rem', fontWeight: 800, color: '#38bdf8' }}>
              ${product.price.toFixed(2)}
            </span>
          </div>

          <button
            onClick={handleAdd}
            disabled={!product.inStock}
            className="btn-primary"
            style={{
              padding: '8px 16px',
              fontSize: '0.85rem',
              background: added ? '#10b981' : undefined
            }}
          >
            {added ? (
              <>
                <Check size={16} />
                <span>Added</span>
              </>
            ) : (
              <>
                <ShoppingCart size={16} />
                <span>Add to Cart</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
