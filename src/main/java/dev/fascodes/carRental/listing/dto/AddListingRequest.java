package dev.fascodes.carRental.listing.dto;

public class AddListingRequest {
    private Long carId;
    private Integer price;
    private String title;
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
