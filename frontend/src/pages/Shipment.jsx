import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getShipments, markShipmentShipped, markShipmentDelivered, trackShipment } from '../api/shipmentApi';
import { getApiErrorMessage } from '../services/apiError';

/**
 * Shipment page — role-aware.
 *
 * ALL ROLES  : view shipments list, track individual shipment
 * STAFF only : "Mark as Shipped" (CREATED → SHIPPED)
 *              "Mark as Delivered" (SHIPPED → DELIVERED)
 *
 * Manager and Customer: read-only.
 * Shipments are created automatically when Staff processes an order.
 */
function Shipment() {
  const { user } = useAuth();
  const role = user?.role ?? '';

  const [shipments, setShipments] = useState([]);
  const [tracked, setTracked]     = useState(null);
  const [error, setError]         = useState('');
  const [msg, setMsg]             = useState('');
  const [loading, setLoading]     = useState(true);

  const load = async () => {
    try {
      setLoading(true);
      setShipments(await getShipments());
    } catch (err) { setError(getApiErrorMessage(err)); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const withFeedback = async (fn, successMsg) => {
    setError(''); setMsg('');
    try { await fn(); setMsg(successMsg); load(); }
    catch (err) { setError(getApiErrorMessage(err)); }
  };

  const onTrack = async (id) => {
    setError('');
    try { setTracked(await trackShipment(id)); }
    catch (err) { setError(getApiErrorMessage(err)); }
  };

  const STATUS_BADGE = {
    CREATED:   'status-created',
    SHIPPED:   'status-shipped',
    DELIVERED: 'status-processed',
  };

  // Human-friendly labels
  const STATUS_LABEL = {
    CREATED:   '📦 Created',
    SHIPPED:   '🚚 Shipped',
    DELIVERED: '✅ Delivered',
  };

  return (
    <div className="stack">
      {error && <div className="error-notice">{error}</div>}
      {msg   && <div className="success-notice">{msg}</div>}

      {/* Staff instruction banner */}
      {role === 'STAFF' && (
        <div className="info-banner">
          📋 Shipments are auto-created when you process an order.
          Click <strong>Mark Shipped</strong> → <strong>Mark Delivered</strong> to complete the lifecycle.
        </div>
      )}

      {/* Tracked shipment detail */}
      {tracked && (
        <div className="card">
          <h3>📍 Shipment Tracking</h3>
          <div className="split">
            <div><label className="hint-text">Shipment #</label>
              <p><strong>{tracked.shipmentNumber ?? tracked.id}</strong></p></div>
            <div><label className="hint-text">Status</label>
              <p><span className={`status-badge ${STATUS_BADGE[tracked.status] ?? 'status-pending'}`}>
                {STATUS_LABEL[tracked.status] ?? tracked.status}
              </span></p></div>
            <div><label className="hint-text">Destination</label>
              <p>{tracked.shippingAddress ?? '—'}</p></div>
            <div><label className="hint-text">Shipped At</label>
              <p>{tracked.shippedAt ? new Date(tracked.shippedAt).toLocaleString() : '—'}</p></div>
          </div>
          <button className="btn secondary" style={{ marginTop: 12 }} onClick={() => setTracked(null)}>
            ✕ Close
          </button>
        </div>
      )}

      {/* Shipments table */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h3>🚚 Shipments</h3>
          {role === 'MANAGER' && (
            <span className="hint-text" style={{ fontStyle: 'italic' }}>👁️ Read-only monitoring view</span>
          )}
          <button className="btn secondary" onClick={load}>🔄 Refresh</button>
        </div>

        {loading ? <p style={{ color: '#6b7280' }}>Loading…</p> : (
          <div className="table-wrap">
            <table className="app-table">
              <thead>
                <tr>
                  <th>Shipment #</th>
                  <th>Order</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {shipments.length === 0
                  ? <tr><td colSpan={4} className="empty-cell">No shipments yet.</td></tr>
                  : shipments.map(s => (
                    <tr key={s.id}>
                      <td><strong>{s.shipmentNumber ?? `SHP-${s.id}`}</strong></td>
                      <td>{s.order?.orderNumber ?? s.order?.id ?? '—'}</td>
                      <td>
                        <span className={`status-badge ${STATUS_BADGE[s.status] ?? 'status-pending'}`}>
                          {STATUS_LABEL[s.status] ?? s.status}
                        </span>
                      </td>
                      <td>
                        <div className="actions">
                          {/* Track — all roles */}
                          <button className="btn secondary" onClick={() => onTrack(s.id)}>
                            📍 Track
                          </button>

                          {/* Mark Shipped — STAFF only, when CREATED */}
                          {role === 'STAFF' && s.status === 'CREATED' && (
                            <button
                              className="btn"
                              onClick={() => withFeedback(
                                () => markShipmentShipped(s.id),
                                `✅ Shipment #${s.shipmentNumber ?? s.id} marked as SHIPPED.`
                              )}
                            >
                              🚚 Mark Shipped
                            </button>
                          )}

                          {/* Mark Delivered — STAFF only, when SHIPPED */}
                          {role === 'STAFF' && s.status === 'SHIPPED' && (
                            <button
                              className="btn"
                              style={{ background: '#10b981' }}
                              onClick={() => withFeedback(
                                () => markShipmentDelivered(s.id),
                                `✅ Shipment #${s.shipmentNumber ?? s.id} delivered. Order complete!`
                              )}
                            >
                              ✅ Mark Delivered
                            </button>
                          )}
                        </div>
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

export default Shipment;
