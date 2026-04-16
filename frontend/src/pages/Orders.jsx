import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { cancelOrder, processOrder, getOrders } from '../api/orderApi';
import { getApiErrorMessage } from '../services/apiError';

/**
 * Orders page — role-aware read view.
 *
 * MANAGER  : read-only monitoring (all orders, no actions)
 * STAFF    : process CREATED orders (no create form — done via Dashboard)
 * CUSTOMER : view own orders + cancel CREATED ones (no create form — done via Dashboard)
 *
 * Order creation for CUSTOMER is handled exclusively in CustomerDashboard.
 */
function Orders() {
  const { user } = useAuth();
  const role = user?.role ?? '';

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');

  const load = async () => {
    try {
      setLoading(true);
      const all = await getOrders();
      // Customers see only their own orders
      if (role === 'CUSTOMER') {
        setOrders(all.filter(o => !o.customer || o.customer?.id === user?.id));
      } else {
        setOrders(all);
      }
    } catch (err) { setError(getApiErrorMessage(err)); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const onProcess = async (id) => {
    setError(''); setMsg('');
    try { await processOrder(id); setMsg('✅ Order processed.'); load(); }
    catch (err) { setError(getApiErrorMessage(err)); }
  };

  const onCancel = async (id) => {
    if (!window.confirm('Cancel this order?')) return;
    setError(''); setMsg('');
    try { await cancelOrder(id); setMsg('Order cancelled.'); load(); }
    catch (err) { setError(getApiErrorMessage(err)); }
  };

  const STATUS_BADGE = {
    CREATED:   'status-created',
    PROCESSED: 'status-processed',
    CANCELLED: 'status-cancelled',
    SHIPPED:   'status-shipped',
  };

  // Label map — human-friendly for customer, technical for ops roles
  const statusLabel = (status) => {
    if (role === 'CUSTOMER') {
      const MAP = { CREATED: '🟡 Pending', PROCESSED: '⚙️ Processing',
                    SHIPPED: '🚚 Shipped', DELIVERED: '✅ Delivered', CANCELLED: '❌ Cancelled' };
      return MAP[status] ?? status;
    }
    return status;
  };

  // Managers and Staff see Customer column; Customers don't need it
  const showCustomerCol = role !== 'CUSTOMER';
  // Only Staff can process; Only Customer can cancel
  const showActionsCol = role === 'STAFF' || role === 'CUSTOMER';
  const colSpan = 3 + (showCustomerCol ? 1 : 0) + (showActionsCol ? 1 : 0);

  return (
    <div className="stack">
      {error && <div className="error-notice">{error}</div>}
      {msg && <div className="success-notice">{msg}</div>}

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h3>{role === 'CUSTOMER' ? '📋 My Orders' : '🛒 Orders'}</h3>
          {role === 'MANAGER' && (
            <span className="hint-text" style={{ fontStyle: 'italic' }}>👁️ Read-only monitoring view</span>
          )}
          {role === 'CUSTOMER' && (
            <span className="hint-text">Place new orders from your Dashboard.</span>
          )}
          <button className="btn secondary" onClick={load}>🔄 Refresh</button>
        </div>

        {loading ? <p style={{ color: '#6b7280' }}>Loading…</p> : (
          <div className="table-wrap">
            <table className="app-table">
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Status</th>
                  <th>Total</th>
                  {showCustomerCol && <th>Customer</th>}
                  {showActionsCol  && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {orders.length === 0
                  ? <tr><td colSpan={colSpan} className="empty-cell">No orders found.</td></tr>
                  : orders.map(o => (
                    <tr key={o.id}>
                      <td><strong>{o.orderNumber ?? `#${o.id}`}</strong></td>
                      <td>
                        <span className={`status-badge ${STATUS_BADGE[o.status] ?? 'status-pending'}`}>
                          {statusLabel(o.status)}
                        </span>
                      </td>
                      <td>₹{o.totalAmount ?? '0.00'}</td>
                      {showCustomerCol && <td>{o.customer?.name ?? '—'}</td>}
                      {showActionsCol && (
                        <td>
                          <div className="actions">
                            {role === 'STAFF' && o.status === 'CREATED' && (
                              <button className="btn" onClick={() => onProcess(o.id)}>Process</button>
                            )}
                            {role === 'CUSTOMER' && o.status === 'CREATED' && (
                              <button className="btn danger" onClick={() => onCancel(o.id)}>Cancel</button>
                            )}
                          </div>
                        </td>
                      )}
                    </tr>
                  ))
                }
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default Orders;
