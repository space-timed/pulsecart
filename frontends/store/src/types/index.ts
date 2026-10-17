export interface Product {
  id: string;
  sku: string;
  name: string;
  description: string;
  categoryId: string;
  categoryName: string;
  price: number;
  inStock: boolean;
  stockQuantity: number;
  imageUrl?: string;
  tags?: string;
}

export interface CartItem {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  imageUrl?: string;
}

export interface Cart {
  customerId: string;
  items: CartItem[];
  subtotal: number;
  discount: number;
  total: number;
  appliedCoupon?: string;
}

export interface Order {
  id: string;
  customerId: string;
  totalAmount: number;
  status: 'CREATED' | 'RESERVED' | 'CONFIRMED' | 'CANCELLED' | 'SHIPPED' | 'DELIVERED';
  items: CartItem[];
  createdAt: string;
  shippingAddress: string;
  trackingNumber?: string;
  carrier?: string;
}

export interface SagaStep {
  name: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'COMPENSATED';
  message: string;
  timestamp?: string;
}

export interface ShipmentTracking {
  trackingNumber: string;
  carrier: string;
  status: 'LABEL_CREATED' | 'DISPATCHED' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED';
  origin: string;
  destination: string;
  estimatedDelivery: string;
  history: { timestamp: string; location: string; event: string }[];
}

export interface ReturnRecord {
  id: string;
  orderId: string;
  status: 'REQUESTED' | 'APPROVED' | 'REJECTED' | 'RECEIVED' | 'REFUNDED';
  reason: string;
  refundAmount: number;
  createdAt: string;
}

export interface PromotionCoupon {
  code: string;
  discountPercent: number;
  description: string;
  minimumAmount: number;
}
