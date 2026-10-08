# DiveMatrix: API Design

Base URL (local): `http://localhost:8080`

All requests and responses use JSON. Protected endpoints need the header:

```
Authorization: Bearer <token from /api/auth/login>
```

## Design conventions

| Convention | How DiveMatrix does it |
|---|---|
| Resource naming | Plural nouns: `/api/dive-sites`, `/api/trips`, `/api/bookings` |
| HTTP methods | `GET` reads, `POST` creates, `PUT` replaces, `PATCH` changes one thing (cancel, verify), `DELETE` removes |
| Status codes | `200` OK, `201` Created, `204` No Content, then the error codes below |
| Validation | `@Valid` on every request body; failures return `400` with `fieldErrors` |
| Pagination | `?page=0&size=10&sort=departureTime,asc` (max size 50) |
| Data shapes | DTO records only; entities and password hashes are never returned |
| Errors | One JSON shape for every error, built by `GlobalExceptionHandler` |

## Endpoints

### Health

| Method | Path | Access | Success |
|---|---|---|---|
| GET | `/api/health` | Public | 200 `{"status":"UP"}` |

### Authentication

| Method | Path | Access | Success | Errors |
|---|---|---|---|---|
| POST | `/api/auth/register` | Public, rate limited | 201 + token | 400 validation, 409 email taken, 429 too many attempts |
| POST | `/api/auth/login` | Public, rate limited | 200 + token | 400, 401 bad credentials, 429 |
| GET | `/api/auth/me` | Logged in | 200 user profile | 401 |

**Register request**

```json
{
  "email": "diver@example.com",
  "password": "Ocean2026",
  "fullName": "Sam Diver",
  "certLevel": "ADVANCED_OPEN_WATER",
  "certAgency": "PADI",
  "certNumber": "AOW-12345"
}
```

**Login response**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 4,
    "email": "diver@example.com",
    "fullName": "Sam Diver",
    "role": "USER",
    "certLevel": "ADVANCED_OPEN_WATER",
    "certVerified": false
  }
}
```

### Dive sites

| Method | Path | Access | Success | Errors |
|---|---|---|---|---|
| GET | `/api/dive-sites` | Public | 200 list | |
| GET | `/api/dive-sites/{id}` | Public | 200 site | 404 |
| POST | `/api/dive-sites` | Admin | 201 site | 400, 401, 403, 409 duplicate name |
| PUT | `/api/dive-sites/{id}` | Admin | 200 site | 400, 401, 403, 404, 409 |
| DELETE | `/api/dive-sites/{id}` | Admin | 204 | 401, 403, 404, 409 site still has trips |

**Create / update request**

```json
{
  "name": "Blue Corner Wall",
  "location": "Palau",
  "maxDepthMeters": 30,
  "difficulty": "ADVANCED",
  "description": "Drift dive along a reef wall with strong currents.",
  "imageUrl": "/images/sites/blue-corner-wall.jpg"
}
```

`imageUrl` is optional and must start with `/` or `https://`.

### Trips

| Method | Path | Access | Success | Errors |
|---|---|---|---|---|
| GET | `/api/trips?page=0&size=10&sort=departureTime,asc` | Public | 200 page of trips | 400 bad sort field |
| GET | `/api/trips/{id}` | Public | 200 trip | 404 |

**Trip response**

```json
{
  "id": 6,
  "siteId": 3,
  "siteName": "Blue Corner Wall",
  "siteMaxDepthMeters": 30,
  "boatName": "Coral Queen",
  "departureTime": "2026-10-16T09:00:00",
  "returnTime": "2026-10-16T14:00:00",
  "capacity": 16,
  "seatsAvailable": 11,
  "price": 135.00,
  "status": "SCHEDULED",
  "siteImageUrl": "/images/sites/blue-corner-wall.jpg"
}
```

**Paged response wrapper** (used by every list that pages)

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 10,
  "totalElements": 24,
  "totalPages": 3,
  "last": false
}
```

### Bookings

| Method | Path | Access | Success | Errors |
|---|---|---|---|---|
| POST | `/api/bookings` | Logged in | 201 booking | 401, 403 not verified, 404 trip, 409 full / departed / already booked, 422 certification too shallow |
| GET | `/api/bookings/me` | Logged in | 200 page of my bookings, newest first | 401 |
| PATCH | `/api/bookings/{id}/cancel` | Owner (until 24 h before departure) or Admin (any time) | 200 booking | 401, 403 not yours, 404, 409 already cancelled or inside 24 h |

**Create request**

```json
{ "tripId": 6 }
```

The diver is always taken from the token, never from the request body, so nobody can book on someone else's behalf.

### Admin

| Method | Path | Access | Success | Errors |
|---|---|---|---|---|
| GET | `/api/admin/users?pendingOnly=true` | Admin | 200 divers awaiting verification | 401, 403 |
| PATCH | `/api/admin/users/{id}/certification` | Admin | 200 updated diver | 400, 401, 403, 404 |
| GET | `/api/admin/trips/{id}/manifest` | Admin | 200 manifest (confirmed divers only) | 401, 403, 404 |

**Verify request**

```json
{ "verified": true }
```

## Error format

Every error, from any endpoint, has the same shape:

```json
{
  "timestamp": "2026-10-08T14:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Great Blue Hole reaches 40 m, but your OPEN_WATER certification only allows dives to 18 m.",
  "path": "/api/bookings"
}
```

Validation errors add a `fieldErrors` object:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": {
    "email": "must be a well-formed email address",
    "password": "must contain a letter and a number"
  }
}
```

| Status | When |
|---|---|
| 400 | Invalid input (validation, bad JSON, bad sort field) |
| 401 | Missing, expired or invalid token; wrong password |
| 403 | Logged in but not allowed (not admin, not your booking, certification not verified) |
| 404 | Resource doesn't exist |
| 409 | Conflicts with current state (duplicate, trip full, departed, already booked, already cancelled) |
| 422 | Valid request that breaks a safety rule (dive deeper than certification allows) |
| 429 | Too many login/register attempts; includes a `Retry-After` header |

## Booking rules (POST /api/bookings)

Checked in this order inside one transaction. If any fails, nothing is saved.

| # | Rule | Error |
|---|---|---|
| 1 | Diver's certification is verified | 403 |
| 2 | Certification depth ≥ site's max depth | 422 |
| 3 | Trip is SCHEDULED and hasn't departed | 409 |
| 4 | Diver has no confirmed booking on this trip | 409 |
| 5 | Trip has a free seat (optimistic locking on the last seat) | 409 |
