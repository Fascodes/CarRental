package dev.fascodes.carRental.listing.dto;

import dev.fascodes.carRental.car.model.GearboxType;

public class ListingResponse {
    private Long id;
    private String title;
    private Integer price;
    private String brand;
    private String model;
    private Integer modelYear;
    private GearboxType gearboxType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }

    public GearboxType getGearboxType() { return gearboxType; }
    public void setGearboxType(GearboxType gearboxType) { this.gearboxType = gearboxType; }
}
