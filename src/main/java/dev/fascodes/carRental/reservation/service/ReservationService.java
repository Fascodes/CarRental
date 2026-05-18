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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper,
                              ListingRepository listingRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse addReservation(AddReservationRequest request, String renterEmail) {
        User renter = userRepository.findByEmail(renterEmail).orElseThrow();
        Listing listing = listingRepository.findByIdWithLock(request.getListingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        boolean hasConflict = reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                listing.getId(), ReservationStatus.CONFIRMED,
                request.getDateEnd(), request.getDateStart());

        if (hasConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Date range already confirmed");
        }

        User owner = listing.getUser();
        Reservation reservation = reservationMapper.toEntity(request, listing, owner, renter);
        reservationRepository.save(reservation);
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse confirmReservation(Long reservationId, String ownerEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation is not PENDING");
        }

        if (!reservation.getOwner().getEmail().equals(ownerEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        listingRepository.findByIdWithLock(reservation.getListing().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean hasConflict = reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                reservation.getListing().getId(), ReservationStatus.CONFIRMED,
                reservation.getDateEnd(), reservation.getDateStart());

        if (hasConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Date range already confirmed for another reservation");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);

        reservationRepository.cancelOverlappingPending(
                reservation.getListing().getId(),
                reservation.getDateStart(),
                reservation.getDateEnd(),
                reservationId);

        return reservationMapper.toResponse(reservation);
    }
}
