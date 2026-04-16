import { useEffect, useState } from 'react';
import { getProducts, createProduct, updateProduct, deactivateProduct } from '../api/inventoryApi';
import { getApiErrorMessage } from '../services/apiError';

const EMPTY_FORM = { sku: '', name: '', description: '', unitPrice: '', category: '' };

function Products() {
  const [products, setProducts] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);

  const load = async () => {
    try {
      setError('');
      setProducts(await getProducts());
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  useEffect(() => { load(); }, []);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const startEdit = (p) => {
    setEditingId(p.id);
    setForm({ sku: p.sku, name: p.name, description: p.description ?? '', unitPrice: p.unitPrice, category: p.category ?? '' });
    setError('');
    setSuccess('');
  };

  const cancelEdit = () => { setEditingId(null); setForm(EMPTY_FORM); };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      const payload = { ...form, unitPrice: Number(form.unitPrice) };
      if (editingId) {
        await updateProduct(editingId, payload);
        setSuccess('Product updated.');
        setEditingId(null);
      } else {
        await createProduct(payload);
        setSuccess('Product created.');
      }
      setForm(EMPTY_FORM);
      await load();
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  const onDeactivate = async (id) => {
    if (!window.confirm('Deactivate this product? It will no longer appear in orders.')) return;
    try {
      await deactivateProduct(id);
      setSuccess('Product deactivated.');
      await load();
    } catch (err) { setError(getApiErrorMessage(err)); }
  };

  return (
    <div className="stack">
      {error && <div className="error-notice">{error}</div>}
      {success && <div className="success-notice">{success}</div>}

      <div className="card">
        <h3>{editingId ? '✏️ Edit Product' : '➕ Create Product'}</h3>
        <form className="stack" onSubmit={onSubmit}>
          <div className="split">
            {[
              { name: 'sku', label: 'SKU', required: true },
              { name: 'name', label: 'Name', required: true },
              { name: 'unitPrice', label: 'Unit Price', type: 'number' },
              { name: 'category', label: 'Category' },
            ].map(f => (
              <div className="field" key={f.name}>
                <label>{f.label}</label>
                <input
                  name={f.name}
                  type={f.type ?? 'text'}
                  value={form[f.name]}
                  onChange={onChange}
                  required={f.required}
                  min={f.type === 'number' ? 0 : undefined}
                  step={f.type === 'number' ? '0.01' : undefined}
                />
              </div>
            ))}
          </div>
          <div className="field">
            <label>Description</label>
            <input name="description" value={form.description} onChange={onChange} />
          </div>
          <div className="actions">
            <button type="submit" className="btn">{editingId ? 'Update Product' : 'Create Product'}</button>
            {editingId && <button type="button" className="btn secondary" onClick={cancelEdit}>Cancel</button>}
          </div>
        </form>
      </div>

      <div className="card">
        <h3>Product Catalog</h3>
        <div className="table-wrap">
          <table className="app-table">
            <thead>
              <tr><th>SKU</th><th>Name</th><th>Price</th><th>Category</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {products.length === 0 ? (
                <tr><td colSpan={6} className="empty-cell">No active products.</td></tr>
              ) : products.map(p => (
                <tr key={p.id}>
                  <td>{p.sku}</td>
                  <td>{p.name}</td>
                  <td>₹{p.unitPrice}</td>
                  <td>{p.category ?? '—'}</td>
                  <td><span className="status-badge status-processed">Active</span></td>
                  <td>
                    <div className="actions">
                      <button className="btn" onClick={() => startEdit(p)}>Edit</button>
                      <button className="btn danger" onClick={() => onDeactivate(p.id)}>Deactivate</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default Products;
