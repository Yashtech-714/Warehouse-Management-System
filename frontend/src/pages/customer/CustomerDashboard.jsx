import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getOrders } from '../../api/orderApi';
import { getProducts } from '../../api/inventoryApi';
import { getApiErrorMessage } from '../../services/apiError';
import apiClient from '../../api/apiClient';

/**
 * CustomerDashboard — Place New Order only.
 *
 * "My Orders" view is handled by Orders.jsx (sidebar → My Orders page).
 * This component intentionally contains ONLY the order placement form.
 */
function CustomerDashboard() {
  const { user } = useAuth();
  const [products, setProducts]             = useState([]);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [quantity, setQuantity]             = useState(1);
  const [error, setError]                   = useState('');
  const [msg, setMsg]                       = useState('');
  const [loading, setLoading]               = useState(false);

  const loadProducts = async () => {
    try {
      const prods = await getProducts();
      setProducts(prods);
      if (prods.length > 0 && !selectedProductId) setSelectedProductId(String(prods[0].id));
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  useEffect(() => { loadProducts(); }, []);

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
      setMsg('✅ Order placed successfully! Check "My Orders" in the sidebar to track it.');
      setQuantity(1);
    } catch (err) { setError(getApiErrorMessage(err)); }
    finally { setLoading(false); }
  };

  return (
    <div className="stack">
      <div className="dashboard-welcome">
        <h2>Welcome, <span className="accent">{user?.name}</span> 🛒</h2>
        <p className="role-badge customer-badge">Customer</p>
      </div>

      {error && <div className="error-notice">{error}</div>}
      {msg   && <div className="success-notice">{msg}</div>}

      {/* ── Place New Order ── */}
      <div className="card" style={{ maxWidth: 480, margin: '0 auto', width: '100%' }}>
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

      <p className="hint-text" style={{ marginTop: 8, textAlign: 'center' }}>
        📋 To view and track your orders, click <strong>My Orders</strong> in the sidebar.
      </p>
    </div>
  );
}

export default CustomerDashboard;
