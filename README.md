# YatraSecure

Spring Boot and React application for tourist accounts, profile management, role-based administration, opt-in live location sharing, safety-zone advisories, incident/SOS records, Digital Tourist IDs, and a provider-mediated travel safety assistant.

## Architecture

- Backend: Java 21, Spring Boot, Spring Security, JWT (JJWT), Spring Data JPA, MySQL.
- Frontend: React 18, Vite, React Router, Axios, React Leaflet/OpenStreetMap, QRCode.
- Backend source: `Yatra-secure/src/main/java/com/tourist/Yatra_secure` (`controller`, `service`, `Repository`, `entity`, `dto`, `Security`).
- Frontend source: `frontend/src` (`pages`, `components`, `api.js`). Axios adds Bearer tokens and handles 401/403.

## Features and API routes

- Authentication: `POST /api/auth/register`, `POST /api/auth/login`.
- Tourist profile: `GET /api/auth/profile`, `GET/PUT /api/user/profile`.
- Admin: `GET /api/admin/dashboard`, `GET /api/admin/tourists`.
- Live location: tourist `PUT/DELETE /api/location/sharing`, `PUT /api/location/sharing/location`; admin `GET /api/location/admin/shared`. Location sharing requires explicit consent.
- Safety zones: admin `GET/POST /api/safety-zones`, `PUT /api/safety-zones/{id}`, `PATCH /api/safety-zones/{id}/status`; tourist `GET /api/safety-zones/active`, `GET /api/safety-zones/check?latitude=&longitude=`.
- Incidents/SOS: tourist `POST /api/incidents`, `POST /api/incidents/sos`, `GET /api/incidents/mine`, `POST /api/incidents/{id}/cancel`; admin `GET /api/incidents/admin`, `PATCH /api/incidents/admin/{id}`. SOS records an in-app alert and does not contact emergency services.
- Digital ID: authenticated tourist `POST /api/tourist-id`, `GET /api/tourist-id/me`; public limited status `GET /api/verify/tourist/{touristId}`; QR URL is generated from `PUBLIC_APP_BASE_URL` (or local `public.app.base-url`); admin `GET /api/admin/tourist-ids`, `DELETE /api/admin/tourist-ids/{id}`.
- Assistant: tourist `POST /api/assistant` with `{ "message": "..." }`. It uses configured provider credentials when available and automatically falls back to local safety guidance when credentials are absent or the provider fails. It does not dispatch emergency services or receive private location context.

## Environment and local secrets

On the first local run, `setup-local.ps1` creates an ignored `application-local.properties` with a random 256-bit JWT signing key that stays stable across restarts. It prompts for MySQL credentials, optional admin bootstrap credentials, and initializes the QR URL from the active network IPv4 address. Keep this file private. `.env.example` is a reference template only.

Create the MySQL database `yatra_secure` before startup. JPA currently uses `ddl-auto=update`; use a managed migration system before production. Production must provide `DB_USERNAME`, `DB_PASSWORD`, and a strong Base64 `JWT_SECRET` through its secret manager/environment, plus optional `ADMIN_EMAIL`, `ADMIN_PASSWORD`, and assistant provider settings. Do not package local configuration in production. Configure Vite API using optional `VITE_API_URL` (defaults to `http://localhost:8080`). Browser geolocation requires localhost or HTTPS.

## Run locally

Start the backend from the repository root (first start bootstraps ignored local settings):

```powershell
.\Yatra-secure\run-local.cmd
```

Frontend from `frontend`:

```powershell
npm.cmd install
npm.cmd run dev
```

Frontend API calls share one Axios instance in `frontend/src/api.js`. It reads `VITE_API_URL` and safely defaults to `http://localhost:8080`. To override locally, copy `frontend/.env.example` to `frontend/.env.local`, set `VITE_API_URL`, and restart Vite. Backend CORS allows `http://localhost:5173`, `http://127.0.0.1:5173`, and the explicitly configured application origin; JWT uses the Authorization header without cookie credentials.

Production frontend build: `npm.cmd run build`. Backend tests: `.\mvnw.cmd clean test` from `Yatra-secure`.

### Verify QR codes from a phone on the same Wi-Fi

Find the laptop's Wi-Fi IPv4 address with `ipconfig` (look under the connected Wi-Fi adapter for `IPv4 Address`). In `Yatra-secure/application-local.properties`, set `public.app.base-url=http://<laptop-ipv4>:5173`, for example `http://192.168.1.5:5173`. Alternatively, set environment variable `PUBLIC_APP_BASE_URL` before starting the backend. This exact origin is used for QR URLs and backend CORS. The Vite dev server listens on `0.0.0.0`; from `frontend`, start it with `npm.cmd run dev -- --host 0.0.0.0`. Leave `VITE_API_URL` unset to make API calls use the hostname serving the frontend with port 8080, or set it to `http://<laptop-ipv4>:8080`. Restart both processes after changing configuration. Allow inbound connections to ports 5173 and 8080 in the laptop firewall if prompted. This works only for devices on the same reachable LAN/Wi-Fi; a private IPv4 address cannot be reached from the public internet or another network. Public scanning requires a publicly reachable HTTPS deployment and its public URL configured as `PUBLIC_APP_BASE_URL`.

## Security notes

Tourist routes derive ownership from the authenticated principal. Admin routes are role restricted. Public Digital ID verification returns only ID, status, display name when valid, and issue date. Public location endpoints do not exist. Safety-zone advisories are not guarantees of safety. SOS is an application record only.


