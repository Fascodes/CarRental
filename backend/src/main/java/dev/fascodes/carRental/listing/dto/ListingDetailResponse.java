package dev.fascodes.carRental.listing.dto;

import dev.fascodes.carRental.car.model.GearboxType;
import dev.fascodes.carRental.listing.model.ListingStatus;

import java.io.Serializable;
import java.math.BigDecimal;

public class ListingDetailResponse implements Serializable {
    private Long id;
    private String title;
    private String localization;
    private String body;
    private Integer price;
    private ListingStatus status;
    private String ownerUsername;
    private String brand;
    private String model;
    private Integer modelYear;
    private GearboxType gearboxType;
    private String vin;
    private Integer seatNumber;
    private Integer horsePower;
    private BigDecimal avgLiters;
    private String info;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocalization() { return localization; }
    public void setLocalization(String localization) { this.localization = localization; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }

    public GearboxType getGearboxType() { return gearboxType; }
    public void setGearboxType(GearboxType gearboxType) { this.gearboxType = gearboxType; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public Integer getHorsePower() { return horsePower; }
    public void setHorsePower(Integer horsePower) { this.horsePower = horsePower; }

    public BigDecimal getAvgLiters() { return avgLiters; }
    public void setAvgLiters(BigDecimal avgLiters) { this.avgLiters = avgLiters; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }
}
