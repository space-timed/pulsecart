import { Product, Cart, Order, ShipmentTracking, ReturnRecord, PromotionCoupon } from '../types';

const GATEWAY_BASE = '';

export const INITIAL_PRODUCTS: Product[] = [
  {
    id: 'PROD-001',
    sku: 'SKU-MECH-KB',
    name: 'Pulse Mechanical Keyboard Pro',
    description: 'Hot-swappable tactile RGB mechanical keyboard with PBT keycaps and aluminum chassis.',
    categoryId: 'CAT-TECH',
    categoryName: 'Electronics',
    price: 129.99,
    inStock: true,
    stockQuantity: 42,
    imageUrl: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80',
    tags: 'keyboard,gaming,rgb,tactile'
  },
  {
    id: 'PROD-002',
    sku: 'SKU-MOUSE-WL',
    name: 'Pulse Precision Wireless Mouse',
    description: 'Ultralight 58g ergonomic gaming mouse with 26K DPI optical sensor and 80-hour battery.',
    categoryId: 'CAT-TECH',
    categoryName: 'Electronics',
    price: 79.99,
    inStock: true,
    stockQuantity: 88,
    imageUrl: 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=600&q=80',
    tags: 'mouse,wireless,precision,ultralight'
  },
  {
    id: 'PROD-003',
    sku: 'SKU-MON-4K',
    name: 'Pulse Vision 32" 4K HDR Monitor',
    description: '144Hz IPS panel with 1ms response, 98% DCI-P3 color gamut, and 90W USB-C power delivery.',
    categoryId: 'CAT-TECH',
    categoryName: 'Electronics',
    price: 499.99,
    inStock: true,
    stockQuantity: 15,
    imageUrl: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=600&q=80',
    tags: 'monitor,4k,display,hdr'
  },
  {
    id: 'PROD-004',
    sku: 'SKU-HEADSET-PRO',
    name: 'Pulse Studio Wireless ANC Headphones',
    description: 'Audiophile planar magnetic drivers, hybrid active noise cancellation, and spatial audio.',
    categoryId: 'CAT-AUDIO',
    categoryName: 'Audio',
    price: 199.99,
    inStock: true,
    stockQuantity: 24,
    imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80',
    tags: 'audio,headphones,anc,bluetooth'
  },
  {
    id: 'PROD-005',
    sku: 'SKU-DESK-PAD',
    name: 'Pulse Hydrophobic Desk Mat XXL',
    description: 'Micro-woven spill-resistant fabric surface with stitched anti-fray edges and rubber base.',
    categoryId: 'CAT-OFFICE',
    categoryName: 'Office',
    price: 29.99,
    inStock: true,
    stockQuantity: 120,
    imageUrl: 'https://images.unsplash.com/photo-1629429408209-1f912961dbd8?auto=format&fit=crop&w=600&q=80',
    tags: 'desk,mat,accessories,office'
  },
  {
    id: 'PROD-006',
    sku: 'SKU-MIC-CARDIOID',
    name: 'Pulse Stream Broadcast Microphone',
    description: 'USB/XLR dynamic microphone with onboard DSP, background noise rejection, and mute touch sensor.',
    categoryId: 'CAT-AUDIO',
    categoryName: 'Audio',
    price: 149.99,
    inStock: true,
    stockQuantity: 30,
    imageUrl: 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=600&q=80',
    tags: 'mic,streaming,broadcast,studio'
  }
];

export const AVAILABLE_COUPONS: PromotionCoupon[] = [
  { code: 'FLASH50', discountPercent: 50, description: 'Flash-Sale 50% discount on orders over $100', minimumAmount: 100 },
  { code: 'WELCOME10', discountPercent: 10, description: 'Welcome voucher for 10% off entire order', minimumAmount: 0 },
  { code: 'STAFF25', discountPercent: 25, description: 'Staff Architect 25% portfolio promo', minimumAmount: 50 }
];

export const apiClient = {
  async searchProducts(query: string = '', category: string = '', minPrice?: number, maxPrice?: number): Promise<Product[]> {
    try {
      const params = new URLSearchParams();
      if (query) params.append('q', query);
      if (category && category !== 'All') params.append('category', category);
      if (minPrice !== undefined) params.append('minPrice', minPrice.toString());
      if (maxPrice !== undefined) params.append('maxPrice', maxPrice.toString());

      const res = await fetch(`${GATEWAY_BASE}/api/v1/search?${params.toString()}`);
      if (res.ok) {
        const data = await res.json();
        if (data.results && data.results.length > 0) {
          return data.results.map((r: any) => ({
            id: r.productId,
            sku: r.sku,
            name: r.name,
            description: r.description,
            categoryId: r.categoryId,
            categoryName: r.categoryName,
            price: Number(r.price),
            inStock: r.inStock,
            stockQuantity: r.stockQuantity,
            imageUrl: INITIAL_PRODUCTS.find(p => p.id === r.productId)?.imageUrl || INITIAL_PRODUCTS[0].imageUrl
          }));
        }
      }
    } catch {
      // Fallback to local catalog
    }

    return INITIAL_PRODUCTS.filter(p => {
      const matchesQuery = !query || p.name.toLowerCase().includes(query.toLowerCase()) || p.description.toLowerCase().includes(query.toLowerCase());
      const matchesCategory = !category || category === 'All' || p.categoryName.toLowerCase() === category.toLowerCase();
      const matchesMin = minPrice === undefined || p.price >= minPrice;
      const matchesMax = maxPrice === undefined || p.price <= maxPrice;
      return matchesQuery && matchesCategory && matchesMin && matchesMax;
    });
  },

  async executeCheckoutSaga(
    cart: Cart,
    shippingAddress: string,
    chaosDecline: boolean = false
  ): Promise<{ orderId: string; status: string; compensationTriggered?: boolean }> {
    const orderId = 'ORD-' + Math.random().toString(36).substring(2, 9).toUpperCase();
    const idempotencyKey = 'idemp-' + Date.now();

    try {
      const res = await fetch(`${GATEWAY_BASE}/api/v1/orders/checkout`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Idempotency-Key': idempotencyKey,
          ...(chaosDecline ? { 'X-Chaos-Action': 'DECLINE_PAYMENT' } : {})
        },
        body: JSON.stringify({
          customerId: cart.customerId,
          idempotencyKey,
          shippingAddress,
          items: cart.items.map(i => ({
            productId: i.productId,
            quantity: i.quantity,
            price: i.price
          }))
        })
      });

      if (res.ok) {
        const data = await res.json();
        return {
          orderId: data.id,
          status: data.status,
          compensationTriggered: data.status === 'CANCELLED'
        };
      }
    } catch {
      // Offline fallback simulation
    }

    // Simulated Saga Execution result
    if (chaosDecline) {
      return { orderId, status: 'CANCELLED', compensationTriggered: true };
    }
    return { orderId, status: 'CONFIRMED', compensationTriggered: false };
  },

  async fetchTracking(trackingNumber: string): Promise<ShipmentTracking> {
    try {
      const res = await fetch(`${GATEWAY_BASE}/api/v1/shipments/tracking/${trackingNumber}`);
      if (res.ok) {
        const data = await res.json();
        return {
          trackingNumber: data.trackingNumber,
          carrier: data.carrier,
          status: 'IN_TRANSIT',
          origin: 'PulseCart Central Hub (Austin, TX)',
          destination: data.shippingAddress || '100 Customer Home',
          estimatedDelivery: data.estimatedDeliveryAt || 'In 2 Business Days',
          history: [
            { timestamp: new Date(Date.now() - 3600000 * 6).toISOString(), location: 'Austin, TX', event: 'Order Picked & Packed' },
            { timestamp: new Date(Date.now() - 3600000 * 3).toISOString(), location: 'Austin Distribution Hub', event: 'Dispatched with Carrier' },
            { timestamp: new Date().toISOString(), location: 'En Route Hub', event: 'In Transit' }
          ]
        };
      }
    } catch {
      // Fallback
    }

    return {
      trackingNumber,
      carrier: trackingNumber.startsWith('1Z') ? 'UPS' : trackingNumber.startsWith('DHL') ? 'DHL' : 'FEDEX',
      status: 'IN_TRANSIT',
      origin: 'PulseCart Central Hub (Austin, TX)',
      destination: 'Customer Destination',
      estimatedDelivery: 'Tomorrow by 5:00 PM',
      history: [
        { timestamp: '2026-03-29 09:30 AM', location: 'Austin Warehouse', event: 'Package Received by Carrier' },
        { timestamp: '2026-03-29 01:15 PM', location: 'Regional Sorting Facility', event: 'In Transit' }
      ]
    };
  },

  async submitReturnRequest(orderId: string, customerId: string, reason: string, items: any[]): Promise<ReturnRecord> {
    const rmaId = 'RMA-' + Math.random().toString(36).substring(2, 8).toUpperCase();
    try {
      const res = await fetch(`${GATEWAY_BASE}/api/v1/returns`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          orderId,
          customerId,
          deliveryDate: new Date(Date.now() - 86400000 * 5).toISOString(),
          reason,
          items: items.map(i => ({
            productId: i.productId,
            quantity: i.quantity,
            condition: 'UNOPENED',
            price: i.price
          }))
        })
      });
      if (res.ok) {
        const data = await res.json();
        return {
          id: data.id,
          orderId: data.orderId,
          status: data.status,
          reason: data.reason,
          refundAmount: Number(data.refundAmount),
          createdAt: data.createdAt
        };
      }
    } catch {
      // Fallback
    }

    return {
      id: rmaId,
      orderId,
      status: 'APPROVED',
      reason,
      refundAmount: items.reduce((sum, i) => sum + i.price * i.quantity, 0),
      createdAt: new Date().toISOString()
    };
  }
};
