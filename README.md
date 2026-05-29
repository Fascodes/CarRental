# carRental — Technical Reference

Full-stack car rental application. Backend: Spring Boot / PostgreSQL / RabbitMQ / JWT. Frontend: React 18 / Vite / Axios.

---

## Project Layout

```
carRental/
├── backend/          Spring Boot 4.0.6, Java 25, Maven
├── frontend/         React 18, Vite 5, React Router 6, Axios
├── docker-compose.yaml
├── init.sql          DB schema (enums + tables + FK constraints)
├── seeding.sql       Dev seed data (mounted only on db, not db-test)
└── README.md
```

---

## Backend

### Stack

| Concern | Detail |
|---|---|
| Framework | Spring Boot 4.0.6 (webmvc) |
| Java | 25 |
| DB | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Auth | JWT stateless (jjwt 0.12.6), `BCryptPasswordEncoder` |
| Messaging | RabbitMQ via `spring-boot-starter-amqp`, `JacksonJsonMessageConverter` |
| Cache | Spring Cache, `ConcurrentMapCacheManager`, cache name `"listing"` |
| Validation | Jakarta Validation (`@Valid`, `@NotNull`, `@AssertTrue`, `@Pattern`) |
| Lombok | `@Getter`, `@Setter`, `@NoArgsConstructor` on entities |

### Infrastructure

```yaml
db:       PostgreSQL 16, port from .env (${DB_PORT}), volumes: init.sql + seeding.sql
db-test:  PostgreSQL 16, port 5433, volumes: init.sql only (no seed data)
rabbitmq: (external / local) — default port 5672
```

Reset dev DB (required after schema changes): `docker compose down -v && docker compose up -d`

### Package Structure

```
dev.fascodes.carRental
├── common
│   ├── config/       SecurityConfig (@EnableMethodSecurity), CacheConfig, RabbitConfig
│   ├── exception/    GlobalExceptionHandler, ApiErrorResponse
│   ├── security/     JwtAuthenticationFilter, AuthenticatedUser record(email, role)
│   └── utility/      JwtUtil
├── user/             User, UserRole(USER|ADMIN), AuthController, UserService
├── car/              Car, GearboxType, CarController, CarService, CarMapper
├── listing/          Listing, ListingStatus, ListingController, ListingService
│   └── specification/ ListingSpecification (JPA Specs for browse filter)
├── reservation/      Reservation, ReservationStatus, ReservationController, ReservationService
└── notification/
    ├── controller/   NotificationController
    ├── dto/          NotificationResponse
    ├── model/        Notification
    ├── repository/   NotificationRepository
    ├── service/      NotificationService, NotificationConsumer (@RabbitListener)
    └── ReservationNotificationEvent  (record, published by ReservationService)
```

**`AuthenticatedUser`** is a record `(String email, String role)` injected via `@AuthenticationPrincipal` in all controllers.

### Domain Model

```
User            id, username, email, password(bcrypt), role(UserRole)

Car             id, owner→User, brand, model, modelYear, vin(17), seatNumber,
                gearboxType(GearboxType), horsePower, avgLiters(numeric 3,2), info(text)

Listing         id, car→Car, user→User(owner), price(int), status(ListingStatus),
                title, localization, body(text), createdAt, lastActive

Reservation     id, listing→Listing, owner→User, renter→User,
                status(ReservationStatus), dateStart, dateEnd, createdAt

Notification    id, userEmail(varchar), message(varchar), reservationId(nullable FK),
                isRead(boolean, default false), createdAt

UserRole:          USER | ADMIN
GearboxType:       MANUAL | AUTOMATIC
ListingStatus:     ACTIVE | INACTIVE
ReservationStatus: PENDING | RENTER_CONFIRMED | CONFIRMED | ACTIVE | CANCELLED | COMPLETED
```

All enum columns: `@JdbcType(PostgreSQLEnumJdbcType.class)` — PostgreSQL native enum type.

### Security

- `POST /api/auth/**` — public
- All other endpoints — `Authorization: Bearer <jwt>` required → 401 if missing
- JWT subject = user email
- `@PreAuthorize("hasRole('ADMIN')")` for admin endpoints — role stored as `ROLE_ADMIN`
- All controllers use `@AuthenticationPrincipal AuthenticatedUser user` → `user.email()`

### Messaging — RabbitMQ

```
Exchange:    reservation.exchange  (TopicExchange)
Queue:       notification.reservation  (durable)
Routing key: reservation.confirmed
Converter:   JacksonJsonMessageConverter (tools.jackson / Jackson 3)
```

**Flow:**
1. `ReservationService` publishes `ReservationNotificationEvent` after renter or owner confirm
2. `NotificationConsumer` receives from queue → builds human-readable message → saves `Notification` to DB

**Event payload** (`ReservationNotificationEvent` record):
```json
{ "reservationId", "recipientEmail", "recipientUsername", "listingTitle", "dateStart", "dateEnd", "type" }
```
`type`: `"RENTER_CONFIRMED"` (sent to owner) | `"CONFIRMED"` (sent to renter)

**Saved messages:**
- `RENTER_CONFIRMED` → *"New reservation request for '{title}' ({start} – {end}) awaiting your confirmation."*
- `CONFIRMED` → *"Your reservation for '{title}' ({start} – {end}) has been confirmed by the owner."*

---

## API Reference

### Auth

| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/auth/register` | Public | `{username, email, password}` | string |
| POST | `/api/auth/login` | Public | `{email, password}` | `{token}` |

---

### Car — `/api/car`

| Method | Path | Auth | Returns | Notes |
|---|---|---|---|---|
| POST | `/add` | User | `AddCarResponse` | Owner = caller |
| DELETE | `/{id}` | Owner | 204 | 404 if not owner; 409 if car has any listing |
| GET | `/my` | User | `List<CarDetailResponse>` | All cars owned by caller |
| GET | `/{id}` | User | `CarDetailResponse` | No ownership check |
| PATCH | `/{id}` | Owner | `CarResponse` | 404 if not owner; all fields nullable |
| PATCH | `/{id}/admin` | ADMIN | `CarResponse` | No ownership check; owner never changed |

**AddCarRequest** (POST /add):
```json
{ "brand", "model", "modelYear", "vin", "seatNumber", "gearboxType"("MANUAL"|"AUTOMATIC"), "horsePower", "avgLiters", "info" }
```

**PatchCarRequest** (PATCH /{id}, /{id}/admin) — all nullable:
```json
{ "brand"?, "model"?, "modelYear"?, "vin"?, "seatNumber"?, "gearboxType"?, "horsePower"?, "avgLiters"?, "info"? }
```

**CarDetailResponse** (GET /my, GET /{id}):
```json
{ "id", "brand", "model", "modelYear", "gearboxType", "vin", "seatNumber", "horsePower", "avgLiters", "info" }
```

**CarResponse** (PATCH responses):
```json
{ "brand", "model", "modelYear", "gearboxType" }
```

---

### Listing — `/api/listing`

| Method | Path | Auth | Returns | Notes |
|---|---|---|---|---|
| POST | `/add` | User | `AddListingResponse` | Car must belong to caller (403 otherwise) |
| DELETE | `/{id}` | Owner | 204 | 403 if not owner; 409 if PENDING/RENTER_CONFIRMED/CONFIRMED reservations exist |
| GET | `/my` | User | `List<ListingResponse>` | Own listings; optional `?status=ACTIVE\|INACTIVE` |
| GET | `/filter` | User | `Page<ListingResponse>` | ACTIVE only; params: `brand`, `priceMax`, `page`, `size`, `sort` |
| GET | `/{id}` | User | `ListingDetailResponse` | INACTIVE → 404 unless caller is owner; result cached when ACTIVE |
| PATCH | `/{id}` | Owner | `ListingResponse` | 404 if not owner; all fields nullable; evicts cache |

**AddListingRequest** (POST /add):
```json
{
  "carId",
  "title",
  "localization",   // required — letters and hyphens only, Unicode (e.g. "Warszawa", "Nowy-Sacz")
  "price",
  "body",
  "status": "ACTIVE"|"INACTIVE"
}
```

**UpdateListingRequest** (PATCH /{id}) — all nullable:
```json
{ "carId"?, "title"?, "localization"?, "price"?, "body"?, "status"? }
```

**ListingResponse** (GET /my, GET /filter, PATCH response):
```json
{ "id", "title", "localization", "price", "brand", "model", "modelYear", "gearboxType", "status" }
```

**ListingDetailResponse** (GET /{id}):
```json
{
  "id", "title", "localization", "body", "price", "status", "ownerUsername",
  "brand", "model", "modelYear", "gearboxType", "vin",
  "seatNumber", "horsePower", "avgLiters", "info"
}
```

---

### Reservation — `/api/reservation`

#### Flow

```
[Renter] POST /               → PENDING
[Renter] POST /{id}/confirm   → RENTER_CONFIRMED
  │  pessimistic lock on listing
  │  overlap check vs CONFIRMED
  │  cancels all overlapping PENDING
  │  publishes RENTER_CONFIRMED event → notification to owner
  │
  ├─ [Owner] POST /{id}/owner-confirm → CONFIRMED
  │    simple status change, no lock
  │    publishes CONFIRMED event → notification to renter
  │
  └─ [Renter] PATCH /cancel/{id} → CANCELLED

[Renter] PATCH /cancel/{id}  valid from: PENDING, RENTER_CONFIRMED, CONFIRMED
[Admin]  PATCH /{id}/admin   can set any status directly
```

**Business rules:**
- Owner cannot reserve own listing → 403
- `dateEnd` must be after `dateStart` → 400
- Only renter can cancel → 403 for others; non-cancellable status → 409
- `addReservation` blocks if RENTER_CONFIRMED or CONFIRMED overlap exists

| Method | Path | Auth | Returns | Notes |
|---|---|---|---|---|
| POST | `/` | User | `ReservationResponse` | 403 if self-reservation; 400 if dates invalid |
| POST | `/{id}/confirm` | Renter | `ReservationResponse` | PENDING → RENTER_CONFIRMED; race condition here |
| POST | `/{id}/owner-confirm` | Owner | `ReservationResponse` | RENTER_CONFIRMED → CONFIRMED; simple |
| PATCH | `/{id}/admin` | ADMIN | `ReservationResponse` | Set any status |
| PATCH | `/cancel/{id}` | Renter | `ReservationResponse` | PENDING/RENTER_CONFIRMED/CONFIRMED → CANCELLED |
| GET | `/{id}` | Owner or Renter | `ReservationResponse` | 403 if neither |
| GET | `/my/owner` | User | `List<ReservationResponse>` | Optional `?status=` |
| GET | `/my/renter` | User | `List<ReservationResponse>` | Optional `?status=` |
| GET | `/admin` | ADMIN | `List<ReservationResponse>` | Required `?email=`, optional `?status=` |

**AddReservationRequest** (POST /):
```json
{ "listingId", "dateStart"(ISO datetime), "dateEnd"(ISO datetime) }
```

**PatchReservationStatusRequest** (PATCH /{id}/admin):
```json
{ "status": "PENDING"|"RENTER_CONFIRMED"|"CONFIRMED"|"ACTIVE"|"CANCELLED"|"COMPLETED" }
```

**ReservationResponse** (all reservation endpoints):
```json
{
  "id", "listingId", "listingTitle", "listingLocalization",
  "status": "PENDING"|"RENTER_CONFIRMED"|"CONFIRMED"|"ACTIVE"|"CANCELLED"|"COMPLETED",
  "dateStart", "dateEnd"
}
```

#### Reservation status UI guide

| Status | Renter sees | Owner sees | Available actions |
|---|---|---|---|
| `PENDING` | Awaiting your confirmation | New request | Renter: confirm or cancel |
| `RENTER_CONFIRMED` | Awaiting owner approval | Ready to confirm | Owner: confirm; Renter: cancel |
| `CONFIRMED` | Confirmed | Confirmed | Renter: cancel |
| `ACTIVE` | Active rental | Active rental | — |
| `CANCELLED` | Cancelled | Cancelled | — |
| `COMPLETED` | Completed | Completed | — |

---

### Notification — `/api/notification`

| Method | Path | Auth | Returns | Notes |
|---|---|---|---|---|
| GET | `/my` | User | `List<NotificationResponse>` | All notifications for caller, newest first |
| PATCH | `/{id}/read` | User | `NotificationResponse` | 403 if not recipient; marks as read |

**NotificationResponse**:
```json
{ "id", "message", "reservationId", "read"(boolean), "createdAt" }
```

---

## Frontend

### Stack

| Concern | Detail |
|---|---|
| Framework | React 18 |
| Build | Vite 5 |
| Routing | React Router 6 |
| HTTP | Axios (`src/api/axiosInstance.js`) |

### Structure

```
frontend/src/
├── api/
│   ├── axiosInstance.js
│   ├── auth.js            register, login
│   ├── cars.js            addCar, deleteCar, getMyCars, getCar, patchCar, patchCarAdmin
│   ├── listings.js        addListing, deleteListing, filterListings, getListing, getMyListings(params), patchListing
│   └── reservations.js    createReservation, confirmReservation (renter), ownerConfirmReservation,
│                          cancelReservation, getReservation,
│                          getOwnerReservations(params), getRenterReservations(params),
│                          patchReservationAdmin, getAdminReservations(params)
├── components/
│   ├── ListingCard.jsx
│   ├── NavBar.jsx
│   ├── ProtectedRoute.jsx   (ProtectedRoute + AdminRoute)
│   └── ReservationCard.jsx
├── context/
│   └── AuthContext.jsx
├── pages/
│   ├── LoginPage.jsx / RegisterPage.jsx
│   ├── HomePage.jsx                      GET /listing/filter
│   ├── ListingDetailPage.jsx             GET /listing/{id} → ListingDetailResponse
│   ├── ListingEditPage.jsx               PATCH /listing/{id}
│   ├── ListingAddPage.jsx                POST /listing/add
│   ├── PanelPage.jsx
│   ├── PanelListingsPage.jsx             GET /listing/my
│   ├── CarsPage.jsx                      GET /car/my
│   ├── CarAddPage.jsx                    POST /car/add
│   ├── CarDetailPage.jsx                 GET /car/{id}
│   ├── OwnerReservationsPage.jsx         GET /reservation/my/owner
│   ├── RenterReservationsPage.jsx        GET /reservation/my/renter
│   ├── ReservationDetailPage.jsx         GET /reservation/{id} + confirm/cancel/owner-confirm
│   ├── AdminPage.jsx
│   ├── AdminReservationsPage.jsx         GET /reservation/admin
│   ├── AdminReservationDetailPage.jsx    PATCH /reservation/{id}/admin
│   └── AdminCarDetailPage.jsx            PATCH /car/{id}/admin
├── router/
│   └── AppRouter.jsx
└── utils/
    └── date.js
```

### Routes

| Path | Component | Auth |
|---|---|---|
| `/login` | LoginPage | Public |
| `/register` | RegisterPage | Public |
| `/` | HomePage | User |
| `/listing/:id` | ListingDetailPage | User |
| `/listing/:id/edit` | ListingEditPage | User |
| `/panel` | PanelPage | User |
| `/panel/cars` | CarsPage | User |
| `/panel/cars/add` | CarAddPage | User |
| `/panel/cars/:id` | CarDetailPage | User |
| `/panel/listings` | PanelListingsPage | User |
| `/panel/listings/add` | ListingAddPage | User |
| `/panel/reservations/owner` | OwnerReservationsPage | User |
| `/panel/reservations/renter` | RenterReservationsPage | User |
| `/panel/reservations/:id` | ReservationDetailPage | User |
| `/admin` | AdminPage | ADMIN |
| `/admin/reservations` | AdminReservationsPage | ADMIN |
| `/admin/reservations/:id` | AdminReservationDetailPage | ADMIN |
| `/admin/cars/:id` | AdminCarDetailPage | ADMIN |

### Frontend API gaps (require implementation)

| Feature | Required call |
|---|---|
| Owner confirm reservation | `ownerConfirmReservation(id)` → POST `/reservation/{id}/owner-confirm` |
| List notifications | `getMyNotifications()` → GET `/notification/my` |
| Mark notification read | `markNotificationRead(id)` → PATCH `/notification/{id}/read` |
| My listings with filter | `getMyListings({ status })` → GET `/listing/my?status=` |

---

## Key Design Decisions

- **Owner identity** — always compared by email, never by ID
- **Self-reservation blocked** — 403 if `listing.owner.email == renterEmail`
- **Race condition** — pessimistic lock + overlap check at `renterConfirmReservation`; `ownerConfirmReservation` is lock-free
- **RENTER_CONFIRMED blocks new reservations** — `addReservation` rejects overlapping RENTER_CONFIRMED or CONFIRMED dates
- **Notification decoupling** — ReservationService only publishes events; NotificationConsumer handles persistence independently
- **NOT_FOUND vs FORBIDDEN** — existence-sensitive endpoints return 404 for unauthorized callers
- **INACTIVE listing** — visible only to owner via GET /{id}; excluded from GET /filter
- **Car delete guard** — blocked by ANY listing regardless of status
- **Listing delete guard** — blocked by PENDING, RENTER_CONFIRMED, or CONFIRMED reservations
- **JWT invalidation** — manual secret rotation (option 4) for dev resets; refresh tokens or token versioning deferred

---

## Open TODOs

1. Rate limiting + auth retry limit (3/min) — `SecurityConfig`
2. DB trigger for cascading car deletion with listing archival — `CarController`
3. Listing auto-expiry — 30 days ACTIVE max, owner-extendable — `ListingController`
4. Date availability filter on GET /listing/filter — `ListingSpecification`
5. Email sending via JavaMailSender — `NotificationConsumer`
