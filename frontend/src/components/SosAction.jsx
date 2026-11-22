import { useRef, useState } from 'react';
import { api, errorMessage } from '../api';

function getOptionalLocation(timeoutMs = 1800) {
  if (!navigator.geolocation) return Promise.resolve(null);

  return new Promise((resolve) => {
    let settled = false;
    const finish = (location) => {
      if (settled) return;
      settled = true;
      window.clearTimeout(timer);
      resolve(location);
    };
    const timer = window.setTimeout(() => finish(null), timeoutMs);

    navigator.geolocation.getCurrentPosition(
      ({ coords }) => finish({ latitude: coords.latitude, longitude: coords.longitude }),
      () => finish(null),
      { enableHighAccuracy: false, maximumAge: 60_000, timeout: timeoutMs },
    );
  });
}

export default function SosAction({ className = '', onCreated }) {
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState(null);
  const inFlight = useRef(false);

  const submit = async () => {
    if (inFlight.current) return;
    if (!window.confirm('Send an SOS alert to YatraSecure administrators? This creates an in-app alert and does not contact emergency services.')) return;

    inFlight.current = true;
    setBusy(true);
    setNotice(null);
    try {
      const location = await getOptionalLocation();
      const { data } = await api.post('/api/incidents/sos', {
        incidentType: 'SAFETY',
        description: 'Tourist requested urgent assistance using the SOS alert.',
        priority: 'CRITICAL',
        latitude: location?.latitude ?? null,
        longitude: location?.longitude ?? null,
      });
      setNotice({ type: 'success', text: 'SOS alert recorded for administrators. This in-app alert does not contact emergency services.' });
      onCreated?.(data);
    } catch (error) {
      setNotice({ type: 'error', text: errorMessage(error) });
    } finally {
      inFlight.current = false;
      setBusy(false);
    }
  };

  return <div className={`sos-action ${className}`}>
    <button type="button" className="sos-button" onClick={submit} disabled={busy}>
      <span className="sos-button-icon" aria-hidden="true">!</span>
      <span>{busy ? 'Sending alert…' : 'Send SOS alert'}</span>
    </button>
    <p className="sos-disclaimer">In-app alert only. It does not contact emergency services.</p>
    {notice && <div className={`notice ${notice.type}`} role={notice.type === 'error' ? 'alert' : 'status'} aria-live="polite">{notice.text}</div>}
  </div>;
}
