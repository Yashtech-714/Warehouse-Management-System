import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getPurchaseOrders, deliverPurchaseOrder } from '../../api/purchaseOrderApi';
import { getApiErrorMessage } from '../../services/apiError';

/**
 * SupplierDashboard — Supplier's single work area.
 *
 * Responsibilities:
 *   ✔ View all purchase orders assigned to them
 *   ✔ Click "Mark Delivered" when goods are dispatched
 *
 * Supplier CANNOT:
 *   ✗ Create purchase orders  (Manager's responsibility)
 *   ✗ Edit PO details         (Manager's responsibility)
 *   ✗ Access inventory        (Staff's responsibility)
 */
function SupplierDashboard() {
  const { user } = useAuth();
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [error, setError]   = useState('');
  const [msg, setMsg]       = useState('');
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      setPurchaseOrders(await getPurchaseOrders());
    } catch (err) { setError(getApiErrorMessage(err)); }
    finally { setLoading(false); }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 15000); // auto-refresh every 15s
    return () => clearInterval(interval);
  }, []);

  const handleDeliver = async (id) => {
    setMsg(''); setError('');
    try {
      await deliverPurchaseOrder(id);
      setMsg('✅ Purchase order marked as DELIVERED. Warehouse staff can now receive the stock.');
      loadData();
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  // Human-readable status labels
  const STATUS_LABEL = {
    DRAFT:            '📝 Draft',
    SENT_TO_SUPPLIER: '📬 Awaiting Dispatch',
    DELIVERED:        '✅ Delivered',
    RECEIVED:         '📦 Received in Warehouse',
    CANCELLED:        '❌ Cancelled',
  };

  const STATUS_BADGE = {
    DRAFT:            'status-pending',
    SENT_TO_SUPPLIER: 'status-created',
    DELIVERED:        'status-processed',
    RECEIVED:         'status-shipped',
    CANCELLED:        'status-cancelled',
  };

  const pendingCount = purchaseOrders.filter(po => po.status === 'SENT_TO_SUPPLIER').length;

  return (
    <div className="stack">
      <div className="dashboard-welcome">
        <h2>Welcome, <span className="accent">{user?.name}</span> 🚚</h2>
        <p className="role-badge supplier-badge">Supplier</p>
      </div>

      {error && <div className="error-notice">{error}</div>}
      {msg   && <div className="success-notice">{msg}</div>}

      {/* Quick stats */}
      <div className="card-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))' }}>
        <div className="card metric">
          <div className="metric-icon">📋</div>
          <h3>Total POs</h3>
          <p>{purchaseOrders.length}</p>
        </div>
        <div className="card metric">
          <div className="metric-icon">📬</div>
          <h3>Awaiting Dispatch</h3>
          <p style={{ color: pendingCount > 0 ? '#f59e0b' : undefined }}>{pendingCount}</p>
        </div>
        <div className="card metric">
          <div className="metric-icon">✅</div>
          <h3>Delivered</h3>
          <p>{purchaseOrders.filter(po => po.status === 'DELIVERED' || po.status === 'RECEIVED').length}</p>
        </div>
      </div>

      {/* Instruction banner */}
      <div className="info-banner">
        📌 When your goods are ready for dispatch, click <strong>Mark Delivered</strong> on the
        corresponding order. The warehouse staff will then receive and store the items.
      </div>

      {/* PO Table */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h3>Purchase Orders</h3>
          <button className="btn secondary" onClick={loadData}>🔄 Refresh</button>
        </div>

        {loading ? <p style={{ color: '#6b7280' }}>Loading…</p> : (
          <div className="table-wrap">
            <table className="app-table">
              <thead>
                <tr>
                  <th>PO Number</th>
                  <th>Order Date</th>
                  <th>Status</th>
                  <th>Total Amount</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {purchaseOrders.length === 0
                  ? <tr><td colSpan={5} className="empty-cell">No purchase orders assigned yet.</td></tr>
                  : purchaseOrders.map(po => (
                    <tr key={po.id}>
                      <td><strong>{po.purchaseOrderNumber ?? `PO-${po.id}`}</strong></td>
                      <td>{po.orderDate ?? '—'}</td>
                      <td>
                        <span className={`status-badge ${STATUS_BADGE[po.status] ?? 'status-pending'}`}>
                          {STATUS_LABEL[po.status] ?? po.status}
                        </span>
                      </td>
                      <td>₹{po.totalAmount ?? '0.00'}</td>
                      <td>
                        {po.status === 'SENT_TO_SUPPLIER' ? (
                          <button className="btn" onClick={() => handleDeliver(po.id)}>
                            Mark Delivered
                          </button>
                        ) : (
                          <span style={{ color: '#9ca3af', fontSize: 13 }}>—</span>
                        )}
                      </td>
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

export default SupplierDashboard;
