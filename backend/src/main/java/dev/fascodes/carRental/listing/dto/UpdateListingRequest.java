package dev.fascodes.carRental.listing.dto;

import dev.fascodes.carRental.listing.model.ListingStatus;
import jakarta.validation.constraints.Pattern;

public class UpdateListingRequest {
    private Long carId;
    private String title;
    private Integer price;
    @Pattern(regexp = "^[\\p{L}-]+$", message = "Localization must contain only letters and hyphens")
    private String localization;
    private String body;
    private ListingStatus status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getLocalization() { return localization; }
    public void setLocalization(String localization) { this.localization = localization; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
}
