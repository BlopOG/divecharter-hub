# DiveCharter Hub

A booking platform for scuba diving charters. Divers browse dive sites and upcoming boat trips and book a seat, but **only on trips their certification qualifies them for**. Before accepting a booking, the backend checks the diver's certification against the dive site's maximum depth. It also manages boat capacity, prevents double-booking, and enforces a cancellation window.

*Capstone project for UCI 2123.*
## The problem it solves

Many small dive shops still take bookings by phone, email, or spreadsheet. That causes a few recurring problems:

- **Safety checks are manual.** Staff have to remember to check each diver's certification card and compare it to how deep the dive goes. If someone forgets, a diver could end up on a dive deeper than their training covers.
- **Boats get overbooked.** When two people take bookings at the same time, or a spreadsheet isn't updated, more divers can be booked than the boat has seats.
- **Cancellations are hard to track.** Last-minute cancellations leave empty seats that nobody else knew were available.
- **Passenger lists are built by hand.** Before each trip, staff piece together who's on board and what they're certified for.

### How DiveCharter Hub solves it

| Problem | Solution |
|---|---|
| Manual certification checks | Staff verify each diver's card once in the app; after that, every booking is checked automatically against the site's depth |
| Divers booking beyond their training | Bookings are refused when a site is deeper than the diver's certification allows, with a clear explanation |
| Overbooking | Seats are counted by the system, and simultaneous bookings for the last seat can't both succeed |
| Untracked cancellations | Divers cancel online up to 24 hours before departure, and the seat is released immediately for someone else |
| Hand-built passenger lists | Bookings, certification details, and seat counts are all stored in one place, ready for a trip manifest |
---

## Features

- **Browse** dive sites and upcoming trips (paginated and sortable), no login required
- **Register and log in** with JWT authentication
- **Book a seat** with five business rules enforced:
  1. The diver's certification must be verified by staff
  2. The certification's depth limit must cover the site's maximum depth
  3. The trip must be scheduled and not yet departed
  4. No double-booking the same trip
  5. The boat must have a free seat (optimistic locking prevents overbooking)
- **View and cancel** your own bookings, up to 24 hours before departure
- **Admins** manage dive sites and can cancel any booking at any time

### Certification depth limits

| Certification | Max depth |
|---|---|
| Open Water | 18 m |
| Advanced Open Water | 30 m |
| Rescue Diver | 30 m |
| Deep Specialty | 40 m |
| Divemaster | 40 m |

---

## Tech stack

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Data JPA, Bean Validation |
| Security | Spring Security, JWT, BCrypt |
| Database | MySQL 8, Flyway migrations |
| API testing | Bruno |
| Frontend | React + Vite *(in progress)* |

---

## Getting started

### Prerequisites

- JDK 21 or newer
- MySQL 8
- Git Bash (or any terminal with `openssl`)

### 1. Create the database

Run in MySQL as `root`, choosing your own password:

```sql
CREATE DATABASE divecharter;
CREATE USER 'divecharter'@'localhost' IDENTIFIED BY 'choose_a_password';
GRANT ALL PRIVILEGES ON divecharter.* TO 'divecharter'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configure secrets

```bash
cd backend
cp .env.example .env
openssl rand -base64 32
```

Paste the `openssl` output into `JWT_SECRET` in `.env`, and set `DB_PASSWORD` to the password from step 1. The `.env` file is git-ignored and must never be committed.

### 3. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`. On first run, Flyway creates the tables and loads sample dive sites and trips. Check it with `http://localhost:8080/api/health`.

### 4. Create an admin

Register through `POST /api/auth/register`, then promote the account in MySQL:

```sql
UPDATE divecharter.users
SET role = 'ADMIN', cert_verified = TRUE
WHERE email = 'your-email@example.com';
```

---

## API endpoints

| Method | Endpoint | Access |
|---|---|---|
| `GET` | `/api/health` | Public |
| `POST` | `/api/auth/register` | Public |
| `POST` | `/api/auth/login` | Public |
| `GET` | `/api/auth/me` | Logged in |
| `GET` | `/api/dive-sites` | Public |
| `GET` | `/api/dive-sites/{id}` | Public |
| `POST` `PUT` `DELETE` | `/api/dive-sites/{id}` | Admin |
| `GET` | `/api/trips` | Public |
| `GET` | `/api/trips/{id}` | Public |
| `POST` | `/api/bookings` | Logged in |
| `GET` | `/api/bookings/me` | Logged in |
| `PATCH` | `/api/bookings/{id}/cancel` | Owner or Admin |
| `GET` | `/api/admin/users?pendingOnly=` | Admin |
| `PATCH` | `/api/admin/users/{id}/certification` | Admin |
| `GET` | `/api/admin/trips/{id}/manifest` | Admin |

Protected endpoints need the header `Authorization: Bearer <token>`, using the token from `/api/auth/login`.

---

## Security

- Passwords hashed with **BCrypt** and never returned
- Stateless **JWT** login with 24-hour tokens
- **USER** and **ADMIN** roles; new accounts are always USER and unverified
- **CORS** limited to the frontend at `http://localhost:5173`
- **Rate limiting**: 5 login or register attempts per minute per IP
- **Input sanitization** removes HTML from text fields
- Secrets stored in a git-ignored `.env` file

---

## Testing

The `bruno/` folder holds an API collection for every endpoint, including error cases. Open it in [Bruno](https://www.usebruno.com/), select the `local` environment, and run `login` first. The token is then saved automatically for the other requests.

*Unit tests with coverage reporting are in progress.*

---

## Project structure

```
divecharter-hub/
├── backend/            Spring Boot API
│   └── src/main/
│       ├── java/com/divecharter/hub/
│       │   ├── config/         Security and CORS settings
│       │   ├── controllers/    REST endpoints
│       │   ├── dto/            Request and response objects
│       │   ├── exceptions/     Custom errors and global handler
│       │   ├── models/         Database entities
│       │   ├── repositories/   Database queries
│       │   ├── security/       JWT, login and rate limiting
│       │   ├── services/       Business logic
│       │   └── utils/          Input cleaning
│       └── resources/
│           └── db/migration/   Database scripts
├── bruno/              API test collection
└── docs/               User stories and requirements
```

---

## Roadmap

- [x] Dive sites and trips with pagination
- [x] Registration, JWT login and roles
- [x] CORS, rate limiting and input sanitization
- [x] Bookings with certification and capacity rules
- [x] Admin certification verification and passenger manifests
- [ ] Unit tests with 70%+ coverage
- [ ] React frontend
