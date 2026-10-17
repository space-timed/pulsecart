import React, { useState, useEffect } from 'react';
import { Product, Cart, Order, ReturnRecord, PromotionCoupon } from './types';
import { apiClient, INITIAL_PRODUCTS } from './api/client';
import { Header } from './components/Header';
import { ProductCard } from './components/ProductCard';
import { ProductDetailModal } from './components/ProductDetailModal';
import { CartDrawer } from './components/CartDrawer';
import { CheckoutSagaModal } from './components/CheckoutSagaModal';
import { OrderHistoryView } from './components/OrderHistoryView';
import { ReturnRequestModal } from './components/ReturnRequestModal';
import { SellerAdminView } from './components/SellerAdminView';
import { Sparkles, Shield, Cpu, RefreshCw } from 'lucide-react';

export const App: React.FC = () => {
  const [products, setProducts] = useState<Product[]>(INITIAL_PRODUCTS);
  const [filteredProducts, setFilteredProducts] = useState<Product[]>(INITIAL_PRODUCTS);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeCategory, setActiveCategory] = useState('All');

  const [cart, setCart] = useState<Cart>({
    customerId: 'CUST-DEMO-01',
    items: [],
    subtotal: 0,
    discount: 0,
    total: 0
  });

  const [orders, setOrders] = useState<Order[]>([
    {
      id: 'ORD-DEMO-100',
      customerId: 'CUST-DEMO-01',
      totalAmount: 129.99,
      status: 'CONFIRMED',
      items: [
        {
          productId: 'PROD-001',
          productName: 'Pulse Mechanical Keyboard Pro',
          price: 129.99,
          quantity: 1,
          imageUrl: INITIAL_PRODUCTS[0].imageUrl
        }
      ],
      createdAt: new Date(Date.now() - 86400000 * 3).toISOString(), // 3 days ago (eligible for return)
      shippingAddress: '100 Silicon Way, Austin, TX',
      trackingNumber: 'FDX-748921849',
      carrier: 'FEDEX'
    }
  ]);

  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] = useState(false);
  const [isOrdersOpen, setIsOrdersOpen] = useState(false);
  const [isAdminOpen, setIsAdminOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [selectedOrderForReturn, setSelectedOrderForReturn] = useState<Order | null>(null);

  // Recalculate filtered catalog
  useEffect(() => {
    let list = products;
    if (activeCategory !== 'All') {
      list = list.filter(p => p.categoryName.toLowerCase() === activeCategory.toLowerCase());
    }
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      list = list.filter(p =>
        p.name.toLowerCase().includes(q) ||
        p.description.toLowerCase().includes(q) ||
        (p.tags && p.tags.toLowerCase().includes(q))
      );
    }
    setFilteredProducts(list);
  }, [products, searchQuery, activeCategory]);

  // Cart operations
  const recalculateCart = (items: Cart['items'], coupon?: string, discountPct: number = 0) => {
    const subtotal = items.reduce((acc, i) => acc + i.price * i.quantity, 0);
    const discount = coupon ? (subtotal * discountPct) / 100 : 0;
    const total = Math.max(0, subtotal - discount);

    setCart({
      customerId: 'CUST-DEMO-01',
      items,
      subtotal,
      discount,
      total,
      appliedCoupon: coupon
    });
  };

  const handleAddToCart = (product: Product, qty: number = 1) => {
    const existing = cart.items.find(i => i.productId === product.id);
    let updated;
    if (existing) {
      updated = cart.items.map(i =>
        i.productId === product.id ? { ...i, quantity: i.quantity + qty } : i
      );
    } else {
      updated = [...cart.items, {
        productId: product.id,
        productName: product.name,
        price: product.price,
        quantity: qty,
        imageUrl: product.imageUrl
      }];
    }
    recalculateCart(updated, cart.appliedCoupon, cart.discount > 0 ? 10 : 0);
  };

  const handleUpdateQuantity = (productId: string, delta: number) => {
    const updated = cart.items.map(i => {
      if (i.productId === productId) {
        const nextQty = i.quantity + delta;
        return nextQty > 0 ? { ...i, quantity: nextQty } : null;
      }
      return i;
    }).filter(Boolean) as Cart['items'];

    recalculateCart(updated, cart.appliedCoupon, cart.discount > 0 ? 10 : 0);
  };

  const handleRemoveItem = (productId: string) => {
    const updated = cart.items.filter(i => i.productId !== productId);
    recalculateCart(updated, cart.appliedCoupon, cart.discount > 0 ? 10 : 0);
  };

  const handleApplyCoupon = (coupon: PromotionCoupon | null) => {
    if (coupon) {
      recalculateCart(cart.items, coupon.code, coupon.discountPercent);
    } else {
      recalculateCart(cart.items, undefined, 0);
    }
  };

  // Order Saga completed
  const handleOrderCompleted = (newOrder: Order) => {
    if (newOrder.status === 'CONFIRMED') {
      setOrders(prev => [newOrder, ...prev]);
      // Reset cart
      setCart({
        customerId: 'CUST-DEMO-01',
        items: [],
        subtotal: 0,
        discount: 0,
        total: 0
      });
      // Deduct stock in catalog
      setProducts(prev => prev.map(p => {
        const matched = newOrder.items.find(i => i.productId === p.id);
        if (matched) {
          const nextStock = Math.max(0, p.stockQuantity - matched.quantity);
          return { ...p, stockQuantity: nextStock, inStock: nextStock > 0 };
        }
        return p;
      }));
    }
  };

  // Admin Ops
  const handleUpdateProductStock = (productId: string, newStock: number) => {
    setProducts(prev => prev.map(p =>
      p.id === productId ? { ...p, stockQuantity: newStock, inStock: newStock > 0 } : p
    ));
  };

  const handleUpdateProductPrice = (productId: string, newPrice: number) => {
    setProducts(prev => prev.map(p =>
      p.id === productId ? { ...p, price: newPrice } : p
    ));
  };

  const handleTriggerRebuild = async () => {
    try {
      await fetch('/api/v1/search/rebuild', { method: 'POST' });
    } catch {
      // offline simulation
    }
  };

  const cartTotalItemCount = cart.items.reduce((sum, i) => sum + i.quantity, 0);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Navigation Header */}
      <Header
        cartItemCount={cartTotalItemCount}
        onOpenCart={() => setIsCartOpen(true)}
        onOpenOrders={() => setIsOrdersOpen(true)}
        onToggleAdmin={() => setIsAdminOpen(!isAdminOpen)}
        isAdminOpen={isAdminOpen}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        activeCategory={activeCategory}
        onSelectCategory={setActiveCategory}
      />

      {/* Main Content Area */}
      <main style={{ flex: 1, paddingBottom: '60px' }}>
        {/* Ops Console Drawer when toggled */}
        {isAdminOpen && (
          <SellerAdminView
            products={products}
            onUpdateProductStock={handleUpdateProductStock}
            onUpdateProductPrice={handleUpdateProductPrice}
            onTriggerRebuild={handleTriggerRebuild}
          />
        )}

        {isOrdersOpen ? (
          /* Order History Screen */
          <OrderHistoryView
            orders={orders}
            onBackToStore={() => setIsOrdersOpen(false)}
            onInitiateReturn={(order) => setSelectedOrderForReturn(order)}
          />
        ) : (
          /* Store Hero Banner & Catalog */
          <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '30px 24px' }}>
            
            {/* Hero Banner */}
            <div style={{
              background: 'linear-gradient(135deg, rgba(30, 27, 75, 0.8) 0%, rgba(15, 23, 42, 0.95) 100%)',
              border: '1px solid #3730a3',
              borderRadius: '16px',
              padding: '36px 40px',
              marginBottom: '36px',
              position: 'relative',
              overflow: 'hidden'
            }}>
              <div style={{ maxWidth: '720px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
                  <span className="badge badge-indigo" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Sparkles size={12} /> Production Microservices
                  </span>
                  <span className="badge badge-emerald">Spring Boot 3.4.3 • Java 26</span>
                </div>
                <h1 style={{ fontSize: '2.2rem', fontWeight: 800, color: '#f8fafc', letterSpacing: '-0.02em', lineHeight: 1.2, marginBottom: '12px' }}>
                  Resilient Distributed Commerce Engine
                </h1>
                <p style={{ fontSize: '1rem', color: '#94a3b8', lineHeight: 1.6, marginBottom: '20px' }}>
                  Experience real-time Saga checkout orchestration, atomic conditional inventory locking,
                  carrier adapter patterns, and asynchronous Kafka event streaming.
                </p>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', fontSize: '0.85rem', color: '#cbd5e1' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Shield size={16} color="#34d399" />
                    <span>Transactional Outbox Pattern</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Cpu size={16} color="#818cf8" />
                    <span>Database-Per-Service PostgreSQL 16</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <RefreshCw size={16} color="#38bdf8" />
                    <span>Kafka KRaft Event Bus</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Catalog Grid Header */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
              <div>
                <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#f8fafc' }}>
                  {activeCategory === 'All' ? 'Featured Catalog' : `${activeCategory} Products`}
                </h2>
                <span style={{ fontSize: '0.8rem', color: '#94a3b8' }}>
                  Showing {filteredProducts.length} items powered by CQRS Search Projection
                </span>
              </div>
            </div>

            {/* Product Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '24px' }}>
              {filteredProducts.map(p => (
                <ProductCard
                  key={p.id}
                  product={p}
                  onAddToCart={handleAddToCart}
                  onViewDetails={setSelectedProduct}
                />
              ))}
            </div>
          </div>
        )}
      </main>

      {/* Slide-over Shopping Cart */}
      <CartDrawer
        isOpen={isCartOpen}
        onClose={() => setIsCartOpen(false)}
        cart={cart}
        onUpdateQuantity={handleUpdateQuantity}
        onRemoveItem={handleRemoveItem}
        onApplyCoupon={handleApplyCoupon}
        onStartCheckout={() => {
          setIsCartOpen(false);
          setIsCheckoutModalOpen(true);
        }}
      />

      {/* Distributed Saga Checkout Modal */}
      <CheckoutSagaModal
        isOpen={isCheckoutModalOpen}
        onClose={() => setIsCheckoutModalOpen(false)}
        cart={cart}
        onOrderCompleted={handleOrderCompleted}
      />

      {/* Product Detail Modal */}
      <ProductDetailModal
        product={selectedProduct}
        isOpen={!!selectedProduct}
        onClose={() => setSelectedProduct(null)}
        onAddToCart={handleAddToCart}
      />

      {/* RMA Return Modal */}
      <ReturnRequestModal
        order={selectedOrderForReturn}
        isOpen={!!selectedOrderForReturn}
        onClose={() => setSelectedOrderForReturn(null)}
        onReturnSubmitted={(rma) => {
          setOrders(prev => prev.map(o =>
            o.id === rma.orderId ? { ...o, status: 'DELIVERED' } : o
          ));
        }}
      />
    </div>
  );
};
