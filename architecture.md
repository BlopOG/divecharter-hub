# DiveMatrix: Architecture

## System overview

DiveMatrix has three parts: a React single-page app, a Spring Boot REST API, and a MySQL database. The browser only ever talks to the API; the API is the only thing that talks to the database.

```mermaid
flowchart LR
    U([Diver / Admin<br/>in a browser])

    subgraph FE["Frontend: React + Vite (localhost:5173)"]
        UI[Pages and components]
        AC[Auth context<br/>token + user]
        CL[API client<br/>fetch + error handling]
    end

    subgraph BE["Backend: Spring Boot API (localhost:8080)"]
        direction TB
        SEC[Security filters<br/>rate limit, CORS, JWT]
        CTL[Controllers]
        SVC[Services<br/>business rules]
        REP[Repositories<br/>Spring Data JPA]
        EH[GlobalExceptionHandler]
        SEC --> CTL --> SVC --> REP
        CTL -.errors.-> EH
        SVC -.errors.-> EH
    end

    DB[(MySQL 8<br/>schema managed by Flyway)]

    U --> UI
    UI --> AC
    UI --> CL
    CL -- "HTTP + JSON<br/>Authorization: Bearer JWT" --> SEC
    REP --> DB
```

## Backend layers

| Layer | Package | Responsibility |
|---|---|---|
| Security | `security`, `config` | Rate-limit login/register, enforce CORS, verify JWTs, apply role rules |
| Controller | `controllers` | Map HTTP requests to service calls; validate input with `@Valid` |
| Service | `services` | Business rules (the 5 booking rules, cancellation, verification) inside `@Transactional` methods |
| Repository | `repositories` | Database queries via Spring Data JPA, including custom JPQL with `JOIN FETCH` |
| Model | `models` | JPA entities mapped to tables |
| DTO | `dto` | JSON shapes in and out, grouped by feature; entities are never exposed |
| Errors | `exceptions` | Custom exceptions and one handler that returns a consistent JSON error |

## Request flow: booking a trip

```mermaid
sequenceDiagram
    actor D as Diver
    participant R as React app
    participant F as JwtAuthFilter
    participant C as BookingController
    participant S as BookingService
    participant DB as MySQL

    D->>R: Click "Book"
    R->>F: POST /api/bookings {tripId}<br/>Authorization: Bearer JWT
    F->>F: Verify signature + expiry,<br/>load user
    F->>C: Authenticated request
    C->>S: create(userId from token, request)
    S->>DB: Load diver and trip (with site)
    S->>S: Rule 1: certification verified?
    S->>S: Rule 2: certification deep enough?
    S->>S: Rule 3: trip scheduled and in future?
    S->>DB: Rule 4: already booked?
    S->>S: Rule 5: reserve seat (trip not full?)
    S->>DB: Save booking + seat count (one transaction)
    S-->>C: BookingResponse
    C-->>R: 201 Created
    R-->>D: "You're booked!"
    Note over S,DB: Any failed rule throws, the transaction rolls back,<br/>and GlobalExceptionHandler returns 403 / 409 / 422
```

## Frontend component diagram

```mermaid
flowchart TB
    main[main.jsx] --> BR[BrowserRouter]
    BR --> AP[AuthProvider<br/>user, token, login, logout]
    AP --> App[App.jsx: routes]
    App --> L[Layout<br/>navbar + footer]

    L --> Home[HomePage /]
    L --> Sites[SitesPage /sites]
    L --> Trips[TripsPage /trips]
    L --> Detail[TripDetailPage /trips/:id]
    L --> Login[LoginPage /login]
    L --> Reg[RegisterPage /register]
    L --> PR1[ProtectedRoute]
    L --> PR2[ProtectedRoute adminOnly]
    L --> NF[NotFoundPage *]

    PR1 --> MyB[MyBookingsPage /my-bookings]
    PR2 --> Admin[AdminPage /admin]
    Admin --> VP[VerificationPanel]
    Admin --> MP[ManifestPanel]

    Sites --> SI[SiteImage]
    Trips --> SI
    Detail --> SI

    API[(api/client.js)]
    Sites -.-> API
    Trips -.-> API
    Detail -.-> API
    MyB -.-> API
    VP -.-> API
    MP -.-> API
    AP -.-> API
```

Every page that needs data calls the backend through `api/client.js`, which adds the JWT, turns error responses into readable messages, and logs the user out if a token has expired. `ProtectedRoute` hides pages for usability; the backend enforces access on every request.

## Key design decisions

| Decision | Reason |
|---|---|
| Stateless JWT instead of server sessions | Each request proves who it is, so the server stores no session state |
| CSRF protection disabled | Tokens travel in a header, not cookies, so CSRF does not apply (reviewed in SonarQube) |
| DTOs instead of returning entities | Controls the API's JSON exactly and never exposes fields like password hashes |
| Optimistic locking (`@Version`) on trips | Two people booking the last seat at once cannot both succeed |
| Flyway migrations | The database schema is versioned with the code and reproducible on any machine |
| Business rules in the service layer | Rules live in one place and are unit tested without a database |
