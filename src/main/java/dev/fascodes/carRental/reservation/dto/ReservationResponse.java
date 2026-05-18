package dev.fascodes.carRental.reservation.dto;

import dev.fascodes.carRental.reservation.model.ReservationStatus;

import java.time.LocalDateTime;

public class ReservationResponse {
    private Long id;
    private ReservationStatus status;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDateTime getDateStart() { return dateStart; }
    public void setDateStart(LocalDateTime dateStart) { this.dateStart = dateStart; }

    public LocalDateTime getDateEnd() { return dateEnd; }
    public void setDateEnd(LocalDateTime dateEnd) { this.dateEnd = dateEnd; }
}
