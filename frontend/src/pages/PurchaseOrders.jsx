import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  createPurchaseOrder,
  getPurchaseOrders,
} from '../api/purchaseOrderApi';
import { getProducts } from '../api/inventoryApi';
import { getSuppliers } from '../api/supplierApi';
import { getApiErrorMessage } from '../services/apiError';

/**
 * PurchaseOrders — Manager's PO management page.
 *
 * Create form uses dropdowns for Product and Supplier (no raw IDs).
 * Unit price is auto-filled from the selected product (read-only).
 * Only Quantity is manually entered.
 *
 * SOLID SRP: UI only; delegates persistence to purchaseOrderService via API.
 */
function PurchaseOrders() {
  const { user } = useAuth();
  const role = user?.role ?? '';

  const [purchaseOrders, setPurchaseOrders]     = useState([]);
  const [products, setProducts]                 = useState([]);
  const [suppliers, setSuppliers]               = useState([]);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [selectedSupplierId, setSelectedSupplierId] = useState('');
  const [quantity, setQuantity]                 = useState(1);
  const [error, setError]                       = useState('');
  const [msg, setMsg]                           = useState('');
  const [loading, setLoading]                   = useState(false);

  const load = async () => {
    try {
      const [pos, prods, sups] = await Promise.all([
        getPurchaseOrders(),
        getProducts(),
        getSuppliers(),
      ]);
      setPurchaseOrders(pos ?? []);
      setProducts(prods ?? []);
      setSuppliers(sups ?? []);
      // Set defaults
      if (prods?.length > 0 && !selectedProductId) setSelectedProductId(String(prods[0].id));
      if (sups?.length  > 0 && !selectedSupplierId) setSelectedSupplierId(String(sups[0].id));
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  useEffect(() => { load(); }, []);

  // Derive selected entities for preview / auto-fill
  const selectedProduct  = products.find(p => String(p.id) === String(selectedProductId));
  const selectedSupplier = suppliers.find(s => String(s.id) === String(selectedSupplierId));

  const totalEstimate = selectedProduct
    ? (selectedProduct.unitPrice * Number(quantity || 0)).toFixed(2)
    : '0.00';

  const onSubmit = async (e) => {
    e.preventDefault(); setError(''); setMsg(''); setLoading(true);
    try {
      await createPurchaseOrder({
        supplierId: Number(selectedSupplierId),
        items: [{
          product:   { id: Number(selectedProductId) },
          quantity:  Number(quantity),
          unitPrice: selectedProduct?.unitPrice ?? 0,
        }],
      });
      setMsg('✅ Purchase Order created and sent to supplier.');
      setQuantity(1);
      await load();
    } catch (err) { setError(getApiErrorMessage(err)); }
    finally { setLoading(false); }
  };

  const STATUS_BADGE = {
    DRAFT:            'status-pending',
    SENT_TO_SUPPLIER: 'status-created',
    DELIVERED:        'status-processed',
    RECEIVED:         'status-shipped',
    CANCELLED:        'status-cancelled',
  };

  const STATUS_LABEL = {
    DRAFT:            '📝 Draft',
    SENT_TO_SUPPLIER: '📬 Sent to Supplier',
    DELIVERED:        '✅ Delivered',
    RECEIVED:         '📦 Received',
    CANCELLED:        '❌ Cancelled',
  };

  return (
    <div className="stack">
      {error && <div className="error-notice">{error}</div>}
      {msg   && <div className="success-notice">{msg}</div>}

      {/* ── Create PO Form — Manager only ── */}
      {role === 'MANAGER' && (
        <div className="card">
          <h3>📋 Create Purchase Order</h3>
          <p className="hint-text">
            Create a purchase order and it will be immediately sent to the supplier.
            Staff will receive the stock once the supplier marks it as delivered.
          </p>

          <form className="stack" onSubmit={onSubmit}>
            <div className="split">

              {/* Product Dropdown */}
              <div className="field">
                <label htmlFor="po-product">Product *</label>
                <select
                  id="po-product"
                  className="field-select"
                  value={selectedProductId}
                  onChange={(e) => setSelectedProductId(e.target.value)}
                  required
                >
                  <option value="">-- Select Product --</option>
                  {products.map(p => (
                    <option key={p.id} value={p.id}>
                      {p.name} — ₹{p.unitPrice}
                    </option>
                  ))}
                </select>
              </div>

              {/* Supplier Dropdown */}
              <div className="field">
                <label htmlFor="po-supplier">Supplier *</label>
                <select
                  id="po-supplier"
                  className="field-select"
                  value={selectedSupplierId}
                  onChange={(e) => setSelectedSupplierId(e.target.value)}
                  required
                >
                  <option value="">-- Select Supplier --</option>
                  {suppliers.map(s => (
                    <option key={s.id} value={s.id}>
                      {s.companyName ? `${s.companyName} (${s.name})` : s.name}
                    </option>
                  ))}
                </select>
              </div>

              {/* Quantity */}
              <div className="field">
                <label htmlFor="po-qty">Quantity *</label>
                <input
                  id="po-qty"
                  type="number"
                  min="1"
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                  required
                />
              </div>

              {/* Unit Price — auto-filled, read-only */}
              <div className="field">
                <label>Unit Price (auto-filled)</label>
                <input
                  type="text"
                  value={selectedProduct ? `₹${selectedProduct.unitPrice}` : '—'}
                  readOnly
                  className="field-input-readonly"
                />
              </div>

            </div>

            {/* Preview summary */}
            {selectedProduct && selectedSupplier && (
              <div className="po-preview">
                <div className="po-preview-row">
                  <span>📦 Product:</span>
                  <strong>{selectedProduct.name}</strong>
                </div>
                <div className="po-preview-row">
                  <span>🏭 Supplier:</span>
                  <strong>{selectedSupplier.companyName ?? selectedSupplier.name}</strong>
                </div>
                <div className="po-preview-row">
                  <span>💰 Estimated Total:</span>
                  <strong style={{ color: '#6366f1' }}>₹{totalEstimate}</strong>
                </div>
              </div>
            )}

            <div className="actions">
              <button type="submit" className="btn" disabled={loading}>
                {loading ? 'Creating…' : 'Create Purchase Order'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* ── PO List ── */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h3>Purchase Orders</h3>
          <button className="btn secondary" onClick={load}>🔄 Refresh</button>
        </div>

        <div className="table-wrap">
          <table className="app-table">
            <thead>
              <tr>
                <th>PO Number</th>
                <th>Supplier</th>
                <th>Status</th>
                <th>Total</th>
                {role === 'MANAGER' && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {purchaseOrders.length === 0
                ? <tr><td colSpan={role === 'MANAGER' ? 5 : 4} className="empty-cell">No purchase orders yet.</td></tr>
                : purchaseOrders.map(po => (
                  <tr key={po.id}>
                    <td><strong>{po.purchaseOrderNumber ?? `PO-${po.id}`}</strong></td>
                    <td>{po.supplier?.companyName ?? po.supplier?.name ?? '—'}</td>
                    <td>
                      <span className={`status-badge ${STATUS_BADGE[po.status] ?? 'status-pending'}`}>
                        {STATUS_LABEL[po.status] ?? po.status}
                      </span>
                    </td>
                    <td>₹{po.totalAmount ?? '0.00'}</td>
                    {role === 'MANAGER' && (
                      <td>
                        {/* Send to Supplier button removed — POs are sent automatically on creation */}
                        <span style={{ color: '#9ca3af', fontSize: 13 }}>—</span>
                      </td>
                    )}
                  </tr>
                ))
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default PurchaseOrders;
