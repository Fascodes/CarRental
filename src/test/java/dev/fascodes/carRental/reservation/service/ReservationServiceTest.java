package dev.fascodes.carRental.reservation.service;

import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.repository.ListingRepository;
import dev.fascodes.carRental.reservation.dto.AddReservationRequest;
import dev.fascodes.carRental.reservation.mapper.ReservationMapper;
import dev.fascodes.carRental.reservation.model.Reservation;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import dev.fascodes.carRental.reservation.repository.ReservationRepository;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationMapper reservationMapper;
    @Mock private ListingRepository listingRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ReservationService reservationService;

    @Test
    void addReservation_throwsNotFound_whenListingDoesNotExist() {
        User renter = new User();
        renter.setEmail("renter@test.com");

        AddReservationRequest request = new AddReservationRequest();
        request.setListingId(1L);
        request.setDateStart(LocalDateTime.now().plusDays(1));
        request.setDateEnd(LocalDateTime.now().plusDays(3));

        when(userRepository.findByEmail("renter@test.com")).thenReturn(Optional.of(renter));
        when(listingRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.addReservation(request, "renter@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void addReservation_throwsConflict_whenConfirmedOverlapExists() {
        User owner = new User();
        User renter = new User();
        renter.setEmail("renter@test.com");

        Listing listing = new Listing();
        listing.setId(1L);
        listing.setUser(owner);

        AddReservationRequest request = new AddReservationRequest();
        request.setListingId(1L);
        request.setDateStart(LocalDateTime.now().plusDays(1));
        request.setDateEnd(LocalDateTime.now().plusDays(3));

        when(userRepository.findByEmail("renter@test.com")).thenReturn(Optional.of(renter));
        when(listingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(listing));
        when(reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                eq(1L), eq(ReservationStatus.CONFIRMED), any(), any())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.addReservation(request, "renter@test.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // --- confirmReservation ---

    @Test
    void confirmReservation_throwsNotFound_whenReservationDoesNotExist() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.confirmReservation(1L, "owner@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void confirmReservation_throwsConflict_whenStatusIsNotPending() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.confirmReservation(1L, "owner@test.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void confirmReservation_throwsForbidden_whenCallerIsNotOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setOwner(owner);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.confirmReservation(1L, "other@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void confirmReservation_throwsConflict_whenDateRangeAlreadyConfirmed() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Listing listing = new Listing();
        listing.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setOwner(owner);
        reservation.setListing(listing);
        reservation.setDateStart(LocalDateTime.now().plusDays(1));
        reservation.setDateEnd(LocalDateTime.now().plusDays(3));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(listingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(listing));
        when(reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                eq(1L), eq(ReservationStatus.CONFIRMED), any(), any())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.confirmReservation(1L, "owner@test.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void confirmReservation_confirmsAndCancelsOverlapping_whenNoConflict() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Listing listing = new Listing();
        listing.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setOwner(owner);
        reservation.setListing(listing);
        reservation.setDateStart(LocalDateTime.now().plusDays(1));
        reservation.setDateEnd(LocalDateTime.now().plusDays(3));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(listingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(listing));
        when(reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                eq(1L), eq(ReservationStatus.CONFIRMED), any(), any())).thenReturn(false);

        reservationService.confirmReservation(1L, "owner@test.com");

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        verify(reservationRepository).cancelOverlappingPending(eq(1L), any(), any(), eq(1L));
    }
}
