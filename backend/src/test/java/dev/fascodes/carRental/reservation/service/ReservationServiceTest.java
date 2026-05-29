package dev.fascodes.carRental.reservation.service;

import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.repository.ListingRepository;
import dev.fascodes.carRental.reservation.dto.AddReservationRequest;
import dev.fascodes.carRental.reservation.dto.ReservationResponse;
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
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
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
    @Mock private AmqpTemplate amqpTemplate;
    @InjectMocks private ReservationService reservationService;

    // --- addReservation ---

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
    void addReservation_throwsForbidden_whenSelfReservation() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Listing listing = new Listing();
        listing.setId(1L);
        listing.setUser(owner);

        AddReservationRequest request = new AddReservationRequest();
        request.setListingId(1L);
        request.setDateStart(LocalDateTime.now().plusDays(1));
        request.setDateEnd(LocalDateTime.now().plusDays(3));

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(listingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(listing));
        when(reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                anyLong(), any(), any(), any())).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.addReservation(request, "owner@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void addReservation_throwsConflict_whenRenterConfirmedOverlapExists() {
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
                eq(1L), eq(ReservationStatus.RENTER_CONFIRMED), any(), any())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.addReservation(request, "renter@test.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // --- renterConfirmReservation ---

    @Test
    void renterConfirmReservation_throwsNotFound_whenReservationDoesNotExist() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.renterConfirmReservation(1L, "renter@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void renterConfirmReservation_throwsConflict_whenStatusIsNotPending() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.RENTER_CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.renterConfirmReservation(1L, "renter@test.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void renterConfirmReservation_throwsForbidden_whenCallerIsNotRenter() {
        User renter = new User();
        renter.setEmail("renter@test.com");

        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setRenter(renter);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.renterConfirmReservation(1L, "other@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void renterConfirmReservation_throwsConflict_whenConfirmedOverlapExists() {
        User renter = new User();
        renter.setEmail("renter@test.com");

        Listing listing = new Listing();
        listing.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setRenter(renter);
        reservation.setListing(listing);
        reservation.setDateStart(LocalDateTime.now().plusDays(1));
        reservation.setDateEnd(LocalDateTime.now().plusDays(3));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(listingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(listing));
        when(reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                eq(1L), eq(ReservationStatus.CONFIRMED), any(), any())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.renterConfirmReservation(1L, "renter@test.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void renterConfirmReservation_setsRenterConfirmedAndCancelsOverlapping_whenNoConflict() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setUsername("owner");

        User renter = new User();
        renter.setEmail("renter@test.com");
        renter.setUsername("renter");

        Listing listing = new Listing();
        listing.setId(1L);
        listing.setTitle("Test listing");

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setOwner(owner);
        reservation.setRenter(renter);
        reservation.setListing(listing);
        reservation.setDateStart(LocalDateTime.now().plusDays(1));
        reservation.setDateEnd(LocalDateTime.now().plusDays(3));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(listingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(listing));
        when(reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                eq(1L), eq(ReservationStatus.CONFIRMED), any(), any())).thenReturn(false);
        when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

        reservationService.renterConfirmReservation(1L, "renter@test.com");

        assertEquals(ReservationStatus.RENTER_CONFIRMED, reservation.getStatus());
        verify(reservationRepository).cancelOverlappingPending(eq(1L), any(), any(), eq(1L));
        verify(amqpTemplate).convertAndSend(any(), any(), any(Object.class));
    }

    // --- ownerConfirmReservation ---

    @Test
    void ownerConfirmReservation_throwsNotFound_whenReservationDoesNotExist() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.ownerConfirmReservation(1L, "owner@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void ownerConfirmReservation_throwsConflict_whenStatusIsNotRenterConfirmed() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.ownerConfirmReservation(1L, "owner@test.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void ownerConfirmReservation_throwsForbidden_whenCallerIsNotOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.RENTER_CONFIRMED);
        reservation.setOwner(owner);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.ownerConfirmReservation(1L, "other@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void ownerConfirmReservation_setsConfirmed_whenValid() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setUsername("owner");

        User renter = new User();
        renter.setEmail("renter@test.com");
        renter.setUsername("renter");

        Listing listing = new Listing();
        listing.setId(1L);
        listing.setTitle("Test listing");

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.RENTER_CONFIRMED);
        reservation.setOwner(owner);
        reservation.setRenter(renter);
        reservation.setListing(listing);
        reservation.setDateStart(LocalDateTime.now().plusDays(1));
        reservation.setDateEnd(LocalDateTime.now().plusDays(3));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

        reservationService.ownerConfirmReservation(1L, "owner@test.com");

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        verify(amqpTemplate).convertAndSend(any(), any(), any(Object.class));
    }

    // --- patchStatusAdmin ---

    @Test
    void patchStatusAdmin_throwsNotFound_whenReservationDoesNotExist() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.patchStatusAdmin(1L, ReservationStatus.CONFIRMED));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void patchStatusAdmin_setsStatus_whenReservationExists() {
        Listing listing = new Listing();
        listing.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setListing(listing);
        reservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

        reservationService.patchStatusAdmin(1L, ReservationStatus.ACTIVE);

        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
    }

    // --- cancelReservation ---

    @Test
    void cancelReservation_throwsNotFound_whenReservationDoesNotExist() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.cancelReservation(1L, "user@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void cancelReservation_throwsForbidden_whenCallerIsNotRenter() {
        User renter = new User();
        renter.setEmail("renter@test.com");

        Reservation reservation = new Reservation();
        reservation.setRenter(renter);
        reservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.cancelReservation(1L, "other@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void cancelReservation_throwsConflict_whenStatusIsNotCancellable() {
        User renter = new User();
        renter.setEmail("renter@test.com");

        Reservation reservation = new Reservation();
        reservation.setRenter(renter);
        reservation.setStatus(ReservationStatus.COMPLETED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservationService.cancelReservation(1L, "renter@test.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void cancelReservation_setsCancelled_whenPending() {
        User renter = new User();
        renter.setEmail("renter@test.com");

        Listing listing = new Listing();
        listing.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setRenter(renter);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setListing(listing);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

        reservationService.cancelReservation(1L, "renter@test.com");

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void cancelReservation_setsCancelled_whenRenterConfirmed() {
        User renter = new User();
        renter.setEmail("renter@test.com");

        Listing listing = new Listing();
        listing.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setRenter(renter);
        reservation.setStatus(ReservationStatus.RENTER_CONFIRMED);
        reservation.setListing(listing);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

        reservationService.cancelReservation(1L, "renter@test.com");

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void cancelReservation_setsCancelled_whenConfirmed() {
        User renter = new User();
        renter.setEmail("renter@test.com");

        Listing listing = new Listing();
        listing.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setRenter(renter);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setListing(listing);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

        reservationService.cancelReservation(1L, "renter@test.com");

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    // --- getReservationsAsOwner ---

    @Test
    void getReservationsAsOwner_callsFilteredRepo_whenStatusProvided() {
        when(reservationRepository.findByOwner_EmailAndStatusOrderByDateStartAsc("owner@test.com", ReservationStatus.PENDING))
                .thenReturn(List.of());

        reservationService.getReservationsAsOwner("owner@test.com", ReservationStatus.PENDING);

        verify(reservationRepository).findByOwner_EmailAndStatusOrderByDateStartAsc("owner@test.com", ReservationStatus.PENDING);
        verify(reservationRepository, never()).findByOwner_EmailOrderByDateStartAsc(any());
    }

    @Test
    void getReservationsAsOwner_callsAllRepo_whenStatusIsNull() {
        when(reservationRepository.findByOwner_EmailOrderByDateStartAsc("owner@test.com"))
                .thenReturn(List.of());

        reservationService.getReservationsAsOwner("owner@test.com", null);

        verify(reservationRepository).findByOwner_EmailOrderByDateStartAsc("owner@test.com");
        verify(reservationRepository, never()).findByOwner_EmailAndStatusOrderByDateStartAsc(any(), any());
    }

    // --- getReservationsAsRenter ---

    @Test
    void getReservationsAsRenter_callsFilteredRepo_whenStatusProvided() {
        when(reservationRepository.findByRenter_EmailAndStatusOrderByDateStartAsc("renter@test.com", ReservationStatus.CONFIRMED))
                .thenReturn(List.of());

        reservationService.getReservationsAsRenter("renter@test.com", ReservationStatus.CONFIRMED);

        verify(reservationRepository).findByRenter_EmailAndStatusOrderByDateStartAsc("renter@test.com", ReservationStatus.CONFIRMED);
        verify(reservationRepository, never()).findByRenter_EmailOrderByDateStartAsc(any());
    }

    @Test
    void getReservationsAsRenter_callsAllRepo_whenStatusIsNull() {
        when(reservationRepository.findByRenter_EmailOrderByDateStartAsc("renter@test.com"))
                .thenReturn(List.of());

        reservationService.getReservationsAsRenter("renter@test.com", null);

        verify(reservationRepository).findByRenter_EmailOrderByDateStartAsc("renter@test.com");
        verify(reservationRepository, never()).findByRenter_EmailAndStatusOrderByDateStartAsc(any(), any());
    }

    // --- getReservationsByUserAdmin ---

    @Test
    void getReservationsByUserAdmin_callsFilteredRepo_whenStatusProvided() {
        when(reservationRepository.findAllByUserEmailAndStatus("user@test.com", ReservationStatus.PENDING))
                .thenReturn(List.of());

        reservationService.getReservationsByUserAdmin("user@test.com", ReservationStatus.PENDING);

        verify(reservationRepository).findAllByUserEmailAndStatus("user@test.com", ReservationStatus.PENDING);
        verify(reservationRepository, never()).findAllByUserEmail(any());
    }

    @Test
    void getReservationsByUserAdmin_callsAllRepo_whenStatusIsNull() {
        when(reservationRepository.findAllByUserEmail("user@test.com"))
                .thenReturn(List.of());

        reservationService.getReservationsByUserAdmin("user@test.com", null);

        verify(reservationRepository).findAllByUserEmail("user@test.com");
        verify(reservationRepository, never()).findAllByUserEmailAndStatus(any(), any());
    }
}
