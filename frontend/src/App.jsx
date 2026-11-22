import { createContext, useContext, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { TOKEN_KEY } from './api';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Profile from './pages/Profile.jsx';
import AdminDashboard from './pages/AdminDashboard.jsx';
import LiveMap from './pages/LiveMap.jsx';
import AdminLiveMap from './pages/AdminLiveMap.jsx';
import AdminSafetyZones from './pages/AdminSafetyZones.jsx';
import Incidents from './pages/Incidents.jsx';
import AdminIncidents from './pages/AdminIncidents.jsx';
import TouristId from './pages/TouristId.jsx';
import VerifyTouristId from './pages/VerifyTouristId.jsx';
import SafetyAssistant from './pages/SafetyAssistant.jsx';
import AppNav from './components/AppNav.jsx';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);
export function readTokenRole(token) { try { return JSON.parse(atob(token.split('.')[1])).role; } catch { return null; } }
export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const login = (value) => { localStorage.setItem(TOKEN_KEY, value); setToken(value); };
  const role = () => readTokenRole(token);
  const logout = () => { localStorage.removeItem(TOKEN_KEY); setToken(null); };
  return <AuthContext.Provider value={{ token, login, logout }}>{token && <AppNav/>}<Routes>
    <Route path="/" element={<Navigate to={token ? (role() === 'ROLE_ADMIN' ? '/admin' : '/dashboard') : '/login'} replace />} />
    <Route path="/login" element={token ? <Navigate to={role() === 'ROLE_ADMIN' ? '/admin' : '/dashboard'} replace /> : <Login />} />
    <Route path="/register" element={token ? <Navigate to="/dashboard" replace /> : <Register />} />
    <Route path="/verify/:touristId" element={<VerifyTouristId />} />
    <Route path="/tourist-id" element={token && role() === 'ROLE_TOURIST' ? <TouristId /> : <Navigate to={token ? '/admin' : '/login'} replace />} />
    <Route path="/assistant" element={token && role() === 'ROLE_TOURIST' ? <SafetyAssistant /> : <Navigate to={token ? '/admin' : '/login'} replace />} />
    <Route path="/dashboard" element={token ? <Dashboard /> : <Navigate to="/login" replace />} />
    <Route path="/profile" element={token ? <Profile /> : <Navigate to="/login" replace />} />
    <Route path="/live-map" element={token && role() === 'ROLE_TOURIST' ? <LiveMap /> : <Navigate to={token ? '/admin' : '/login'} replace />} />
    <Route path="/incidents" element={token && role() === 'ROLE_TOURIST' ? <Incidents /> : <Navigate to={token ? '/admin' : '/login'} replace />} />
    <Route path="/admin" element={token && role() === 'ROLE_ADMIN' ? <AdminDashboard /> : <Navigate to={token ? '/dashboard' : '/login'} replace />} />
    <Route path="/admin/live-map" element={token && role() === 'ROLE_ADMIN' ? <AdminLiveMap /> : <Navigate to={token ? '/dashboard' : '/login'} replace />} />
    <Route path="/admin/safety-zones" element={token && role() === 'ROLE_ADMIN' ? <AdminSafetyZones /> : <Navigate to={token ? '/dashboard' : '/login'} replace />} />
    <Route path="/admin/incidents" element={token && role() === 'ROLE_ADMIN' ? <AdminIncidents /> : <Navigate to={token ? '/dashboard' : '/login'} replace />} />
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes></AuthContext.Provider>;
}
