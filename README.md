# carRental — Technical Reference

Spring Boot 4.0.6 / Java 25 / PostgreSQL car rental backend. JWT-stateless auth, Spring Data JPA, Spring Cache (ConcurrentMap), pessimistic locking for reservations.

## Stack

| Concern | Choice |
|---|---|
| Framework | Spring Boot 4.0.6 (webmvc) |
| Java | 25 |
| DB | PostgreSQL (prod: default port, test: port 5433 via Docker `db-test`) |
| ORM | Spring Data JPA + Hibernate |
| Auth | JWT (jjwt 0.12.6), stateless sessions, `BCryptPasswordEncoder` |
| Validation | Jakarta Validation (`@Valid`, `@NotBlank`, etc.) |
| Cache | Spring Cache (`@EnableCaching`), `ConcurrentMapCacheManager`, cache name `"listing"` |
| Lombok | `@Getter`, `@Setter`, `@NoArgsConstructor` on entities |
| Tests | JUnit 5 + Mockito + MockMvc + `@SpringBootTest` |

## Package Structure

```
dev.fascodes.carRental
├── common
│   ├── config/       SecurityConfig, CacheConfig
│   ├── exception/    GlobalExceptionHandler, ApiErrorResponse
│   └── security/     JwtAuthenticationFilter
├── user/             User, UserRole, UserPrincipal, AuthController, UserService
├── car/              Car, GearboxType, CarController, CarService, CarMapper
├── listing/          Listing, ListingStatus, ListingController, ListingService
│   └── specification/ ListingSpecification (JPA Specs for filter)
└── reservation/      Reservation, ReservationStatus, ReservationController, ReservationService
```

## Domain Model

```
User (users)
  id, username, email, password (bcrypt), role (UserRole)

UserRole: USER | ADMIN

Car (cars)
  id, owner→User, brand, model, modelYear, vin, seatNumber,
  gearboxType (GearboxType), horsePower, avgLiters (BigDecimal), info

GearboxType: MANUAL | AUTOMATIC (PostgreSQL native enum, @JdbcType(PostgreSQLEnumJdbcType))

Listing (listings)
  id, car→Car, user→User (owner), price (Integer), status (ListingStatus),
  title, body (text), createdAt, lastActive

ListingStatus: ACTIVE | INACTIVE

Reservation (reservations)
  id, listing→Listing, owner→User, renter→User,
  status (ReservationStatus), dateStart (LocalDateTime), dateEnd (LocalDateTime), createdAt

ReservationStatus: PENDING | CONFIRMED | ACTIVE | CANCELLED | COMPLETED
```

All enum columns use `@JdbcType(PostgreSQLEnumJdbcType.class)` for PostgreSQL native enum mapping.

## Security Model

- `POST /api/auth/**` — public (permitAll)
- All other endpoints — require JWT (`Authorization: Bearer <token>`)
- JWT subject = user email; extracted in `JwtAuthenticationFilter`, stored in `SecurityContext`
- `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` for admin-only endpoints
- `UserPrincipal` uses `ROLE_` prefix, so `hasRole('ADMIN')` matches `ROLE_ADMIN`
- Auth errors: 401 via `authenticationEntryPoint → response.sendError(SC_UNAUTHORIZED)`
- Email read pattern in controllers: `SecurityContextHolder.getContext().getAuthentication().getName()`

## API Endpoints

### Auth — `POST /api/auth`
| Method | Path | Access | Notes |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Returns JWT |
| POST | `/api/auth/login` | Public | Returns JWT |

### Car — `/api/car`
| Method | Path | Access | Returns | Notes |
|---|---|---|---|---|
| POST | `/add` | Authenticated | `AddCarResponse` | Owner = authenticated user |
| DELETE | `/{id}` | Owner | 204 | NOT_FOUND if not owner; 409 if car has any listing |
| GET | `/{id}` | Authenticated | `CarDetailResponse` | Client-facing view (no VIN, no id) |
| PATCH | `/{id}` | Owner | `CarResponse` | NOT_FOUND if not owner; nullable patch |
| PATCH | `/{id}/admin` | ADMIN | `CarResponse` | No ownership check; cannot change owner |

**DTOs:**
- `AddCarRequest` — full car fields with `@Valid`
- `AddCarResponse` — confirmation after creation
- `CarDetailResponse` — brand, model, modelYear, gearboxType, seatNumber, horsePower, avgLiters, info (NO vin, NO id)
- `CarResponse` — brand, model, modelYear, gearboxType (brief confirmation)
- `PatchCarRequest` — all nullable; `gearboxType` is direct `GearboxType` enum

**Service rules:**
- `removeCar`: 409 if `listingRepository.existsByCarId(carId)` (any listing, any status)
- `patchCar` / `patchCarAdmin`: shared `private applyPatch(Car, PatchCarRequest)` helper
- TODO: DB trigger/mechanism for cascading car deletion with archive of FK listing data

### Listing — `/api/listing`
| Method | Path | Access | Returns | Notes |
|---|---|---|---|---|
| POST | `/add` | Authenticated | `AddListingResponse` | Car must belong to calling user |
| DELETE | `/{id}` | Owner | 204 | 403 if not owner; 409 if PENDING/CONFIRMED reservations exist |
| GET | `/filter` | Authenticated | `Page<ListingResponse>` | Params: `brand`, `priceMax`, `Pageable` |
| GET | `/{id}` | Authenticated | `ListingResponse` | INACTIVE listings: NOT_FOUND unless owner |
| PATCH | `/{id}` | Owner | `ListingResponse` | NOT_FOUND if not owner; nullable patch |

**Caching:**
- `getListing` → `@Cacheable(value="listing", key="#listingId", condition="#result.status.name()=='ACTIVE'")`
- `updateListing` → `@CacheEvict(value="listing", key="#listingId")`
- Cache: `ConcurrentMapCacheManager("listing")` in `CacheConfig`

**PATCH fields:** carId (Long, verified ownership), title, price, body, status (all nullable)
- carId patch: verifies car exists AND `car.owner.email == caller email` → NOT_FOUND otherwise

**`getListings` — JPA Specification chain:**
- Always: `ListingSpecification.isActive()` (status = ACTIVE)
- Optional: `priceAtMost(priceMax)`, `hasBrand(brand)`

TODO: listings ACTIVE for 30 days maximum (scheduling not yet implemented)

### Reservation — `/api/reservation`
| Method | Path | Access | Returns | Notes |
|---|---|---|---|---|
| POST | `/` | Authenticated | `ReservationResponse` | Renter = caller; conflict check on CONFIRMED |
| POST | `/{id}/confirm` | Owner | `ReservationResponse` | Pessimistic lock; cancels overlapping PENDING |
| PATCH | `/{id}/admin` | ADMIN | `ReservationResponse` | Set any status, no ownership check |
| PATCH | `/cancel/{id}` | Renter only | `ReservationResponse` | 403 if not renter; 409 if not PENDING/CONFIRMED |
| GET | `/my/owner` | Authenticated | `List<ReservationResponse>` | Optional `?status=` param |
| GET | `/my/renter` | Authenticated | `List<ReservationResponse>` | Optional `?status=` param |
| GET | `/admin` | ADMIN | `List<ReservationResponse>` | Required `?email=`, optional `?status=` |

**ReservationResponse fields:** id, listingId, status, dateStart, dateEnd, createdAt

**Race condition protection:**
- `addReservation` + `confirmReservation`: `listingRepository.findByIdWithLock(id)` — `@Lock(PESSIMISTIC_WRITE)`
- Overlap check: `existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(listingId, CONFIRMED, reqEnd, reqStart)`
- On confirm: `cancelOverlappingPending(listingId, dateStart, dateEnd, excludeId)` — bulk JPQL UPDATE

**Repository query methods:**
```java
// Derived
findByOwner_EmailOrderByDateStartAsc(String email)
findByOwner_EmailAndStatusOrderByDateStartAsc(String email, ReservationStatus status)
findByRenter_EmailOrderByDateStartAsc(String email)
findByRenter_EmailAndStatusOrderByDateStartAsc(String email, ReservationStatus status)

// JPQL @Query (OR across owner/renter)
findAllByUserEmail(String email)
findAllByUserEmailAndStatus(String email, ReservationStatus status)

// Existence checks
existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(...)
existsByListingIdAndStatusIn(Long listingId, List<ReservationStatus> statuses)
```

## Test Structure

All tests require Docker test DB on port 5433 (db-test container).

| File | Type | Coverage |
|---|---|---|
| `car/service/CarServiceTest` | Unit (Mockito) | addCar, removeCar, getCar, patchCar, patchCarAdmin |
| `car/controller/CarControllerTest` | Controller security (MockMvc) | getCar 401/200, patchCar 401/200, patchCarAdmin 401/403/200 |
| `listing/service/ListingServiceTest` | Unit (Mockito) | addListing, removeListing, getListing, updateListing |
| `reservation/service/ReservationServiceTest` | Unit (Mockito) | addReservation, confirmReservation, patchStatusAdmin, cancelReservation, getAsOwner/Renter/Admin |
| `reservation/repository/ReservationRepositoryTest` | Integration (@SpringBootTest) | findAllByUserEmail, findAllByUserEmailAndStatus |
| `reservation/controller/ReservationControllerTest` | Controller security (MockMvc) | cancel 200, patchAdmin 403/200, getOwner/Renter 200, getAdmin 403/200 |

**Controller test pattern:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
// + @MockitoBean [Service] + @WithMockUser / @WithMockUser(roles="ADMIN")
// Requires @Import(SecurityConfig.class) if not auto-loaded
```

**Unit test pattern:**
```java
@ExtendWith(MockitoExtension.class)
// + @Mock dependencies + @InjectMocks service
```

## Key Design Decisions

- **Owner identity**: always verified by email comparison (`entity.owner.email.equals(callerEmail)`), never by ID
- **NOT_FOUND vs FORBIDDEN**: endpoints where existence itself is sensitive return NOT_FOUND for unauthorized callers (car ownership, listing ownership in update)
- **Ownership in cancel**: only renter can cancel; owner has no cancel path (admin `patchStatusAdmin` covers edge cases)
- **Listing delete guard**: blocks on PENDING or CONFIRMED reservations; ACTIVE/CANCELLED/COMPLETED are ignored
- **Car delete guard**: blocks on ANY listing (status irrelevant); no cascade implemented yet
- **VIN excluded** from client-facing `CarDetailResponse` (sensitive, insurance/fraud risk)
- **No owner change**: `patchCarAdmin` applies only car fields; `ownerId` is never touched

## Open TODOs (in code)

1. `SecurityConfig`: rate limiting for all endpoints; retry limit (3/min) for `/api/auth`
2. `CarController`: DB trigger/mechanism for cascading car deletion with archival of listing FKs
3. `ListingController`: scheduling — listings ACTIVE max 30 days, owner must extend
4. `ListingService.getListings`: date availability filter (not yet implemented in Specification)
