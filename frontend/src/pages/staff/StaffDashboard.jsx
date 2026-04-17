import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getOrders, processOrder } from '../../api/orderApi';
import { getInventory, assignToLocation } from '../../api/inventoryApi';
import { getPurchaseOrders, receivePurchaseOrder } from '../../api/purchaseOrderApi';
import { getStorageLocations } from '../../api/storageLocationApi';
import { getShipments } from '../../api/shipmentApi';
import { getApiErrorMessage } from '../../services/apiError';

function StaffDashboard() {
  const { user } = useAuth();
  const [orders, setOrders]               = useState([]);
  const [inventory, setInventory]         = useState([]);
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [locations, setLocations]         = useState([]);
  const [pendingShipments, setPendingShipments] = useState([]);
  const [activeTab, setActiveTab]         = useState('orders');
  const [msg, setMsg]                     = useState('');
  const [error, setError]                 = useState('');

  // Inventory operations state
  const [selectedItemId, setSelectedItemId]       = useState('');
  const [selectedLocationId, setSelectedLocationId] = useState('');
  const [receiveLocationId, setReceiveLocationId]   = useState('');

  const loadAll = async () => {
    try {
      const [o, inv, pos, locs, ships] = await Promise.all([
        getOrders(), getInventory(), getPurchaseOrders(), getStorageLocations(), getShipments()
      ]);
      setOrders(o.filter(order => order.status === 'CREATED'));
      setInventory(inv);
      setPurchaseOrders(pos.filter(po => po.status === 'DELIVERED'));
      setLocations(locs);
      // Shipments that still need Staff action
      setPendingShipments(ships.filter(s => s.status === 'CREATED' || s.status === 'SHIPPED'));
      if (locs.length > 0) {
        setSelectedLocationId(String(locs[0].id));
        setReceiveLocationId(String(locs[0].id));
      }
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  useEffect(() => { loadAll(); }, []);

  const handleProcess = async (id) => {
    setMsg(''); setError('');
    try {
      await processOrder(id);
      setMsg('✅ Order processed! Inventory deducted, shipment created.');
      loadAll();
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  const handleReceive = async (poId) => {
    if (!receiveLocationId) { setError('Select a storage location to receive stock.'); return; }
    setMsg(''); setError('');
    try {
      await receivePurchaseOrder(Number(poId), Number(receiveLocationId));
      setMsg('✅ Stock received and added to inventory.');
      loadAll();
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  const handleAssign = async (e) => {
    e.preventDefault(); setMsg(''); setError('');
    try {
      await assignToLocation(Number(selectedItemId), Number(selectedLocationId));
      setMsg('✅ Inventory item assigned to location.');
      loadAll();
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  // ── Metric cards data ────────────────────────────────────────────────────
  const metrics = [
    {
      label:   'Orders to Process',
      value:   orders.length,
      icon:    '🛒',
      tab:     'orders',
      urgent:  orders.length > 0,
      hint:    'CREATED orders waiting for you',
    },
    {
      label:   'POs to Receive',
      value:   purchaseOrders.length,
      icon:    '📦',
      tab:     'receive',
      urgent:  purchaseOrders.length > 0,
      hint:    'DELIVERED purchase orders to stock',
    },
    {
      label:   'Shipments Pending',
      value:   pendingShipments.length,
      icon:    '🚚',
      tab:     null,  // navigates to Shipments page, not a tab
      urgent:  pendingShipments.length > 0,
      hint:    'Awaiting SHIPPED or DELIVERED action',
    },
  ];

  const TABS = [
    { key: 'orders',    label: '🛒 Process Orders' },
    { key: 'receive',   label: '📦 Receive Stock' },
    { key: 'inventory', label: '🗄️ Inventory Ops' },
  ];

  return (
    <div className="stack">
      <div className="dashboard-welcome">
        <h2>Welcome, <span className="accent">{user?.name}</span> 📦</h2>
        <p className="role-badge staff-badge">Warehouse Staff</p>
      </div>

      {error && <div className="error-notice">{error}</div>}
      {msg   && <div className="success-notice">{msg}</div>}

      {/* ── Work Queue Summary ── */}
      <h3 className="section-title">Your Work Queue</h3>
      <div className="card-grid">
        {metrics.map(m => (
          <article
            key={m.label}
            className={`card metric ${m.urgent ? 'metric-urgent' : ''}`}
            style={{ cursor: m.tab ? 'pointer' : 'default' }}
            onClick={() => m.tab && setActiveTab(m.tab)}
            title={m.tab ? `Click to open ${m.label}` : undefined}
          >
            <div className="metric-icon">{m.icon}</div>
            <h3>{m.label}</h3>
            <p style={{ color: m.urgent ? '#ef4444' : '#6366f1', fontWeight: 700, fontSize: 28 }}>
              {m.value}
            </p>
            <span className="hint-text" style={{ fontSize: 11 }}>{m.hint}</span>
          </article>
        ))}
      </div>

      {/* ── Tabs ── */}
      <div className="tab-bar">
        {TABS.map(t => (
          <button
            key={t.key}
            className={`tab-btn ${activeTab === t.key ? 'tab-active' : ''}`}
            onClick={() => { setActiveTab(t.key); setMsg(''); setError(''); }}
          >{t.label}</button>
        ))}
      </div>

      {/* ── Process Orders ── */}
      {activeTab === 'orders' && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
            <h3>Orders to Process <span className="badge">{orders.length}</span></h3>
            <button className="btn secondary" onClick={loadAll}>🔄 Refresh</button>
          </div>
          <p className="hint-text">Processing an order checks + deducts inventory and creates a shipment automatically.</p>
          <div className="table-wrap">
            <table className="app-table">
              <thead><tr><th>Order #</th><th>Customer</th><th>Status</th><th>Total</th><th>Action</th></tr></thead>
              <tbody>
                {orders.length === 0
                  ? <tr><td colSpan={5} className="empty-cell">✅ No orders to process right now.</td></tr>
                  : orders.map(o => (
                    <tr key={o.id}>
                      <td><strong>{o.orderNumber}</strong></td>
                      <td>{o.customer?.name ?? '—'}</td>
                      <td><span className="status-badge status-created">{o.status}</span></td>
                      <td>₹{o.totalAmount}</td>
                      <td>
                        <button className="btn" onClick={() => handleProcess(o.id)}>
                          ⚙️ Process
                        </button>
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* ── Receive Stock ── */}
      {activeTab === 'receive' && (
        <div className="stack">
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
              <h3>Receive Stock from Delivered POs <span className="badge">{purchaseOrders.length}</span></h3>
              <button className="btn secondary" onClick={loadAll}>🔄 Refresh</button>
            </div>
            <p className="hint-text">Select a target storage location then click Receive to add items to inventory.</p>
            <div className="field" style={{ maxWidth: 300, marginBottom: 16 }}>
              <label>Target Storage Location</label>
              <select className="field-select" value={receiveLocationId} onChange={e => setReceiveLocationId(e.target.value)}>
                {locations.map(l => <option key={l.id} value={l.id}>{l.code}</option>)}
              </select>
            </div>
            <div className="table-wrap">
              <table className="app-table">
                <thead><tr><th>PO #</th><th>Supplier</th><th>Status</th><th>Action</th></tr></thead>
                <tbody>
                  {purchaseOrders.length === 0
                    ? <tr><td colSpan={4} className="empty-cell">✅ No delivered purchase orders to receive.</td></tr>
                    : purchaseOrders.map(po => (
                      <tr key={po.id}>
                        <td><strong>{po.purchaseOrderNumber}</strong></td>
                        <td>{po.supplier?.name ?? '—'}</td>
                        <td><span className="status-badge status-processed">{po.status}</span></td>
                        <td>
                          <button className="btn" onClick={() => handleReceive(po.id)}>
                            📥 Receive Stock
                          </button>
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* ── Inventory Ops ── */}
      {activeTab === 'inventory' && (
        <div className="stack">
          <div className="card">
            <h3>Assign / Move Inventory Item</h3>
            <p className="hint-text">Move an inventory item to a different storage location.</p>
            <form className="stack" onSubmit={handleAssign}>
              <div className="split">
                <div className="field">
                  <label>Inventory Item</label>
                  <select className="field-select" value={selectedItemId} onChange={e => setSelectedItemId(e.target.value)} required>
                    <option value="">-- select item --</option>
                    {inventory.map(i => (
                      <option key={i.id} value={i.id}>
                        {i.product?.name} (qty: {i.quantity}) @ {i.storageLocation?.code ?? 'Unassigned'}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="field">
                  <label>Target Location</label>
                  <select className="field-select" value={selectedLocationId} onChange={e => setSelectedLocationId(e.target.value)} required>
                    {locations.map(l => <option key={l.id} value={l.id}>{l.code}</option>)}
                  </select>
                </div>
              </div>
              <div className="actions">
                <button type="submit" className="btn">Assign / Move</button>
              </div>
            </form>
          </div>

          <div className="card">
            <h3>Current Inventory</h3>
            <div className="table-wrap">
              <table className="app-table">
                <thead><tr><th>Product</th><th>Qty</th><th>Reorder At</th><th>Location</th><th>Alert</th></tr></thead>
                <tbody>
                  {inventory.map(item => (
                    <tr key={item.id}>
                      <td>{item.product?.name}</td>
                      <td style={{ fontWeight: 600, color: item.quantity <= item.reorderLevel ? '#ef4444' : undefined }}>
                        {item.quantity}
                      </td>
                      <td>{item.reorderLevel}</td>
                      <td>{item.storageLocation?.code ?? <span style={{color:'#f59e0b'}}>Unassigned</span>}</td>
                      <td>{item.quantity <= item.reorderLevel ? '⚠️ Low' : '✅'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default StaffDashboard;
