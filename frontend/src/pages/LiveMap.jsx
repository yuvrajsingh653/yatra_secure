import { useEffect, useRef, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap, GeoJSON } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { api, errorMessage } from '../api';

const markerIcon = L.icon({
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41],
});

function Recenter({ point }) {
  const map = useMap();
  useEffect(() => { if (point) map.setView(point, map.getZoom()); }, [point, map]);
  return null;
}

export default function LiveMap() {
  const [point, setPoint] = useState(null);
  const [sharing, setSharing] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [denied, setDenied] = useState(false);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [zones, setZones] = useState([]);
  const [zoneCheck, setZoneCheck] = useState(null);
  const watchId = useRef(null);
  const lastAttempt = useRef(0);

  const stopWatching = () => {
    if (watchId.current !== null) {
      navigator.geolocation.clearWatch(watchId.current);
      watchId.current = null;
    }
  };

  useEffect(() => () => stopWatching(), []);
  useEffect(() => { api.get('/api/safety-zones/active').then(({data}) => setZones(data)).catch(() => {}); }, []);
  useEffect(() => { if (!point) return; api.get('/api/safety-zones/check', {params:{latitude:point[0],longitude:point[1]}}).then(({data})=>setZoneCheck(data)).catch(()=>setZoneCheck(null)); }, [point]);

  const stopSharing = async () => {
    setBusy(true);
    setError('');
    stopWatching();
    try {
      await api.delete('/api/location/sharing');
      setSharing(false);
      setLastUpdated(null);
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setBusy(false);
    }
  };

  const startSharing = () => {
    setError('');
    setDenied(false);
    if (!navigator.geolocation) {
      setError('Geolocation is unavailable in this browser.');
      return;
    }
    setBusy(true);
    navigator.geolocation.getCurrentPosition(async (position) => {
      const coords = { latitude: position.coords.latitude, longitude: position.coords.longitude };
      try {
        const { data } = await api.put('/api/location/sharing', coords);
        setPoint([coords.latitude, coords.longitude]);
        setLastUpdated(data.updatedAt || new Date().toISOString());
        setSharing(true);
        watchId.current = navigator.geolocation.watchPosition((nextPosition) => {
          const now = Date.now();
          if (now - lastAttempt.current < 15000) return;
          lastAttempt.current = now;
          const nextCoords = { latitude: nextPosition.coords.latitude, longitude: nextPosition.coords.longitude };
          api.put('/api/location/sharing/location', nextCoords)
            .then(({ data: update }) => {
              setPoint([nextCoords.latitude, nextCoords.longitude]);
              setLastUpdated(update.updatedAt || new Date().toISOString());
            })
            .catch((requestError) => setError(errorMessage(requestError)));
        }, (locationError) => {
          setError(locationError.code === 1 ? 'Location permission denied.' : locationError.code === 2 ? 'Current location is unavailable.' : 'Could not determine your location.');
        }, { enableHighAccuracy: true, maximumAge: 10000, timeout: 20000 });
      } catch (requestError) {
        setError(errorMessage(requestError));
      } finally {
        setBusy(false);
      }
    }, (locationError) => {
      setBusy(false);
      if (locationError.code === 1) setDenied(true);
      setError(locationError.code === 1 ? 'Location permission denied. Enable it in browser settings to share.' : locationError.code === 2 ? 'Current location is unavailable.' : 'Location request timed out.');
    }, { enableHighAccuracy: true, maximumAge: 10000, timeout: 20000 });
  };

  return <main className="dashboard">
    
    <section className="profile-area">
      <h2>Live location sharing</h2>
      <p>Sharing is off until you choose Start Sharing. Only authorized administrators can view your location while sharing is enabled.</p>
      <p className={sharing ? 'notice success' : 'status-pill'} aria-live="polite">{sharing ? 'Location sharing is ON' : 'Location sharing is OFF'}</p>
      {point && <p>Your coordinates: {point[0].toFixed(6)}, {point[1].toFixed(6)}</p>}
      {zoneCheck?.zones?.map(zone => <div key={zone.id} className={`notice ${zone.zoneType==='SAFE'?'success':'error'}`}>{zone.advisory}</div>)}
      {lastUpdated && <p>Last successful location update: <time dateTime={lastUpdated}>{new Date(lastUpdated).toLocaleString()}</time></p>}
      {error && <div className="notice error" role="alert">{error}</div>}
      {denied && <p>Allow location access in your browser, then try again.</p>}
      <button disabled={busy || sharing} className="primary-btn" onClick={startSharing}>{busy ? 'Getting location…' : 'Start Sharing'}</button>{' '}
      <button disabled={busy || !sharing} className="logout-btn" onClick={stopSharing}>Stop Sharing</button>
      {point && <MapContainer center={point} zoom={14} style={{ height: 420, marginTop: 20 }}>
        <TileLayer attribution="&copy; OpenStreetMap contributors" url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        {zones.map(zone => <GeoJSON key={zone.id} data={{type:'Feature',properties:{},geometry:zone.boundary}} style={{color:zone.zoneType==='SAFE'?'#168e56':zone.zoneType==='CAUTION'?'#d28a00':'#b83232',weight:2,fillOpacity:.18}}><Popup>{zone.name} · {zone.zoneType}<br/>{zone.description}<br/>Source: {zone.source} (verified)</Popup></GeoJSON>)}
        <Marker position={point} icon={markerIcon}><Popup>Your shared location</Popup></Marker>
        <Recenter point={point} />
      </MapContainer>}
    </section>
  </main>;
}
