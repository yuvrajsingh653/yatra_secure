import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, errorMessage } from '../api';

export default function AdminDashboard() {
  const [count, setCount] = useState(null);
  const [tourists, setTourists] = useState([]);
  const [openSos, setOpenSos] = useState([]);
  const [busy, setBusy] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      api.get('/api/admin/dashboard'),
      api.get('/api/admin/tourists'),
      api.get('/api/incidents/admin', { params: { status: 'OPEN' } }),
    ]).then(([summary, list, incidents]) => {
      setCount(summary.data.touristCount);
      setTourists(list.data);
      setOpenSos(incidents.data.filter((incident) => incident.incidentType === 'SOS'));
    }).catch((requestError) => setError(errorMessage(requestError))).finally(() => setBusy(false));
  }, []);

  return <main className="dashboard">
    <section className="profile-area admin-dashboard-page">
      <div className="section-title"><div><span className="eyebrow">CONTROL CENTER</span><h1>Admin dashboard</h1></div><span className="status-pill">ADMIN</span></div>
      {error && <div className="notice error" role="alert">{error}</div>}
      {busy ? <div className="profile-card loading-card">Loading admin data…</div> : <>
        <div className="admin-stat-grid">
          <article className="admin-stat-card"><span className="detail-label">REGISTERED TOURISTS</span><strong>{count}</strong></article>
          <article className={`admin-stat-card ${openSos.length ? 'admin-sos-stat' : ''}`}><span className="detail-label">OPEN SOS ALERTS</span><strong>{openSos.length}</strong><Link to="/admin/incidents">Review incident queue →</Link></article>
        </div>

        <section className="admin-sos-section">
          <div className="recent-panel-heading"><div><span className="eyebrow">URGENT IN-APP REPORTS</span><h2>Open SOS alerts</h2></div><Link className="secondary-link" to="/admin/incidents">Manage all incidents</Link></div>
          {openSos.length === 0 ? <div className="empty-state">There are no open SOS alerts.</div> : <div className="admin-sos-list">{openSos.slice(0, 5).map((incident) => <article className="admin-sos-row" key={incident.id}>
            <span className="dashboard-sos-icon" aria-hidden="true">!</span>
            <div><strong>{incident.touristName} · Tourist #{incident.touristId}</strong><p>{incident.description}</p><small>{new Date(incident.createdAt).toLocaleString()} · {incident.priority}</small></div>
            <span className="incident-status status-open">{incident.status}</span>
          </article>)}</div>}
          <p className="muted-line">Alerts are in-app records. No emergency service, SMS, or email dispatch is configured.</p>
        </section>

        <section className="admin-tourist-section">
          <div className="recent-panel-heading"><div><span className="eyebrow">REGISTERED ACCOUNTS</span><h2>Tourists</h2></div><span className="muted-line">{tourists.length} accounts</span></div>
          {tourists.length === 0 ? <div className="empty-state">No tourists registered yet.</div> : <div className="admin-table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Role</th></tr></thead><tbody>{tourists.map((tourist) => <tr key={tourist.id}><td>{tourist.name}</td><td>{tourist.email}</td><td>{tourist.role}</td></tr>)}</tbody></table></div>}
        </section>
      </>}
    </section>
  </main>;
}
