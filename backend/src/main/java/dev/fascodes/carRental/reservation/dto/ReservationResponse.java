package dev.fascodes.carRental.reservation.dto;

import dev.fascodes.carRental.reservation.model.ReservationStatus;

import java.time.LocalDateTime;

public class ReservationResponse {
    private Long id;
    private Long listingId;
    private String listingTitle;
    private String listingLocalization;
    private ReservationStatus status;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private String ownerUsername;
    private String renterUsername;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }

    public String getListingTitle() { return listingTitle; }
    public void setListingTitle(String listingTitle) { this.listingTitle = listingTitle; }

    public String getListingLocalization() { return listingLocalization; }
    public void setListingLocalization(String listingLocalization) { this.listingLocalization = listingLocalization; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDateTime getDateStart() { return dateStart; }
    public void setDateStart(LocalDateTime dateStart) { this.dateStart = dateStart; }

    public LocalDateTime getDateEnd() { return dateEnd; }
    public void setDateEnd(LocalDateTime dateEnd) { this.dateEnd = dateEnd; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public String getRenterUsername() { return renterUsername; }
    public void setRenterUsername(String renterUsername) { this.renterUsername = renterUsername; }
}
