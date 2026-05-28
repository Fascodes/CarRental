package dev.fascodes.carRental.listing.dto;

import jakarta.validation.constraints.NotNull;

public class AddListingRequest {
    @NotNull
    private Long carId;
    @NotNull
    private Integer price;
    @NotNull
    private String title;
    @NotNull
    private String body;

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
