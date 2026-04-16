import { useEffect, useState } from 'react';
import apiClient from '../api/apiClient';
import { getApiErrorMessage } from '../services/apiError';

function Reports() {
  const [tab, setTab] = useState('inventory');
  const [inventoryReport, setInventoryReport] = useState([]);
  const [orderReport, setOrderReport] = useState({});
  const [shipmentReport, setShipmentReport] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true); setError('');
    try {
      const [inv, ord, ship] = await Promise.all([
        apiClient.get('/reports/inventory'),
        apiClient.get('/reports/orders'),
        apiClient.get('/reports/shipments'),
      ]);
      setInventoryReport(inv.data);
      setOrderReport(ord.data);
      setShipmentReport(ship.data);
    } catch (err) { setError(getApiErrorMessage(err)); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const TABS = [
    { key: 'inventory', label: '🗄️ Inventory Summary' },
    { key: 'orders', label: '🛒 Order Distribution' },
    { key: 'shipments', label: '🚚 Shipment Tracking' },
  ];

  return (
    <div className="stack">
      {error && <div className="error-notice">{error}</div>}

      <div className="tab-bar">
        {TABS.map(t => (
          <button key={t.key} className={`tab-btn ${tab === t.key ? 'tab-active' : ''}`} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
        <button className="btn secondary" style={{ marginLeft: 'auto' }} onClick={load}>
          🔄 Refresh
        </button>
      </div>

      {loading && <p style={{ color: '#6b7280' }}>Loading report…</p>}

      {/* ── Inventory Summary ── */}
      {tab === 'inventory' && (
        <div className="card">
          <h3>Inventory Summary</h3>
          <div className="table-wrap">
            <table className="app-table">
              <thead>
                <tr><th>Product</th><th>SKU</th><th>Qty</th><th>Reorder At</th><th>Location</th><th>Status</th></tr>
              </thead>
              <tbody>
                {inventoryReport.length === 0
                  ? <tr><td colSpan={6} className="empty-cell">No inventory data.</td></tr>
                  : inventoryReport.map((row, i) => (
                    <tr key={i}>
                      <td>{row.product}</td>
                      <td>{row.sku}</td>
                      <td><strong>{row.quantity}</strong></td>
                      <td>{row.reorderLevel}</td>
                      <td>{row.location}</td>
                      <td>
                        <span className={`status-badge ${row.lowStock ? 'status-cancelled' : 'status-processed'}`}>
                          {row.lowStock ? '⚠️ Low Stock' : '✅ OK'}
                        </span>
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* ── Order Distribution ── */}
      {tab === 'orders' && (
        <div className="stack">
          <div className="card-grid">
            <div className="card metric">
              <div className="metric-icon">🛒</div>
              <h3>Total Orders</h3>
              <p>{orderReport.total ?? 0}</p>
            </div>
            {Object.entries(orderReport.distribution ?? {}).map(([status, count]) => (
              <div className="card metric" key={status}>
                <div className="metric-icon">
                  {status === 'CREATED' ? '🆕' : status === 'PROCESSED' ? '⚙️' : status === 'CANCELLED' ? '❌' : '📦'}
                </div>
                <h3>{status}</h3>
                <p>{count}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ── Shipment Tracking ── */}
      {tab === 'shipments' && (
        <div className="card">
          <h3>Shipment Tracking</h3>
          <div className="table-wrap">
            <table className="app-table">
              <thead>
                <tr><th>Shipment #</th><th>Status</th><th>Address</th><th>Shipped At</th><th>Est. Delivery</th></tr>
              </thead>
              <tbody>
                {shipmentReport.length === 0
                  ? <tr><td colSpan={5} className="empty-cell">No shipments yet.</td></tr>
                  : shipmentReport.map((s, i) => (
                    <tr key={i}>
                      <td>{s.shipmentNumber}</td>
                      <td><span className={`status-badge status-${(s.status ?? '').toLowerCase()}`}>{s.status}</span></td>
                      <td>{s.shippingAddress ?? '—'}</td>
                      <td>{s.shippedAt ? new Date(s.shippedAt).toLocaleString() : '—'}</td>
                      <td>{s.estimatedDelivery ?? '—'}</td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default Reports;
