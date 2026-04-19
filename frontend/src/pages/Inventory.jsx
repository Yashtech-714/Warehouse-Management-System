import { getInventory } from '../api/inventoryApi';
import { getApiErrorMessage } from '../services/apiError';
import { useEffect, useState } from 'react';

/**
 * Inventory — read-only view of current stock.
 * Stock is ONLY updated via PO receiving (Staff) or order processing (Staff).
 */
function Inventory() {
  const [inventory, setInventory] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      setLoading(true);
      setInventory(await getInventory());
    } catch (err) { setError(getApiErrorMessage(err)); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  return (
    <div className="stack">
      {error && <div className="error-notice">{error}</div>}

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h3>📦 Inventory Records</h3>
          <button className="btn secondary" onClick={load}>🔄 Refresh</button>
        </div>
        <p className="hint-text">
          Stock levels update automatically when Staff receives POs or processes customer orders.
        </p>
        {loading ? <p style={{ color: '#6b7280' }}>Loading…</p> : (
          <div className="table-wrap">
            <table className="app-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
                  <th>Location</th>
                  <th>Qty</th>
                  <th>Reorder At</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {inventory.length === 0
                  ? <tr><td colSpan={6} className="empty-cell">No inventory records yet.</td></tr>
                  : inventory.map(item => (
                    <tr key={item.id}>
                      <td>{item.product?.name ?? '—'}</td>
                      <td>{item.product?.sku ?? '—'}</td>
                      <td>{item.storageLocation?.code ?? <span style={{ color: '#f59e0b' }}>Unassigned</span>}</td>
                      <td><strong>{item.quantity}</strong></td>
                      <td>{item.reorderLevel}</td>
                      <td>
                        <span className={`status-badge ${
                          item.quantity === 0
                            ? 'status-cancelled'
                            : item.quantity <= item.reorderLevel
                              ? 'status-pending'
                              : 'status-processed'
                        }`}>
                          {item.quantity === 0
                            ? '❌ Out of Stock'
                            : item.quantity <= item.reorderLevel
                              ? '⚠️ Low'
                              : '✅ OK'}
                        </span>
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

export default Inventory;
