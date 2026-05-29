package dev.fascodes.carRental.reservation.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AddReservationRequest {
    @NotNull
    private Long listingId;
    @NotNull
    private LocalDateTime dateStart;
    @NotNull
    private LocalDateTime dateEnd;

    @AssertTrue(message = "dateEnd must be after dateStart")
    public boolean isDateRangeValid() {
        return dateStart != null && dateEnd != null && dateEnd.isAfter(dateStart);
    }

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }

    public LocalDateTime getDateStart() { return dateStart; }
    public void setDateStart(LocalDateTime dateStart) { this.dateStart = dateStart; }

    public LocalDateTime getDateEnd() { return dateEnd; }
    public void setDateEnd(LocalDateTime dateEnd) { this.dateEnd = dateEnd; }
}
