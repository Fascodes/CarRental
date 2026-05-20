package dev.fascodes.carRental.ReservationTests;

import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.model.GearboxType;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.model.ListingStatus;
import dev.fascodes.carRental.listing.repository.ListingRepository;
import dev.fascodes.carRental.reservation.model.Reservation;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import dev.fascodes.carRental.reservation.repository.ReservationRepository;
import dev.fascodes.carRental.reservation.service.ReservationService;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.model.UserRole;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ReservationServiceTests {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private Long reservation1Id;
    private Long reservation2Id;
    private String ownerEmail;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setUsername("owner");
        owner.setPassword("password");
        owner.setRole(UserRole.USER);
        owner = userRepository.save(owner);
        ownerEmail = owner.getEmail();

        User renter1 = new User();
        renter1.setEmail("renter1@test.com");
        renter1.setUsername("renter1");
        renter1.setPassword("password");
        renter1.setRole(UserRole.USER);
        renter1 = userRepository.save(renter1);

        User renter2 = new User();
        renter2.setEmail("renter2@test.com");
        renter2.setUsername("renter2");
        renter2.setPassword("password");
        renter2.setRole(UserRole.USER);
        renter2 = userRepository.save(renter2);

        Car car = new Car();
        car.setOwner(owner);
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setModelYear(2020);
        car.setVin("12345678901234567");
        car.setSeatNumber(5);
        car.setGearboxType(GearboxType.MANUAL);
        car.setHorsePower(120);
        car = carRepository.save(car);

        Listing listing = new Listing();
        listing.setCar(car);
        listing.setUser(owner);
        listing.setTitle("Test listing");
        listing.setPrice(100);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setCreatedAt(LocalDateTime.now());
        listing.setLastActive(LocalDateTime.now());
        listing = listingRepository.save(listing);

        LocalDateTime dateStart = LocalDateTime.now().plusDays(1);
        LocalDateTime dateEnd = LocalDateTime.now().plusDays(5);

        Reservation res1 = new Reservation();
        res1.setListing(listing);
        res1.setOwner(owner);
        res1.setRenter(renter1);
        res1.setStatus(ReservationStatus.PENDING);
        res1.setDateStart(dateStart);
        res1.setDateEnd(dateEnd);
        res1.setCreatedAt(LocalDateTime.now());
        reservation1Id = reservationRepository.save(res1).getId();

        Reservation res2 = new Reservation();
        res2.setListing(listing);
        res2.setOwner(owner);
        res2.setRenter(renter2);
        res2.setStatus(ReservationStatus.PENDING);
        res2.setDateStart(dateStart);
        res2.setDateEnd(dateEnd);
        res2.setCreatedAt(LocalDateTime.now());
        reservation2Id = reservationRepository.save(res2).getId();
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        listingRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void raceConditionTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Future<Object> request1 = executor.submit(() -> {
            latch.await();
            return reservationService.confirmReservation(reservation1Id, ownerEmail);
        });

        Future<Object> request2 = executor.submit(() -> {
            latch.await();
            return reservationService.confirmReservation(reservation2Id, ownerEmail);
        });

        latch.countDown();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        int successCount = 0;
        int failCount = 0;

        try { request1.get(); successCount++; }
        catch (ExecutionException e) { failCount++; }

        try { request2.get(); successCount++; }
        catch (ExecutionException e) { failCount++; }

        assertEquals(1, successCount);
        assertEquals(1, failCount);
    }
}
