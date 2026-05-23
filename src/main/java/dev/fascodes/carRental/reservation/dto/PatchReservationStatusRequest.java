package dev.fascodes.carRental.reservation.dto;

import dev.fascodes.carRental.reservation.model.ReservationStatus;

public class PatchReservationStatusRequest {
    private ReservationStatus status;

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
}
