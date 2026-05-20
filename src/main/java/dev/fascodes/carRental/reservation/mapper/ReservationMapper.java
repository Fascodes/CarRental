package dev.fascodes.carRental.reservation.mapper;

import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.reservation.dto.AddReservationRequest;
import dev.fascodes.carRental.reservation.dto.ReservationResponse;
import dev.fascodes.carRental.reservation.model.Reservation;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import dev.fascodes.carRental.user.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservationMapper {

    public Reservation toEntity(AddReservationRequest request, Listing listing, User owner, User renter) {
        Reservation reservation = new Reservation();
        reservation.setListing(listing);
        reservation.setOwner(owner);
        reservation.setRenter(renter);
        reservation.setDateStart(request.getDateStart());
        reservation.setDateEnd(request.getDateEnd());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        return reservation;
    }

    public ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setStatus(reservation.getStatus());
        response.setDateStart(reservation.getDateStart());
        response.setDateEnd(reservation.getDateEnd());
        return response;
    }
}
