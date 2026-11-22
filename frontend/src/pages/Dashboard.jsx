import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, errorMessage } from '../api';
import SosAction from '../components/SosAction';

const emptySummary = { touristId: null, sharing: null, zones: [], incidents: [] };

function statusClass(value = '') {
  return `incident-status status-${value.toLowerCase().replaceAll('_', '-')}`;
}

export default function Dashboard() {
  const [profile, setProfile] = useState(null);
  const [summary, setSummary] = useState(emptySummary);
  const [busy, setBusy] = useState(true);
  const [error, setError] = useState('');
  const [dataErrors, setDataErrors] = useState([]);

  const refreshIncidents = () => api.get('/api/incidents/mine').then(({ data }) => {
    setSummary((current) => ({ ...current, incidents: data }));
  }).catch((requestError) => setDataErrors((current) => [...current, `Recent reports: ${errorMessage(requestError)}`]));

  useEffect(() => {
    let active = true;
    Promise.allSettled([
      api.get('/api/auth/profile'),
      api.get('/api/tourist-id/me'),
      api.get('/api/location/sharing'),
      api.get('/api/safety-zones/active'),
      api.get('/api/incidents/mine'),
    ]).then((results) => {
      if (!active) return;
      const [profileResult, idResult, locationResult, zoneResult, incidentResult] = results;
      if (profileResult.status === 'fulfilled') setProfile(profileResult.value.data);
      else setError(errorMessage(profileResult.reason));

      const notices = [];
      if (idResult.status === 'fulfilled') setSummary((current) => ({ ...current, touristId: idResult.value.data }));
      else if (idResult.reason.response?.status !== 404) notices.push(`Digital ID: ${errorMessage(idResult.reason)}`);
      if (locationResult.status === 'fulfilled') setSummary((current) => ({ ...current, sharing: locationResult.value.data }));
      else notices.push(`Location status: ${errorMessage(locationResult.reason)}`);
      if (zoneResult.status === 'fulfilled') setSummary((current) => ({ ...current, zones: zoneResult.value.data }));
      else notices.push(`Safety zones: ${errorMessage(zoneResult.reason)}`);
      if (incidentResult.status === 'fulfilled') setSummary((current) => ({ ...current, incidents: incidentResult.value.data }));
      else notices.push(`Recent reports: ${errorMessage(incidentResult.reason)}`);
      setDataErrors(notices);
    }).finally(() => { if (active) setBusy(false); });
    return () => { active = false; };
  }, []);

  const completion = useMemo(() => {
    if (!profile) return 0;
    const fields = [profile.name, profile.email, profile.mobileNumber, profile.address];
    return Math.round(fields.filter((value) => Boolean(value?.trim())).length / fields.length * 100);
  }, [profile]);
  const name = profile?.name?.trim()?.split(/\s+/)[0] || 'traveller';
  const recentIncidents = summary.incidents.slice(0, 3);

  return <main className="dashboard dashboard-modern">
    <div className="dashboard-shell">
      <section className="dashboard-welcome">
        <div>
          <span className="eyebrow">YOUR TRAVEL SAFETY SPACE</span>
          <h1>{busy ? 'Preparing your dashboard…' : `Welcome, ${name}`}</h1>
          <p>Keep your travel essentials close and get support when you need it.</p>
        </div>
        <Link className="welcome-profile-link" to="/profile"><span className="welcome-avatar">{profile?.name?.[0]?.toUpperCase() || 'Y'}</span><span><small>YOUR PROFILE</small><strong>{profile?.name || 'View profile'}</strong></span><span aria-hidden="true">↗</span></Link>
      </section>

      {error && <div className="notice error" role="alert">Could not load your profile: {error}</div>}
      {dataErrors.length > 0 && <div className="dashboard-data-notice" role="status">Some dashboard details could not be loaded: {dataErrors.join(' · ')}</div>}

      <section className="dashboard-sos-card">
        <div className="dashboard-sos-copy">
          <span className="dashboard-sos-icon" aria-hidden="true">!</span>
          <div><span className="eyebrow">URGENT ASSISTANCE</span><h2>Need help right now?</h2><p>Send an in-app SOS alert to authorized YatraSecure administrators.</p></div>
        </div>
        <SosAction className="dashboard-sos-control" onCreated={refreshIncidents} />
      </section>

      <section className="dashboard-overview-grid" aria-label="Travel safety overview">
        <article className="dashboard-info-card tourist-id-card">
          <div className="dashboard-card-heading"><span className="dashboard-icon icon-teal" aria-hidden="true">ID</span><span className="incident-status status-active">DIGITAL ID</span></div>
          <h2>{summary.touristId?.touristId || 'Your Tourist ID'}</h2>
          <p>{summary.touristId ? `Status: ${summary.touristId.status}` : 'A verified travel ID is ready when you are.'}</p>
          <Link className="card-link" to="/tourist-id">{summary.touristId ? 'View QR identity' : 'Create Digital ID'} <span aria-hidden="true">→</span></Link>
        </article>

        <article className="dashboard-info-card">
          <div className="dashboard-card-heading"><span className="dashboard-icon icon-blue" aria-hidden="true">⌖</span><span className={`incident-status ${summary.sharing?.sharingEnabled ? 'status-active' : 'status-cancelled'}`}>{summary.sharing?.sharingEnabled ? 'SHARING ON' : 'SHARING OFF'}</span></div>
          <h2>Live location</h2>
          <p>{summary.sharing?.sharingEnabled ? 'Your location is being shared with authorized administrators.' : 'Your location remains private until you choose to share it.'}</p>
          {summary.sharing?.updatedAt && <small className="muted-line">Updated {new Date(summary.sharing.updatedAt).toLocaleString()}</small>}
          <Link className="card-link" to="/live-map">Manage location sharing <span aria-hidden="true">→</span></Link>
        </article>

        <article className="dashboard-info-card profile-completion-card">
          <div className="dashboard-card-heading"><span className="dashboard-icon icon-gold" aria-hidden="true">✓</span><span className="incident-status status-active">{completion}% COMPLETE</span></div>
          <h2>Profile status</h2>
          <div className="completion-track" role="progressbar" aria-label="Profile completion" aria-valuemin="0" aria-valuemax="100" aria-valuenow={completion}><span style={{ width: `${completion}%` }} /></div>
          <p>{completion === 100 ? 'Your profile has all the key details.' : 'Add contact details to complete your travel profile.'}</p>
          <Link className="card-link" to="/profile">Review profile <span aria-hidden="true">→</span></Link>
        </article>
      </section>

      <section className="dashboard-lower-grid">
        <article className="dashboard-panel assistant-panel">
          <div className="dashboard-panel-heading"><span className="dashboard-icon icon-violet" aria-hidden="true">✦</span><span className="eyebrow">TRAVEL GUIDANCE</span></div>
          <h2>Safety Assistant</h2>
          <p>Get practical travel precautions, emergency guidance, and help using YatraSecure.</p>
          <Link className="primary-btn dashboard-panel-action" to="/assistant">Ask the assistant <span aria-hidden="true">→</span></Link>
        </article>

        <article className="dashboard-panel zones-panel">
          <div className="dashboard-panel-heading"><span className="dashboard-icon icon-green" aria-hidden="true">◈</span><span className="eyebrow">AREA ADVISORIES</span></div>
          <h2>Safety zones</h2>
          <p>{summary.zones.length ? `${summary.zones.length} active verified ${summary.zones.length === 1 ? 'zone' : 'zones'} are available.` : 'No active verified zones are available right now.'}</p>
          <div className="zone-chip-row">{summary.zones.slice(0, 3).map((zone) => <span className={`zone-chip zone-${zone.zoneType.toLowerCase()}`} key={zone.id}>{zone.zoneType}: {zone.name}</span>)}</div>
          <Link className="card-link" to="/live-map">View map and advisories <span aria-hidden="true">→</span></Link>
          <small className="muted-line">Zone information is advisory and may not reflect changing conditions.</small>
        </article>
      </section>

      <section className="dashboard-panel recent-panel">
        <div className="recent-panel-heading"><div><span className="eyebrow">YOUR ACTIVITY</span><h2>Recent reports</h2></div><Link className="secondary-link" to="/incidents">Safety &amp; reports</Link></div>
        {recentIncidents.length === 0 ? <div className="empty-state">{busy ? 'Loading your reports…' : 'No incident reports yet. Your reports and SOS alerts will appear here.'}</div> : <div className="recent-incident-list">{recentIncidents.map((incident) => <article className="recent-incident-row" key={incident.id}>
          <span className={`incident-type-mark ${incident.incidentType === 'SOS' ? 'mark-sos' : ''}`} aria-hidden="true">{incident.incidentType === 'SOS' ? '!' : '•'}</span>
          <div className="recent-incident-details"><strong>{incident.incidentType === 'SOS' ? 'SOS alert' : incident.incidentType}</strong><span>{incident.description}</span><small>{new Date(incident.createdAt).toLocaleString()} · {incident.priority}</small></div>
          <span className={statusClass(incident.status)}>{incident.status.replace('_', ' ')}</span>
        </article>)}</div>}
      </section>

      <footer className="dashboard-footer"><span>YatraSecure · Travel with confidence</span><span>SOS alerts are in-app records only.</span></footer>
    </div>
  </main>;
}
