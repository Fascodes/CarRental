package dev.fascodes.carRental.reservation.dto;

import java.time.LocalDateTime;

public class AddReservationRequest {
    private Long listingId;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }

    public LocalDateTime getDateStart() { return dateStart; }
    public void setDateStart(LocalDateTime dateStart) { this.dateStart = dateStart; }

    public LocalDateTime getDateEnd() { return dateEnd; }
    public void setDateEnd(LocalDateTime dateEnd) { this.dateEnd = dateEnd; }
}
