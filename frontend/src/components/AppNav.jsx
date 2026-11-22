import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { readTokenRole, useAuth } from '../App';

const touristLinks = [
  ['Dashboard', '/dashboard'],
  ['My Profile', '/profile'],
  ['Digital ID', '/tourist-id'],
  ['Safety Assistant', '/assistant'],
  ['Live Location', '/live-map'],
  ['Safety & Reports', '/incidents'],
];
const adminLinks = [
  ['Admin Dashboard', '/admin'],
  ['Admin Live Map', '/admin/live-map'],
  ['Safety Zones', '/admin/safety-zones'],
  ['Incident Management', '/admin/incidents'],
];

export default function AppNav() {
  const { token, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const admin = readTokenRole(token) === 'ROLE_ADMIN';
  const links = admin ? adminLinks : touristLinks;

  const signOut = () => {
    logout();
    setOpen(false);
    navigate('/login', { replace: true });
  };

  return <header className="dash-nav app-header">
    <Link className="brand" to={admin ? '/admin' : '/dashboard'}><span className="brand-mark">Y</span> Yatra<span>Secure</span></Link>
    <button className="nav-toggle" type="button" aria-label="Toggle navigation" aria-expanded={open} onClick={() => setOpen(!open)}>Menu</button>
    <nav className={`app-nav-links ${open ? 'open' : ''}`} aria-label="Main navigation">
      {links.map(([label, path]) => <Link key={path} to={path} onClick={() => setOpen(false)}>{label}</Link>)}
      <button className="logout-btn" type="button" onClick={signOut}>Log out</button>
    </nav>
  </header>;
}
