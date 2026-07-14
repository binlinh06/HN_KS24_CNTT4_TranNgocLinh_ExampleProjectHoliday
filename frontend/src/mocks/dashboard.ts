// Mock Data for the Pho Bo Management System

export interface OrderMock {
  id: string;
  customerName: string;
  type: 'ONLINE' | 'POS';
  items: string;
  total: number;
  status: string;
  time: string;
}

export interface MenuItemMock {
  id: string;
  name: string;
  category: string;
  price: number;
  status: 'ACTIVE' | 'INACTIVE';
  image?: string;
}

export interface VoucherMock {
  code: string;
  discount: string;
  minSpend: number;
  expiry: string;
  status: 'ACTIVE' | 'EXPIRED';
}

export const mockOrders: OrderMock[] = [
  { id: 'ORD-001', customerName: 'Nguyễn Văn A', type: 'ONLINE', items: '1x Phở chín (Lớn), 1x Trà đá', total: 65000, status: 'CHO_XAC_NHAN', time: '10 phút trước' },
  { id: 'ORD-002', customerName: 'Bàn số 5', type: 'POS', items: '2x Phở tái (Vừa), 1x Quẩy (đĩa)', total: 115000, status: 'DANG_CHE_BIEN', time: '5 phút trước' },
  { id: 'ORD-003', customerName: 'Trần Thị B', type: 'ONLINE', items: '1x Phở đặc biệt, 1x Sữa đậu nành', total: 80000, status: 'DANG_GIAO', time: '15 phút trước' },
  { id: 'ORD-004', customerName: 'Bàn số 2', type: 'POS', items: '1x Phở chín nạm, 1x Trà chanh', total: 65000, status: 'HOAN_THANH', time: '30 phút trước' },
  { id: 'ORD-005', customerName: 'Lê Văn C', type: 'ONLINE', items: '3x Phở tái gầu', total: 165000, status: 'DA_HUY', time: '1 giờ trước' },
];

export const mockMenuItems: MenuItemMock[] = [
  { id: '1', name: 'Phở Chín nạm', category: 'Phở Bò', price: 50000, status: 'ACTIVE' },
  { id: '2', name: 'Phở Tái gầu', category: 'Phở Bò', price: 55000, status: 'ACTIVE' },
  { id: '3', name: 'Phở Đặc Biệt (Tái, Chín, Nạm, Gầu, Bò viên)', category: 'Phở Bò', price: 75000, status: 'ACTIVE' },
  { id: '4', name: 'Quẩy giòn (Đĩa)', category: 'Món ăn kèm', price: 15000, status: 'ACTIVE' },
  { id: '5', name: 'Trà đá Hà Nội', category: 'Thức uống', price: 5000, status: 'ACTIVE' },
  { id: '6', name: 'Sữa đậu nành', category: 'Thức uống', price: 15000, status: 'ACTIVE' },
];

export const mockVouchers: VoucherMock[] = [
  { code: 'PHOMOICUOI', discount: '10%', minSpend: 100000, expiry: '31/12/2026', status: 'ACTIVE' },
  { code: 'CHOISANG', discount: '20K', minSpend: 150000, expiry: '30/09/2026', status: 'ACTIVE' },
  { code: 'PHOBOSANG', discount: '15%', minSpend: 80000, expiry: '15/08/2026', status: 'ACTIVE' },
];

export const mockStats = {
  admin: {
    revenueToday: 5420000,
    ordersToday: 84,
    activeUsers: 1205,
    menuItems: 24,
  },
  customer: {
    loyaltyPoints: 350,
    totalSpent: 1850000,
    ordersCount: 24,
    savedAddresses: 2,
  },
  staff: {
    pendingOrders: 8,
    cookingOrders: 4,
    completedToday: 42,
    activeTables: 12,
  },
  manager: {
    monthlyRevenue: 145800000,
    attendanceRate: '95%',
    inventoryAlerts: 3,
    averageRating: 4.8,
  }
};
