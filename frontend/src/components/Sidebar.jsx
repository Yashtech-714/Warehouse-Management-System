function Sidebar({ items, activePage, onSelect, role }) {
  const roleColors = {
    MANAGER:  '#6366f1',
    STAFF:    '#0ea5e9',
    CUSTOMER: '#10b981',
    SUPPLIER: '#f59e0b',
  };

  const accent = roleColors[role] ?? '#6366f1';

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="sidebar-logo">WMS</span>
        {role && (
          <span className="sidebar-role-tag" style={{ background: accent }}>
            {role}
          </span>
        )}
      </div>
      <nav className="sidebar-nav">
        {items.map((item) => (
          <button
            key={item.key}
            className={`sidebar-link ${activePage === item.key ? 'active' : ''}`}
            style={activePage === item.key ? { borderLeft: `3px solid ${accent}` } : {}}
            onClick={() => onSelect(item.key)}
          >
            {item.label}
          </button>
        ))}
      </nav>
    </aside>
  );
}

export default Sidebar;
