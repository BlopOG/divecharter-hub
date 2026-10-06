# DiveCharter Hub: User Stories and Requirements

This document describes who uses DiveCharter Hub, what they need it to do, and the requirements those needs create.

---

## Personas

| Persona | Role | Description |
|---|---|---|
| **Visitor** | none | Anyone browsing the site without an account |
| **Diver** | `USER` | A registered diver who books trips |
| **Admin** | `ADMIN` | Dive shop staff who manage sites, verify certifications and run trips |

---

## Story summary

| ID | Story | Persona | Priority | Status |
|---|---|---|---|---|
| [US-01](#us-01-browse-dive-sites) | Browse dive sites | Visitor | Must | ✅ Done |
| [US-02](#us-02-browse-upcoming-trips) | Browse upcoming trips | Visitor | Must | ✅ Done |
| [US-03](#us-03-register-an-account) | Register an account | Diver | Must | ✅ Done |
| [US-04](#us-04-log-in-securely) | Log in securely | Diver | Must | ✅ Done |
| [US-05](#us-05-book-a-seat-on-a-trip) | Book a seat on a trip | Diver | Must | ✅ Done |
| [US-06](#us-06-be-protected-from-unsafe-bookings) | Be protected from unsafe bookings | Diver | Must | ✅ Done |
| [US-07](#us-07-get-clear-booking-errors) | Get clear booking errors | Diver | Must | ✅ Done |
| [US-08](#us-08-view-my-bookings) | View my bookings | Diver | Must | ✅ Done |
| [US-09](#us-09-cancel-my-booking) | Cancel my booking | Diver | Must | ✅ Done |
| [US-10](#us-10-manage-dive-sites) | Manage dive sites | Admin | Must | ✅ Done |
| [US-11](#us-11-verify-diver-certifications) | Verify diver certifications | Admin | Must | 🕒 Planned |
| [US-12](#us-12-view-a-trips-passenger-manifest) | View a trip's passenger manifest | Admin | Should | 🕒 Planned |
| [US-13](#us-13-override-cancellations) | Override cancellations | Admin | Should | ✅ Done |

**Priority key:** **Must** = required for a working product · **Should** = important but not blocking

---

## Visitor stories

### US-01: Browse dive sites

> **As a** visitor,
> **I want to** see a list of dive sites with their location, depth and difficulty,
> **so that** I can find places I'd like to dive.

**Acceptance criteria**

- [x] Anyone can view the list and a single site's details without logging in
- [x] Each site shows its maximum depth and difficulty level
- [x] An unknown site id returns a `404` error

**Priority:** Must · **Status:** ✅ Done

---

### US-02: Browse upcoming trips

> **As a** visitor,
> **I want to** see upcoming boat trips page by page, sorted by date or price,
> **so that** I can choose a trip that fits my schedule and budget.

**Acceptance criteria**

- [x] Only scheduled trips departing in the future are shown
- [x] Results are paginated (default 10 per page, maximum 50) and sortable
- [x] Each trip shows its site, boat, departure and return times, price and seats available

**Priority:** Must · **Status:** ✅ Done

---

## Diver stories

### US-03: Register an account

> **As a** diver,
> **I want to** create an account with my certification details,
> **so that** I can book trips.

**Acceptance criteria**

- [x] Email must be valid and unique (case-insensitive)
- [x] Password must be 8–72 characters and contain a letter and a number
- [x] New accounts are always role `USER` with certification **unverified**
- [x] The password is stored only as a BCrypt hash and never returned

**Priority:** Must · **Status:** ✅ Done

---

### US-04: Log in securely

> **As a** diver,
> **I want to** log in with my email and password,
> **so that** I can access my bookings.

**Acceptance criteria**

- [x] Correct credentials return a JWT token valid for 24 hours
- [x] Wrong credentials return a `401` error that doesn't reveal whether the email exists
- [x] More than 5 attempts per minute from one IP return a `429` error

**Priority:** Must · **Status:** ✅ Done

---

### US-05: Book a seat on a trip

> **As a** verified diver,
> **I want to** book a seat on an upcoming trip,
> **so that** I'm on the boat's passenger list.

**Acceptance criteria**

- [x] A booking is created with status `CONFIRMED` and the trip's price
- [x] The trip's available seats decrease by one
- [x] Booking requires login (`401` otherwise)

**Priority:** Must · **Status:** ✅ Done

---

### US-06: Be protected from unsafe bookings

> **As a** diver,
> **I want** the system to stop me booking a dive deeper than my certification allows,
> **so that** I only dive within my training.

**Acceptance criteria**

- [x] A diver whose certification isn't verified gets a `403` error
- [x] A diver whose certification depth is less than the site's maximum depth gets a `422` error explaining both depths
- [x] No seat is taken when a booking is refused

**Priority:** Must · **Status:** ✅ Done

---

### US-07: Get clear booking errors

> **As a** diver,
> **I want** clear errors when a trip is full, already departed, or already booked by me,
> **so that** I understand why a booking failed.

**Acceptance criteria**

- [x] A full trip returns `409` "fully booked"
- [x] A departed or cancelled trip returns `409`
- [x] Booking the same trip twice returns `409`
- [x] If two divers book the last seat at the same moment, only one succeeds

**Priority:** Must · **Status:** ✅ Done

---

### US-08: View my bookings

> **As a** diver,
> **I want to** see a list of my bookings, newest first,
> **so that** I can keep track of my upcoming dives.

**Acceptance criteria**

- [x] Only the logged-in diver's own bookings are shown
- [x] Each booking shows the site, boat, departure time, status and price paid

**Priority:** Must · **Status:** ✅ Done

---

### US-09: Cancel my booking

> **As a** diver,
> **I want to** cancel a booking,
> **so that** my seat is freed if my plans change.

**Acceptance criteria**

- [x] Divers can cancel only their own bookings (`403` otherwise)
- [x] Cancellation is allowed until 24 hours before departure
- [x] The booking is kept with status `CANCELLED`, and the seat is released
- [x] Cancelling twice returns `409`; the diver may book the trip again later

**Priority:** Must · **Status:** ✅ Done

---

## Admin stories

### US-10: Manage dive sites

> **As an** admin,
> **I want to** add, edit and remove dive sites,
> **so that** the catalogue stays accurate.

**Acceptance criteria**

- [x] Only admins can create, update or delete (`403` for divers)
- [x] Names must be unique; depth must be 1–40 m
- [x] A site that still has trips cannot be deleted (`409`)
- [x] HTML is stripped from text fields

**Priority:** Must · **Status:** ✅ Done

---

### US-11: Verify diver certifications

> **As an** admin,
> **I want to** see divers waiting for verification and mark their certification as verified,
> **so that** only checked divers can book.

**Acceptance criteria**

- [x] Admins can list divers whose certification is unverified
- [x] Admins can mark a diver verified or unverified
- [x] Divers cannot change their own verification status

**Priority:** Must · **Status:** 🕒 Planned

---

### US-12: View a trip's passenger manifest

> **As an** admin,
> **I want to** see everyone booked on a trip with their certification details,
> **so that** the crew knows who is on board.

**Acceptance criteria**

- [x] Shows each confirmed diver's name, email, certification level and agency
- [x] Cancelled bookings are not included
- [x] Only admins can view manifests

**Priority:** Should · **Status:** 🕒 Planned

---

### US-13: Override cancellations

> **As an** admin,
> **I want to** cancel any diver's booking at any time,
> **so that** I can handle weather cancellations and special cases.

**Acceptance criteria**

- [] Admins are not limited by the 24-hour cutoff
- [] The seat is released, as with a normal cancellation

**Priority:** Should · **Status:** ✅ Done

---

## Functional requirements

| ID | Requirement | Stories |
|---|---|---|
| FR-01 | The system shall list dive sites and return a single site by id. | US-01 |
| FR-02 | The system shall list scheduled future trips with pagination (max 50 per page) and sorting. | US-02 |
| FR-03 | The system shall register users with validated, unique, case-insensitive email addresses. | US-03 |
| FR-04 | The system shall store passwords only as BCrypt hashes. | US-03 |
| FR-05 | The system shall authenticate users and issue a signed JWT that expires after 24 hours. | US-04 |
| FR-06 | The system shall restrict endpoints by role (`USER`, `ADMIN`). | US-10, US-11, US-12 |
| FR-07 | The system shall limit login and registration attempts to 5 per minute per IP address. | US-04 |
| FR-08 | The system shall refuse bookings from divers whose certification is not verified. | US-06 |
| FR-09 | The system shall refuse bookings where the site's maximum depth exceeds the diver's certification limit. | US-06 |
| FR-10 | The system shall prevent bookings on full, cancelled or departed trips. | US-07 |
| FR-11 | The system shall prevent a diver from holding two confirmed bookings on the same trip. | US-07 |
| FR-12 | The system shall prevent overbooking under concurrent requests using optimistic locking. | US-07 |
| FR-13 | The system shall let divers cancel their own bookings until 24 hours before departure, and admins at any time. | US-09, US-13 |
| FR-14 | The system shall let admins create, update and delete dive sites, refusing deletion of sites with trips. | US-10 |
| FR-15 | The system shall let admins view and update divers' certification verification status. | US-11 |
| FR-16 | The system shall provide a passenger manifest for each trip to admins. | US-12 |
| FR-17 | The system shall return all errors in a consistent JSON format with an appropriate HTTP status. | All |

---

## Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-01 | Browser access to the API is restricted to the frontend origin (CORS). |
| NFR-02 | Free-text input is sanitized to remove HTML tags. |
| NFR-03 | Secrets (database password, JWT key) are never committed to source control. |
| NFR-04 | The database schema is versioned and reproducible through Flyway migrations. |
| NFR-05 | List endpoints load related data in a single query (no N+1 queries). |
| NFR-06 | Backend unit tests reach at least 70% line coverage. |
