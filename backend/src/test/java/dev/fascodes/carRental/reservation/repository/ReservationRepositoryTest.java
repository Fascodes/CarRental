package dev.fascodes.carRental.reservation.repository;

import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.model.GearboxType;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.model.ListingStatus;
import dev.fascodes.carRental.listing.repository.ListingRepository;
import dev.fascodes.carRental.reservation.model.Reservation;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.model.UserRole;
import dev.fascodes.carRental.user.repository.UserRepository;
import dev.fascodes.carRental.util.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Spins up a Postgres Testcontainer (see AbstractIntegrationTest); no manually-started DB required.
class ReservationRepositoryTest extends AbstractIntegrationTest {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CarRepository carRepository;
    @Autowired private ListingRepository listingRepository;

    private User owner;
    private User renter1;
    private User renter2;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(buildUser("owner@repo.test", "ownerRepo"));
        renter1 = userRepository.save(buildUser("renter1@repo.test", "renter1Repo"));
        renter2 = userRepository.save(buildUser("renter2@repo.test", "renter2Repo"));

        Car car = new Car();
        car.setOwner(owner);
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setModelYear(2021);
        car.setVin("REPOTEST00000001A");
        car.setSeatNumber(5);
        car.setGearboxType(GearboxType.MANUAL);
        car.setHorsePower(120);
        car = carRepository.save(car);

        Listing listing = new Listing();
        listing.setCar(car);
        listing.setUser(owner);
        listing.setTitle("Repo test listing");
        listing.setPrice(80);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setCreatedAt(LocalDateTime.now());
        listing.setLastActive(LocalDateTime.now());
        listing = listingRepository.save(listing);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(4);

        // res1: owner=owner, renter=renter1, PENDING
        reservationRepository.save(buildReservation(listing, owner, renter1, ReservationStatus.PENDING, start, end));
        // res2: owner=owner, renter=renter2, CONFIRMED
        reservationRepository.save(buildReservation(listing, owner, renter2, ReservationStatus.CONFIRMED, start, end));
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        listingRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllByUserEmail_returnsReservationsWhereUserIsOwner() {
        List<Reservation> results = reservationRepository.findAllByUserEmail(owner.getEmail());

        assertEquals(2, results.size());
    }

    @Test
    void findAllByUserEmail_returnsReservationsWhereUserIsRenter() {
        List<Reservation> results = reservationRepository.findAllByUserEmail(renter1.getEmail());

        assertEquals(1, results.size());
        assertEquals(ReservationStatus.PENDING, results.get(0).getStatus());
    }

    @Test
    void findAllByUserEmail_returnsEmpty_whenUserHasNoReservations() {
        User unrelated = userRepository.save(buildUser("nobody@repo.test", "nobody"));

        List<Reservation> results = reservationRepository.findAllByUserEmail(unrelated.getEmail());

        assertTrue(results.isEmpty());
    }

    @Test
    void findAllByUserEmailAndStatus_filtersToMatchingStatus() {
        List<Reservation> results = reservationRepository.findAllByUserEmailAndStatus(
                owner.getEmail(), ReservationStatus.PENDING);

        assertEquals(1, results.size());
        assertEquals(ReservationStatus.PENDING, results.get(0).getStatus());
    }

    @Test
    void findAllByUserEmailAndStatus_returnsEmpty_whenNoMatchingStatus() {
        List<Reservation> results = reservationRepository.findAllByUserEmailAndStatus(
                renter1.getEmail(), ReservationStatus.CONFIRMED);

        assertTrue(results.isEmpty());
    }

    @Test
    void findAllByUserEmailAndStatus_findsRenterByStatus() {
        List<Reservation> results = reservationRepository.findAllByUserEmailAndStatus(
                renter2.getEmail(), ReservationStatus.CONFIRMED);

        assertEquals(1, results.size());
    }

    private User buildUser(String email, String username) {
        User u = new User();
        u.setEmail(email);
        u.setUsername(username);
        u.setPassword("password");
        u.setRole(UserRole.USER);
        return u;
    }

    private Reservation buildReservation(Listing listing, User owner, User renter,
                                         ReservationStatus status, LocalDateTime start, LocalDateTime end) {
        Reservation r = new Reservation();
        r.setListing(listing);
        r.setOwner(owner);
        r.setRenter(renter);
        r.setStatus(status);
        r.setDateStart(start);
        r.setDateEnd(end);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }
}
