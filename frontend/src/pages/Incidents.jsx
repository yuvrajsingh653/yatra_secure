import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import SosAction from '../components/SosAction';
import { api, errorMessage } from '../api';

export default function Incidents() {
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({ incidentType: 'SAFETY', description: '', latitude: '', longitude: '', priority: 'MEDIUM' });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const load = () => api.get('/api/incidents/mine')
    .then(({ data }) => setRows(data))
    .catch((requestError) => setError(errorMessage(requestError)));

  useEffect(() => { load(); }, []);

  const submitReport = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    setBusy(true);
    const payload = {
      ...form,
      latitude: form.latitude === '' ? null : Number(form.latitude),
      longitude: form.longitude === '' ? null : Number(form.longitude),
    };
    try {
      await api.post('/api/incidents', payload);
      setMessage('Incident report submitted.');
      setForm((current) => ({ ...current, description: '' }));
      await load();
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setBusy(false);
    }
  };

  const cancel = async (id) => {
    setError('');
    try {
      await api.post(`/api/incidents/${id}/cancel`);
      await load();
    } catch (requestError) {
      setError(errorMessage(requestError));
    }
  };

  return <main className="dashboard">
    <section className="profile-area incidents-page">
      <div className="section-title">
        <div><span className="eyebrow">YOUR SAFETY</span><h1>Safety &amp; incident reports</h1></div>
        <Link className="secondary-link" to="/dashboard">Back to dashboard</Link>
      </div>
      <p className="page-intro">Report a non-emergency concern or send an SOS alert to YatraSecure administrators.</p>
      {error && <div className="notice error" role="alert">{error}</div>}
      {message && <div className="notice success" role="status">{message}</div>}

      <section className="sos-panel" aria-labelledby="sos-heading">
        <div><span className="eyebrow">URGENT ASSISTANCE</span><h2 id="sos-heading">Need help right now?</h2><p>Your alert is saved for authorized administrators. GPS is optional.</p></div>
        <SosAction onCreated={load} />
      </section>

      <section className="report-layout">
        <form className="auth-form report-form" onSubmit={submitReport}>
          <div><span className="eyebrow">NON-EMERGENCY</span><h2>Submit a report</h2></div>
          <label>Incident type<select value={form.incidentType} onChange={(event) => setForm({ ...form, incidentType: event.target.value })}><option>SAFETY</option><option>MEDICAL</option><option>THEFT</option><option>OTHER</option></select></label>
          <label>Description<textarea required maxLength="2000" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} /></label>
          <label>Priority<select value={form.priority} onChange={(event) => setForm({ ...form, priority: event.target.value })}><option>LOW</option><option>MEDIUM</option><option>HIGH</option></select></label>
          <div className="coordinate-fields">
            <label>Latitude (optional)<input type="number" step="any" value={form.latitude} onChange={(event) => setForm({ ...form, latitude: event.target.value })} /></label>
            <label>Longitude (optional)<input type="number" step="any" value={form.longitude} onChange={(event) => setForm({ ...form, longitude: event.target.value })} /></label>
          </div>
          <button disabled={busy} className="primary-btn">{busy ? 'Submitting…' : 'Submit incident report'}</button>
        </form>

        <section className="recent-reports" aria-labelledby="reports-heading">
          <div className="section-title"><h2 id="reports-heading">Recent reports</h2><span>{rows.length} total</span></div>
          {rows.length === 0 ? <div className="empty-state">Your submitted reports will appear here.</div> : rows.map((incident) => <article className="incident-card" key={incident.id}>
            <div className="incident-card-heading"><strong>{incident.incidentType === 'SOS' ? 'SOS alert' : incident.incidentType}</strong><span className={`incident-status status-${incident.status.toLowerCase()}`}>{incident.status.replace('_', ' ')}</span></div>
            <p>{incident.description}</p>
            <small>{incident.priority} · {new Date(incident.createdAt).toLocaleString()}</small>
            {incident.latitude != null && <p className="incident-location">Location: {incident.latitude}, {incident.longitude}</p>}
            {incident.status === 'OPEN' && <button className="text-button" type="button" onClick={() => cancel(incident.id)}>Cancel report</button>}
          </article>)}
        </section>
      </section>
    </section>
  </main>;
}
