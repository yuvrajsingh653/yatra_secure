import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, errorMessage } from '../api';
import { useAuth } from '../App';
import { readTokenRole } from '../App';
import AuthLayout from '../components/AuthLayout';
export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' }); const [show, setShow] = useState(false); const [busy, setBusy] = useState(false); const [error, setError] = useState('');
  const { login } = useAuth(); const navigate = useNavigate();
  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });
  const submit = async (e) => { e.preventDefault(); setError(''); setBusy(true); try { const { data } = await api.post('/api/auth/login', form); if (!data?.token) throw new Error('The server response did not include a token.'); login(data.token); navigate(readTokenRole(data.token) === 'ROLE_ADMIN' ? '/admin' : '/dashboard'); } catch (err) { setError(errorMessage(err)); } finally { setBusy(false); } };
  return <AuthLayout eyebrow="WELCOME BACK" title="Sign in" subtitle="Pick up where your next adventure begins." footer="New to YatraSecure?" alternate="/register" alternateText="Create an account"><form onSubmit={submit} className="auth-form"><label>Email address<input autoComplete="email" type="email" name="email" placeholder="you@example.com" value={form.email} onChange={change} required /></label><label>Password<div className="password-box"><input autoComplete="current-password" type={show ? 'text' : 'password'} name="password" placeholder="Enter your password" value={form.password} onChange={change} required minLength="1"/><button type="button" className="reveal" onClick={() => setShow(!show)}>{show ? 'Hide' : 'Show'}</button></div></label>{error && <div className="notice error" role="alert">{error}</div>}<button className="primary-btn" disabled={busy}>{busy ? <><span className="spinner"/> Signing in…</> : <>Sign in <span>→</span></>}</button><p className="secure-note">⌑ &nbsp;Your details are protected with secure encryption</p></form></AuthLayout>;
}
