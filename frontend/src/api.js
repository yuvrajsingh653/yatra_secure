import axios from 'axios';

export const TOKEN_KEY = 'yatrasecure_token';
export const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').trim().replace(/\/+$/, '');
export const api = axios.create({ baseURL: API_BASE_URL });
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
api.interceptors.response.use((response) => response, (error) => {
  if (error.response?.status === 401) {
    localStorage.removeItem(TOKEN_KEY);
    const publicAuthRequest = ['/api/auth/login', '/api/auth/register'].includes(error.config?.url);
    if (!publicAuthRequest && window.location.pathname !== '/login' && window.location.pathname !== '/register') {
      window.location.assign('/login');
    }
  } else if (error.response?.status === 403 && window.location.pathname.startsWith('/admin')) {
    window.location.assign('/dashboard');
  }
  return Promise.reject(error);
});
export function errorMessage(error) {
  const data = error.response?.data;
  if (typeof data?.message === 'string') return data.message;
  if (typeof data?.error === 'string') return data.error;
  if (error.response?.status === 401 && error.config?.url === '/api/auth/login') return 'Invalid email or password.';
  if (error.response?.status === 403) return 'You do not have permission to perform this action.';
  if (error.response?.status === 404) return 'The requested API endpoint was not found (404).';
  if (error.response?.status >= 500) return `The backend returned an error (${error.response.status}). Please try again or check the backend logs.`;
  if (error.code === 'ERR_NETWORK') return `Cannot reach the backend at ${API_BASE_URL}. Confirm it is running and that the frontend origin is allowed by backend CORS.`;
  return 'Something went wrong. Please try again.';
}
