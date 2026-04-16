import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getProducts, getInventory } from '../../api/inventoryApi';
import { getOrders } from '../../api/orderApi';
import { getPurchaseOrders } from '../../api/purchaseOrderApi';
import { getShipments } from '../../api/shipmentApi';
import { getApiErrorMessage } from '../../services/apiError';
import apiClient from '../../api/apiClient';

function ManagerDashboard() {
  const { user } = useAuth();
  const [stats, setStats]             = useState({ products: 0, inventory: 0, orders: 0, purchaseOrders: 0, shipments: 0 });
  const [pendingOrders, setPendingOrders] = useState([]);
  const [lowStockItems, setLowStockItems] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [error, setError]             = useState('');

  const loadAll = async () => {
    try {
      const [products, inventory, orders, purchaseOrders, shipments] = await Promise.all([
        getProducts(), getInventory(), getOrders(), getPurchaseOrders(), getShipments(),
      ]);
      setStats({
        products: products.length,
        inventory: inventory.length,
        orders: orders.length,
        purchaseOrders: purchaseOrders.length,
        shipments: shipments.length,
      });
      // Compute pending-stock orders from all orders
      setPendingOrders(orders.filter(o => o.status === 'PENDING_STOCK'));
      // Compute low-stock items
      setLowStockItems(inventory.filter(i => i.quantity <= i.reorderLevel));
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  const loadNotifications = async () => {
    try {
      const { data } = await apiClient.get('/notifications');
      setNotifications(data);
    } catch { /* non-critical */ }
  };

  useEffect(() => {
    loadAll();
    loadNotifications();
    const interval = setInterval(loadNotifications, 10000);
    return () => clearInterval(interval);
  }, []);

  const metricCards = [
    { label: 'Products',        value: stats.products,        icon: '📦' },
    { label: 'Inventory Items', value: stats.inventory,       icon: '🗄️' },
    { label: 'Orders',          value: stats.orders,          icon: '🛒' },
    { label: 'Purchase Orders', value: stats.purchaseOrders,  icon: '📋' },
    { label: 'Shipments',       value: stats.shipments,       icon: '🚚' },
  ];

  return (
    <div className="stack">
      <div className="dashboard-welcome">
        <h2>Welcome back, <span className="accent">{user?.name}</span> 👋</h2>
        <p className="role-badge manager-badge">Manager</p>
      </div>

      {error && <div className="error-notice">{error}</div>}

      {/* ── System Overview ── */}
      <h3 className="section-title">System Overview</h3>
      <div className="card-grid">
        {metricCards.map(s => (
          <article className="card metric" key={s.label}>
            <div className="metric-icon">{s.icon}</div>
            <h3>{s.label}</h3>
            <p>{s.value}</p>
          </article>
        ))}
      </div>

      {/* ── Low Stock / Pending Orders Alert ── */}
      {(pendingOrders.length > 0 || lowStockItems.length > 0) && (
        <>
          <h3 className="section-title">
            ⚠️ Action Required
            <span className="badge badge-warning" style={{ marginLeft: 8 }}>
              {pendingOrders.length + lowStockItems.length}
            </span>
          </h3>

          {/* Pending stock orders */}
          {pendingOrders.length > 0 && (
            <div className="card">
              <h3>🛒 Orders Awaiting Stock <span className="badge badge-warning">{pendingOrders.length}</span></h3>
              <p className="hint-text">
                These orders could not be processed due to insufficient inventory.
                Create a Purchase Order to restock the required products.
              </p>
              <div className="table-wrap">
                <table className="app-table">
                  <thead>
                    <tr><th>Order #</th><th>Customer</th><th>Products Needed</th><th>Total</th></tr>
                  </thead>
                  <tbody>
                    {pendingOrders.map(o => (
                      <tr key={o.id}>
                        <td><strong>{o.orderNumber ?? `#${o.id}`}</strong></td>
                        <td>{o.customer?.name ?? '—'}</td>
                        <td>
                          {o.items?.map(item => (
                            <span key={item.id} style={{ display: 'block', fontSize: 13 }}>
                              {item.product?.name ?? `Product #${item.product?.id}`} × {item.quantity}
                            </span>
                          )) ?? '—'}
                        </td>
                        <td>₹{o.totalAmount ?? '0.00'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Low-stock inventory items */}
          {lowStockItems.length > 0 && (
            <div className="card">
              <h3>📉 Low Stock Items <span className="badge badge-warning">{lowStockItems.length}</span></h3>
              <p className="hint-text">These items are at or below their reorder level. Consider creating a Purchase Order.</p>
              <div className="table-wrap">
                <table className="app-table">
                  <thead><tr><th>Product</th><th>Current Qty</th><th>Reorder At</th><th>Location</th></tr></thead>
                  <tbody>
                    {lowStockItems.map(item => (
                      <tr key={item.id}>
                        <td>{item.product?.name ?? '—'}</td>
                        <td style={{ color: '#ef4444', fontWeight: 600 }}>{item.quantity}</td>
                        <td>{item.reorderLevel}</td>
                        <td>{item.storageLocation?.code ?? <span style={{ color: '#f59e0b' }}>Unassigned</span>}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}

      {/* ── Notifications ── */}
      <h3 className="section-title">
        Live Notifications <span className="badge">{notifications.length}</span>
      </h3>
      <div className="card notifications-panel">
        {notifications.length === 0 ? (
          <p className="empty-cell">No notifications yet.</p>
        ) : (
          <ul className="notification-list">
            {notifications.map((n, i) => (
              <li key={i} className={`notification-item ${
                n.includes('LOW_STOCK') || n.includes('PENDING_STOCK') ? 'notif-warning' :
                n.includes('CANCELLED') ? 'notif-danger' : 'notif-info'
              }`}>
                {n}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

export default ManagerDashboard;
