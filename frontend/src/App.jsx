import { useMemo, useState } from 'react';
import { useAuth } from './context/AuthContext';


import Login from './pages/Login';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';

// Manager pages
import ManagerDashboard from './pages/manager/ManagerDashboard';
import Inventory from './pages/Inventory';
import Orders from './pages/Orders';
import Products from './pages/Products';
import PurchaseOrders from './pages/PurchaseOrders';
import Reports from './pages/Reports';
import Shipment from './pages/Shipment';
import StorageLocations from './pages/StorageLocations';

// Staff pages
import StaffDashboard from './pages/staff/StaffDashboard';

// Customer pages
import CustomerDashboard from './pages/customer/CustomerDashboard';

// Supplier pages
import SupplierDashboard from './pages/supplier/SupplierDashboard';

// ── Role-based nav config ──────────────────────────────────────────────────
const NAV_BY_ROLE = {
  MANAGER: [
    { key: 'dashboard', label: '🏠 Dashboard' },
    { key: 'products', label: '📦 Products' },
    { key: 'storage-locations', label: '📍 Storage Locations' },
    { key: 'inventory', label: '🗄️  Inventory' },
    { key: 'orders', label: '🛒 Orders' },
    { key: 'purchase-orders', label: '📋 Purchase Orders' },
    { key: 'shipments', label: '🚚 Shipments' },
    { key: 'reports', label: '📊 Reports' },
  ],
  STAFF: [
    { key: 'dashboard', label: '🏠 Dashboard' },
    { key: 'inventory', label: '🗄️  Inventory' },
    { key: 'shipments', label: '🚚 Shipments' },
  ],
  CUSTOMER: [
    { key: 'dashboard', label: '🏠 Dashboard' },
    { key: 'orders', label: '🛒 My Orders' },
  ],
  SUPPLIER: [
    { key: 'dashboard', label: '🏠 Dashboard' },
    // Supplier only needs Dashboard — all PO visibility and delivery actions are there
  ],
};

// ── Page resolver per role ─────────────────────────────────────────────────
function resolvePageComponent(role, page) {
  if (role === 'MANAGER') {
    if (page === 'dashboard') return <ManagerDashboard />;
    if (page === 'products') return <Products />;
    if (page === 'storage-locations') return <StorageLocations />;
    if (page === 'inventory') return <Inventory />;
    if (page === 'orders') return <Orders />;
    if (page === 'purchase-orders') return <PurchaseOrders />;
    if (page === 'shipments') return <Shipment />;
    if (page === 'reports') return <Reports />;
  }
  if (role === 'STAFF') {
    if (page === 'dashboard') return <StaffDashboard />;
    if (page === 'inventory') return <Inventory />;
    if (page === 'shipments') return <Shipment />;
    // Orders page removed — Staff processes orders inside Dashboard
  }
  if (role === 'CUSTOMER') {
    if (page === 'dashboard') return <CustomerDashboard />;
    if (page === 'orders') return <Orders />;
  }
  if (role === 'SUPPLIER') {
    if (page === 'dashboard') return <SupplierDashboard />;
    // No other pages for Supplier — dashboard contains PO view + deliver action
  }
  return <div className="card"><p>Page not found.</p></div>;
}

// ── App ────────────────────────────────────────────────────────────────────
function App() {
  const { user } = useAuth();
  const [activePage, setActivePage] = useState('dashboard');

  const navItems = useMemo(
    () => NAV_BY_ROLE[user?.role] ?? [],
    [user?.role]
  );

  const activeLabel = useMemo(
    () => navItems.find((item) => item.key === activePage)?.label ?? 'WMS',
    [activePage, navItems]
  );

  // Not logged in → show Login
  if (!user) return <Login />;

  return (
    <div className="app-shell">
      <Sidebar items={navItems} activePage={activePage} onSelect={setActivePage} role={user.role} />
      <div className="app-content">
        <Navbar title={activeLabel} />
        <main className="page-content">
          {resolvePageComponent(user.role, activePage)}
        </main>
      </div>
    </div>
  );
}

export default App;
