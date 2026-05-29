# carRental

Platforma wypożyczalni samochodów — właściciele mogą wystawiać samochody na wynajem, renters przeglądać i rezerwować pojazdy, a system zarządza dwuetapowym procesem potwierdzenia rezerwacji z obsługą powiadomień w czasie rzeczywistym.

---

## Struktura projektu

```
carRental/
├── backend/            Aplikacja Spring Boot (Java 25, Maven)
├── frontend/           Aplikacja React (Vite, React Router, Axios)
├── docker-compose.yaml Środowisko deweloperskie (PostgreSQL + RabbitMQ)
├── init.sql            Schemat bazy danych
├── seeding.sql         Dane testowe (tylko środowisko dev)
└── README.md
```

---

## Backend

### Technologie

| Warstwa | Wybór |
|---|---|
| Framework | Spring Boot 4.0.6 (webmvc) |
| Język | Java 25 |
| Baza danych | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Autoryzacja | JWT stateless (jjwt 0.12.6), BCrypt |
| Kolejki | RabbitMQ (Spring AMQP) |
| Cache | Spring Cache (ConcurrentMapCacheManager) |
| Walidacja | Jakarta Validation |

### Uruchomienie

**Wymagania:** Docker, JDK 25, Maven

```bash
# 1. Uruchom infrastrukturę (PostgreSQL + RabbitMQ)
docker compose up -d

# 2. Uruchom aplikację
cd backend
mvn spring-boot:run
```

Zmienne środowiskowe (plik `.env` w katalogu głównym):
```
DB_PORT=5432
POSTGRES_DB=carRental
POSTGRES_USER=...
POSTGRES_PASSWORD=...
RABBITMQ_USER=...
RABBITMQ_PASS=...
JWT_SECRET=...
```

> **Reset bazy danych** (wymagany po zmianie schematu): `docker compose down -v && docker compose up -d`

> **Sekret JWT** — zmiana `JWT_SECRET` unieważnia wszystkie aktywne tokeny (używane przy pełnym resecie środowiska).

### Architektura

Aplikacja podzielona na domeny, każda z warstwami Controller → Service → Repository:

```
dev.fascodes.carRental
├── common/         Konfiguracja (Security, Cache, RabbitMQ), obsługa błędów, JWT
├── user/           Rejestracja, logowanie, profil zalogowanego użytkownika
├── car/            Zarządzanie samochodami właściciela
├── listing/        Ogłoszenia wynajmu z filtrowaniem i cache'm
├── reservation/    Dwuetapowy proces rezerwacji z zabezpieczeniem race condition
└── notification/   Powiadomienia konsumowane z kolejki RabbitMQ, zapis do bazy
```

### Model dziedziny

```
User         — konto użytkownika (USER / ADMIN)
Car          — pojazd przypisany do właściciela
Listing      — ogłoszenie wynajmu auta (ACTIVE / INACTIVE)
Reservation  — rezerwacja pomiędzy właścicielem a wynajmującym
Notification — powiadomienie zapisane po zdarzeniu rezerwacji
```

### Proces rezerwacji

```
[Renter] Tworzy rezerwację          → PENDING
[Renter] Potwierdza (płatność)      → RENTER_CONFIRMED
  │  (race condition guard + pessimistic lock)
  │
  ├─ [Owner] Potwierdza             → CONFIRMED
  └─ [Renter] Anuluje               → CANCELLED

[Admin] Może ustawić dowolny status bezpośrednio
```

Po każdym potwierdzeniu system publikuje zdarzenie na RabbitMQ → `NotificationConsumer` zapisuje powiadomienie do bazy danych.

### Endpointy API

Wszystkie endpointy (oprócz `/api/auth/**`) wymagają nagłówka `Authorization: Bearer <token>`.

| Zasób | Ścieżka bazowa | Opis |
|---|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` | Rejestracja i logowanie |
| User | `GET /api/user/me` | Profil zalogowanego użytkownika |
| Car | `/api/car` | CRUD samochodów właściciela |
| Listing | `/api/listing` | Przeglądanie i zarządzanie ogłoszeniami |
| Reservation | `/api/reservation` | Rezerwacje (owner / renter / admin) |
| Notification | `/api/notification` | Powiadomienia zalogowanego użytkownika |

Szczegółowe kształty requestów i responsów — patrz [Backend API Reference](#backend-api-reference) poniżej.

### Testy

```bash
# Wymagany kontener testowy (port 5433)
docker compose up -d db-test

cd backend
mvn test
```

| Typ | Zakres |
|---|---|
| Testy jednostkowe (Mockito) | CarService, ListingService, ReservationService |
| Testy kontrolerów (MockMvc) | Bezpieczeństwo endpointów (401/403/200) dla Car, Reservation |
| Testy integracyjne (SpringBootTest) | ReservationRepository (zapytania JPQL) |
| Test współbieżności | Race condition przy potwierdzaniu rezerwacji (pesymistyczny lock) |

---

## Frontend

### Technologie

| Warstwa | Wybór |
|---|---|
| Framework | React 18 |
| Build | Vite 5 |
| Routing | React Router 6 |
| HTTP | Axios |
| Styl | CSS (czysty, bez frameworka) |

### Uruchomienie

**Wymagania:** Node.js 18+, uruchomiony backend (`mvn spring-boot:run`)

```bash
cd frontend
npm install
npm run dev     # dev server na http://localhost:5173
```

Vite proxy przekierowuje `/api/*` → `http://localhost:8080` — brak problemów z CORS w środowisku deweloperskim.

### Struktura

```
frontend/src/
├── api/              Funkcje HTTP pogrupowane per domena — jedyne miejsce wywołań Axios
│   ├── axiosInstance.js   Instancja z interceptorami (token + globalny 401)
│   ├── auth.js
│   ├── cars.js
│   ├── listings.js
│   ├── notifications.js
│   ├── reservations.js
│   └── user.js
├── components/
│   ├── ListingCard.jsx
│   ├── NavBar.jsx         Dzwonek powiadomień z badge i dropdownem
│   ├── ProtectedRoute.jsx (ProtectedRoute + AdminRoute)
│   └── ReservationCard.jsx
├── context/
│   └── AuthContext.jsx    Token, rola, login/logout
├── pages/                 Jeden plik = jeden widok (Route)
├── router/
│   └── AppRouter.jsx
└── utils/
    ├── apiError.js        Stripowanie prefiksu "NNN STATUS" z ResponseStatusException
    └── date.js            Formatowanie dat DD.MM.YYYY HH:mm
```

### Kluczowe decyzje techniczne

**Interceptory Axios (`axiosInstance.js`)**
Token JWT dołączany automatycznie do każdego requestu. Odpowiedź 401 czyści localStorage i robi `window.location.href = '/login'` — globalna obsługa wygaśnięcia sesji bez powielania kodu w każdym komponencie.

**JWT dekodowany po stronie frontu (`AuthContext`)**
Payload tokenu dekodowany bez weryfikacji sygnatury — wyłącznie do odczytu `role` na potrzeby renderowania UI (ukrywanie elementów admina). Weryfikacja odbywa się zawsze po stronie backendu.

**Rola w rezerwacji przez `location.state`**
`ReservationResponse` zwraca `ownerUsername`/`renterUsername` (display names), JWT zawiera email — nie można ich bezpośrednio porównać. Zamiast tego rola (`'owner'`/`'renter'`) jest przekazywana przez React Router `state` przy nawigacji z list rezerwacji i po tworzeniu rezerwacji. Bezpośrednie wejście w URL (zakładka) pokazuje wszystkie przyciski pasujące do statusu — backend egzekwuje autoryzację przez 403.

**Vite proxy zamiast absolutnego URL**
`baseURL: ''` + proxy w `vite.config.js` eliminuje CORS w devsie. W produkcji aplikację należy serwować z tego samego hosta co backend lub skonfigurować CORS po stronie serwera.

**Parsowanie błędów API (`utils/apiError.js`)**
Spring's `ResponseStatusException.getMessage()` zwraca format `"409 CONFLICT \"reason\""`. Regex `replace(/^\d{3}\s+\S+\s*/i, '').replace(/^"|"$/g, '')` stripuje prefiks statusu i cudzysłowy, zostawiając sam komunikat dla użytkownika.

### Widoki

| Ścieżka | Widok | Dostęp |
|---|---|---|
| `/login` | Logowanie | Publiczny |
| `/register` | Rejestracja | Publiczny |
| `/` | Lista ogłoszeń + filtry | Zalogowany |
| `/listing/:id` | Szczegół ogłoszenia + formularz rezerwacji | Zalogowany |
| `/listing/:id/edit` | Edycja ogłoszenia | Właściciel |
| `/panel` | Dashboard (username z `/api/user/me`) | Zalogowany |
| `/panel/cars` | Lista własnych samochodów | Zalogowany |
| `/panel/cars/add` | Dodaj samochód | Zalogowany |
| `/panel/cars/:id` | Szczegół i edycja samochodu | Zalogowany |
| `/panel/listings` | Lista własnych ogłoszeń | Zalogowany |
| `/panel/listings/add` | Dodaj ogłoszenie | Zalogowany |
| `/panel/reservations/owner` | Rezerwacje jako właściciel | Zalogowany |
| `/panel/reservations/renter` | Rezerwacje jako najemca | Zalogowany |
| `/panel/reservations/:id` | Szczegół rezerwacji + akcje | Zalogowany |
| `/admin` | Panel administratora | ADMIN |
| `/admin/reservations` | Wyszukiwanie rezerwacji po emailu | ADMIN |
| `/admin/reservations/:id` | Zmiana statusu rezerwacji | ADMIN |
| `/admin/cars/:id` | Edycja samochodu (admin) | ADMIN |

---

## Uruchomienie całego projektu

```bash
# 1. Infrastruktura
docker compose up -d

# 2. Backend (osobny terminal)
cd backend
mvn spring-boot:run

# 3. Frontend (osobny terminal)
cd frontend
npm install
npm run dev
```

Aplikacja dostępna pod `http://localhost:5173`.
Dane testowe (seed) są ładowane automatycznie przez Docker przy pierwszym uruchomieniu.

---

## Backend API Reference

### Auth

| Metoda | Ścieżka | Body | Odpowiedź |
|---|---|---|---|
| POST | `/api/auth/register` | `{username, email, password}` | string |
| POST | `/api/auth/login` | `{email, password}` | `{token}` |

### User

| Metoda | Ścieżka | Auth | Odpowiedź |
|---|---|---|---|
| GET | `/api/user/me` | User | `{username, role}` |

### Car — `/api/car`

| Metoda | Ścieżka | Auth | Odpowiedź | Uwagi |
|---|---|---|---|---|
| POST | `/add` | User | `AddCarResponse` | Owner = zalogowany |
| DELETE | `/{id}` | Owner | 204 | 409 jeśli ma ogłoszenia |
| GET | `/my` | User | `List<CarDetailResponse>` | Samochody zalogowanego |
| GET | `/{id}` | User | `CarDetailResponse` | Publiczny widok |
| PATCH | `/{id}` | Owner | `CarResponse` | Nullable patch |
| PATCH | `/{id}/admin` | ADMIN | `CarResponse` | Bez sprawdzania właściciela |

**CarDetailResponse:** `{id, brand, model, modelYear, gearboxType, vin, seatNumber, horsePower, avgLiters, info}`

### Listing — `/api/listing`

| Metoda | Ścieżka | Auth | Odpowiedź | Uwagi |
|---|---|---|---|---|
| POST | `/add` | User | `AddListingResponse` | Auto musi należeć do wywołującego |
| DELETE | `/{id}` | Owner | 204 | 409 przy aktywnych rezerwacjach |
| GET | `/my` | User | `List<ListingResponse>` | Opcjonalny `?status=` |
| GET | `/filter` | User | `Page<ListingResponse>` | Tylko ACTIVE; `brand`, `priceMax`, `Pageable` |
| GET | `/{id}` | User | `ListingDetailResponse` | INACTIVE → 404 chyba że owner |
| PATCH | `/{id}` | Owner | `ListingResponse` | Nullable patch; ewiktuje cache |

**AddListingRequest:** `{carId, title, localization, price, body, status}`
— `localization`: wymagane, tylko litery i myślniki (Unicode)

**ListingResponse:** `{id, title, localization, price, brand, model, modelYear, gearboxType, status}`

**ListingDetailResponse:** `{id, title, localization, body, price, status, ownerUsername, brand, model, modelYear, gearboxType, vin, seatNumber, horsePower, avgLiters, info}`

### Reservation — `/api/reservation`

| Metoda | Ścieżka | Auth | Odpowiedź | Uwagi |
|---|---|---|---|---|
| POST | `/` | User | `ReservationResponse` | 403 self-reservation; 400 złe daty |
| POST | `/{id}/confirm` | Renter | `ReservationResponse` | PENDING → RENTER_CONFIRMED |
| POST | `/{id}/owner-confirm` | Owner | `ReservationResponse` | RENTER_CONFIRMED → CONFIRMED |
| PATCH | `/cancel/{id}` | Renter | `ReservationResponse` | PENDING/RENTER_CONFIRMED/CONFIRMED → CANCELLED |
| PATCH | `/{id}/admin` | ADMIN | `ReservationResponse` | Dowolny status |
| GET | `/{id}` | Owner lub Renter | `ReservationResponse` | 403 dla innych |
| GET | `/my/owner` | User | `List<ReservationResponse>` | Opcjonalny `?status=` |
| GET | `/my/renter` | User | `List<ReservationResponse>` | Opcjonalny `?status=` |
| GET | `/admin` | ADMIN | `List<ReservationResponse>` | Wymagany `?email=` |

**ReservationResponse:** `{id, listingId, listingTitle, listingLocalization, status, dateStart, dateEnd, ownerUsername, renterUsername}`

**Statusy:** `PENDING` → `RENTER_CONFIRMED` → `CONFIRMED` → `ACTIVE` → `COMPLETED` / `CANCELLED`

### Notification — `/api/notification`

| Metoda | Ścieżka | Auth | Odpowiedź | Uwagi |
|---|---|---|---|---|
| GET | `/my` | User | `List<NotificationResponse>` | Najnowsze pierwsze |
| PATCH | `/{id}/read` | User | `NotificationResponse` | 403 jeśli nie adresat |

**NotificationResponse:** `{id, message, reservationId, read, createdAt}`
