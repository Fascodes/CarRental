package dev.fascodes.carRental.listing.dto;

import dev.fascodes.carRental.listing.model.ListingStatus;

public class AddListingResponse {
    private String title;
    private Integer price;
    private ListingStatus status;


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }
}
