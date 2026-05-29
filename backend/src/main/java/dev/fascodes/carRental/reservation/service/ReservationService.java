package dev.fascodes.carRental.reservation.service;

import dev.fascodes.carRental.common.config.RabbitConfig;
import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.repository.ListingRepository;
import dev.fascodes.carRental.notification.ReservationNotificationEvent;
import dev.fascodes.carRental.reservation.dto.AddReservationRequest;
import dev.fascodes.carRental.reservation.dto.ReservationResponse;
import dev.fascodes.carRental.reservation.mapper.ReservationMapper;
import dev.fascodes.carRental.reservation.model.Reservation;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import dev.fascodes.carRental.reservation.repository.ReservationRepository;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final AmqpTemplate amqpTemplate;

    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper,
                              ListingRepository listingRepository, UserRepository userRepository, AmqpTemplate amqpTemplate) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.amqpTemplate = amqpTemplate;
    }

    @Transactional
    public ReservationResponse addReservation(AddReservationRequest request, String renterEmail) {
        User renter = userRepository.findByEmail(renterEmail).orElseThrow();
        Listing listing = listingRepository.findByIdWithLock(request.getListingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        boolean hasConflict = reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                listing.getId(), ReservationStatus.RENTER_CONFIRMED,
                request.getDateEnd(), request.getDateStart()) || reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                listing.getId(), ReservationStatus.CONFIRMED,
                request.getDateEnd(), request.getDateStart());

        if (hasConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Date range already confirmed");
        }

        User owner = listing.getUser();
        if (owner.getEmail().equals(renterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot reserve your own listing");
        }
        Reservation reservation = reservationMapper.toEntity(request, listing, owner, renter);
        reservationRepository.save(reservation);
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse renterConfirmReservation(Long reservationId, String renterEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation is not PENDING");
        }

        if (!reservation.getRenter().getEmail().equals(renterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        listingRepository.findByIdWithLock(reservation.getListing().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean hasConflict = reservationRepository.existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
                reservation.getListing().getId(), ReservationStatus.CONFIRMED,
                reservation.getDateEnd(), reservation.getDateStart());

        if (hasConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Date range already confirmed");
        }

        reservation.setStatus(ReservationStatus.RENTER_CONFIRMED);

        reservationRepository.cancelOverlappingPending(
                reservation.getListing().getId(),
                reservation.getDateStart(),
                reservation.getDateEnd(),
                reservationId);

        amqpTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY,
                new ReservationNotificationEvent(
                        reservation.getId(),
                        reservation.getOwner().getEmail(),
                        reservation.getOwner().getUsername(),
                        reservation.getListing().getTitle(),
                        reservation.getDateStart().toString(),
                        reservation.getDateEnd().toString(),
                        "RENTER_CONFIRMED"
                ));

        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse ownerConfirmReservation(Long reservationId, String ownerEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.RENTER_CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation is not RENTER_CONFIRMED");
        }

        if (!reservation.getOwner().getEmail().equals(ownerEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);

        amqpTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY,
                new ReservationNotificationEvent(
                        reservation.getId(),
                        reservation.getRenter().getEmail(),
                        reservation.getRenter().getUsername(),
                        reservation.getListing().getTitle(),
                        reservation.getDateStart().toString(),
                        reservation.getDateEnd().toString(),
                        "CONFIRMED"
                ));

        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long reservationId, String email) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean isParticipant = reservation.getOwner().getEmail().equals(email)
                || reservation.getRenter().getEmail().equals(email);
        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse patchStatusAdmin(Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        reservation.setStatus(status);
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, String email) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean isRenter = reservation.getRenter().getEmail().equals(email);
        if (!isRenter) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.RENTER_CONFIRMED
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING, RENTER_CONFIRMED or CONFIRMED reservations can be cancelled");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsAsOwner(String email, ReservationStatus status) {
        List<Reservation> reservations = status != null
                ? reservationRepository.findByOwner_EmailAndStatusOrderByDateStartAsc(email, status)
                : reservationRepository.findByOwner_EmailOrderByDateStartAsc(email);
        return reservations.stream().map(reservationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsAsRenter(String email, ReservationStatus status) {
        List<Reservation> reservations = status != null
                ? reservationRepository.findByRenter_EmailAndStatusOrderByDateStartAsc(email, status)
                : reservationRepository.findByRenter_EmailOrderByDateStartAsc(email);
        return reservations.stream().map(reservationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByUserAdmin(String userEmail, ReservationStatus status) {
        List<Reservation> reservations = status != null
                ? reservationRepository.findAllByUserEmailAndStatus(userEmail, status)
                : reservationRepository.findAllByUserEmail(userEmail);
        return reservations.stream().map(reservationMapper::toResponse).toList();
    }
}
