import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, errorMessage } from '../api';

const statuses = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED'];

export default function AdminIncidents() {
  const [rows, setRows] = useState([]);
  const [filter, setFilter] = useState('OPEN');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [updating, setUpdating] = useState(null);

  const load = () => api.get('/api/incidents/admin', { params: filter ? { status: filter } : {} })
    .then(({ data }) => setRows(data))
    .catch((requestError) => setError(errorMessage(requestError)));

  useEffect(() => { load(); }, [filter]);

  const update = async (incident, status) => {
    setError('');
    setNotice('');
    setUpdating(incident.id);
    try {
      await api.patch(`/api/incidents/admin/${incident.id}`, { status, assignedTo: incident.assignedTo || null });
      setNotice(`Incident #${incident.id} status updated to ${status.replace('_', ' ')}.`);
      await load();
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setUpdating(null);
    }
  };

  return <main className="dashboard">
    <section className="profile-area admin-incidents-page">
      <div className="section-title"><div><span className="eyebrow">ADMIN CONTROL CENTER</span><h1>Incident management</h1></div><Link className="secondary-link" to="/admin">Back to dashboard</Link></div>
      <p className="page-intro">Review tourist reports and update their status. SOS alerts are stored in-app; the system does not dispatch emergency services.</p>
      <div className="incident-filter-row"><label htmlFor="incident-filter">Filter status</label><select id="incident-filter" value={filter} onChange={(event) => setFilter(event.target.value)}><option value="">All statuses</option>{statuses.map((status) => <option key={status} value={status}>{status.replace('_', ' ')}</option>)}</select></div>
      {error && <div className="notice error" role="alert">{error}</div>}
      {notice && <div className="notice success" role="status">{notice}</div>}
      {rows.length === 0 ? <div className="empty-state">No incidents match this filter.</div> : <div className="admin-incident-list">{rows.map((incident) => <article className={`admin-incident-card ${incident.incidentType === 'SOS' ? 'admin-incident-sos' : ''}`} key={incident.id}>
        <div className="incident-card-heading"><div><span className="eyebrow">{incident.incidentType === 'SOS' ? 'URGENT IN-APP ALERT' : 'TOURIST REPORT'}</span><h2>#{incident.id} · {incident.incidentType === 'SOS' ? 'SOS alert' : incident.incidentType}</h2></div><span className={statusClass(incident.status)}>{incident.status.replace('_', ' ')}</span></div>
        <p className="admin-incident-description">{incident.description}</p>
        <div className="admin-incident-meta"><span><strong>Tourist</strong> {incident.touristName} · #{incident.touristId}</span><span><strong>Priority</strong> {incident.priority}</span><span><strong>Received</strong> {new Date(incident.createdAt).toLocaleString()}</span></div>
        {incident.latitude != null && <p className="incident-location">Location: {incident.latitude}, {incident.longitude} · <a target="_blank" rel="noreferrer" href={`https://www.openstreetmap.org/?mlat=${incident.latitude}&mlon=${incident.longitude}#map=16/${incident.latitude}/${incident.longitude}`}>Open map</a></p>}
        <label className="admin-status-control">Update status<select disabled={updating === incident.id} value={incident.status} onChange={(event) => update(incident, event.target.value)}>{statuses.map((status) => <option key={status} value={status}>{status.replace('_', ' ')}</option>)}</select>{updating === incident.id && <span>Saving…</span>}</label>
      </article>)}</div>}
    </section>
  </main>;
}

function statusClass(status) {
  return `incident-status status-${status.toLowerCase().replaceAll('_', '-')}`;
}
