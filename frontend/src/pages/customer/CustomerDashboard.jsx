import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { cancelOrder } from '../../api/orderApi';
import { getOrders } from '../../api/orderApi';
import { getProducts } from '../../api/inventoryApi';
import { getApiErrorMessage } from '../../services/apiError';
import apiClient from '../../api/apiClient';

function CustomerDashboard() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [products, setProducts] = useState([]);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const loadData = async () => {
    try {
      const [allOrders, prods] = await Promise.all([getOrders(), getProducts()]);
      setOrders(allOrders.filter(o => !o.customer || o.customer?.id === user?.id));
      setProducts(prods);
      if (prods.length > 0 && !selectedProductId) setSelectedProductId(String(prods[0].id));
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  useEffect(() => { loadData(); }, []);

  const selectedProduct = products.find(p => String(p.id) === String(selectedProductId));
  const TOTAL_ESTIMATE = selectedProduct
    ? `Estimated Total: ₹${(selectedProduct.unitPrice * Number(quantity || 0)).toFixed(2)}`
    : '';

  const onPlaceOrder = async (e) => {
    e.preventDefault();
    setMsg(''); setError('');
    if (!selectedProductId) { setError('Please select a product.'); return; }
    setLoading(true);
    try {
      await apiClient.post('/orders', {
        customerId: user.id,
        items: [{
          product: { id: Number(selectedProductId) },
          quantity: Number(quantity),
          unitPrice: selectedProduct?.unitPrice ?? 0
        }]
      });
      setMsg('✅ Order placed successfully! You will receive a confirmation shortly.');
      setQuantity(1);
      await loadData();
    } catch (err) { setError(getApiErrorMessage(err)); }
    finally { setLoading(false); }
  };

  const onCancel = async (id) => {
    if (!window.confirm('Cancel this order?')) return;
    setMsg(''); setError('');
    try {
      await cancelOrder(id);
      setMsg('Order cancelled.');
      loadData();
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  // ── Status display maps ──────────────────────────────────────────────────
  const STATUS_BADGE = {
    CREATED:       'status-created',
    PENDING_STOCK: 'status-pending-stock',
    PROCESSED:     'status-processed',
    SHIPPED:       'status-shipped',
    DELIVERED:     'status-processed',
    CANCELLED:     'status-cancelled',
  };

  const STATUS_LABEL = {
    CREATED:       '🟡 Pending',
    PENDING_STOCK: '⏳ Out of Stock',
    PROCESSED:     '⚙️ Processing',
    SHIPPED:       '🚚 Shipped',
    DELIVERED:     '✅ Delivered',
    CANCELLED:     '❌ Cancelled',
  };

  const hasPendingStock = orders.some(o => o.status === 'PENDING_STOCK');

  return (
    <div className="stack">
      <div className="dashboard-welcome">
        <h2>Welcome, <span className="accent">{user?.name}</span> 🛒</h2>
        <p className="role-badge customer-badge">Customer</p>
      </div>

      {error && <div className="error-notice">{error}</div>}
      {msg   && <div className="success-notice">{msg}</div>}

      {/* Out-of-stock banner */}
      {hasPendingStock && (
        <div className="info-banner">
          ⏳ One or more of your orders is currently waiting for stock to be replenished.
          You will be notified and your order will resume automatically once stock is available.
        </div>
      )}

      <div className="split" style={{ gridTemplateColumns: '1fr 1.6fr' }}>

        {/* ── Place New Order ── */}
        <div className="card">
          <h3>🛒 Place New Order</h3>
          <p className="hint-text">Select a product and enter the quantity you need.</p>

          <form className="stack" onSubmit={onPlaceOrder}>
            <div className="field">
              <label htmlFor="product-select">Product</label>
              <select
                id="product-select"
                value={selectedProductId}
                onChange={(e) => setSelectedProductId(e.target.value)}
                className="field-select"
                required
              >
                <option value="">-- Choose a product --</option>
                {products.map(p => (
                  <option key={p.id} value={p.id}>
                    {p.name} — ₹{p.unitPrice}
                  </option>
                ))}
              </select>
            </div>

            {selectedProduct && (
              <div className="product-preview">
                <span>📦 {selectedProduct.name}</span>
                <span>Category: {selectedProduct.category ?? 'General'}</span>
                <span>Unit Price: <strong>₹{selectedProduct.unitPrice}</strong></span>
              </div>
            )}

            <div className="field">
              <label htmlFor="qty-input">Quantity</label>
              <input
                id="qty-input"
                type="number"
                min="1"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                required
              />
            </div>

            {TOTAL_ESTIMATE && (
              <p className="estimate-text">{TOTAL_ESTIMATE}</p>
            )}

            <button type="submit" className="btn" disabled={loading}>
              {loading ? 'Placing…' : 'Place Order'}
            </button>
          </form>
        </div>

        {/* ── My Orders ── */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3>📋 My Orders</h3>
            <button className="btn secondary" onClick={loadData}>🔄 Refresh</button>
          </div>
          <div className="table-wrap">
            <table className="app-table">
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Status</th>
                  <th>Total</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {orders.length === 0 ? (
                  <tr><td colSpan={4} className="empty-cell">No orders yet. Place your first order!</td></tr>
                ) : orders.map(o => (
                  <tr key={o.id}
                    style={o.status === 'PENDING_STOCK' ? { background: '#fffbeb' } : undefined}>
                    <td><strong>{o.orderNumber ?? `#${o.id}`}</strong></td>
                    <td>
                      <span className={`status-badge ${STATUS_BADGE[o.status] ?? 'status-pending'}`}>
                        {STATUS_LABEL[o.status] ?? o.status}
                      </span>
                      {o.status === 'PENDING_STOCK' && (
                        <div style={{ fontSize: 11, color: '#92400e', marginTop: 4 }}>
                          Will resume automatically when restocked.
                        </div>
                      )}
                    </td>
                    <td>₹{o.totalAmount ?? '0.00'}</td>
                    <td>
                      {o.status === 'CREATED' && (
                        <button className="btn danger" onClick={() => onCancel(o.id)}>
                          Cancel
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CustomerDashboard;
