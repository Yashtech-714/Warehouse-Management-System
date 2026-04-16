import { useEffect, useState } from 'react';
import { getStorageLocations, createStorageLocation, updateStorageLocation } from '../api/storageLocationApi';
import { getApiErrorMessage } from '../services/apiError';

const EMPTY = { code: '', zone: '', aisle: '', rack: '', bin: '' };

function StorageLocations() {
  const [locations, setLocations] = useState([]);
  const [form, setForm] = useState(EMPTY);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async () => {
    try { setLocations(await getStorageLocations()); }
    catch (err) { setError(getApiErrorMessage(err)); }
  };

  useEffect(() => { load(); }, []);

  const onChange = (e) => setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const startEdit = (loc) => {
    setEditingId(loc.id);
    setForm({ code: loc.code, zone: loc.zone ?? '', aisle: loc.aisle ?? '', rack: loc.rack ?? '', bin: loc.bin ?? '' });
    setError(''); setSuccess('');
  };

  const cancelEdit = () => { setEditingId(null); setForm(EMPTY); };

  const onSubmit = async (e) => {
    e.preventDefault(); setError(''); setSuccess('');
    try {
      if (editingId) {
        await updateStorageLocation(editingId, form);
        setSuccess('Location updated.');
        setEditingId(null);
      } else {
        await createStorageLocation(form);
        setSuccess('Location created.');
      }
      setForm(EMPTY); await load();
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  return (
    <div className="stack">
      {error && <div className="error-notice">{error}</div>}
      {success && <div className="success-notice">{success}</div>}

      <div className="card">
        <h3>{editingId ? '✏️ Edit Location' : '➕ Create Storage Location'}</h3>
        <form className="stack" onSubmit={onSubmit}>
          <div className="split">
            {[
              { name: 'code', label: 'Code (e.g. A1-R2-B3)', required: true },
              { name: 'zone', label: 'Zone' },
              { name: 'aisle', label: 'Aisle' },
              { name: 'rack', label: 'Rack' },
              { name: 'bin', label: 'Bin' },
            ].map(f => (
              <div className="field" key={f.name}>
                <label>{f.label}{f.required ? ' *' : ''}</label>
                <input name={f.name} value={form[f.name]} onChange={onChange} required={!!f.required} />
              </div>
            ))}
          </div>
          <div className="actions">
            <button type="submit" className="btn">{editingId ? 'Update' : 'Create Location'}</button>
            {editingId && <button type="button" className="btn secondary" onClick={cancelEdit}>Cancel</button>}
          </div>
        </form>
      </div>

      <div className="card">
        <h3>Warehouse Locations</h3>
        <div className="table-wrap">
          <table className="app-table">
            <thead>
              <tr><th>Code</th><th>Zone</th><th>Aisle</th><th>Rack</th><th>Bin</th><th>Action</th></tr>
            </thead>
            <tbody>
              {locations.length === 0 ? (
                <tr><td colSpan={6} className="empty-cell">No storage locations yet.</td></tr>
              ) : locations.map(loc => (
                <tr key={loc.id}>
                  <td><strong>{loc.code}</strong></td>
                  <td>{loc.zone ?? '—'}</td>
                  <td>{loc.aisle ?? '—'}</td>
                  <td>{loc.rack ?? '—'}</td>
                  <td>{loc.bin ?? '—'}</td>
                  <td><button className="btn" onClick={() => startEdit(loc)}>Edit</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default StorageLocations;
