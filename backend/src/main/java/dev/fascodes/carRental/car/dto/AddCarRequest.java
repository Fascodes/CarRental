package dev.fascodes.carRental.car.dto;

import dev.fascodes.carRental.car.model.GearboxType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class AddCarRequest {
    @NotNull
    private String brand;
    @NotNull
    private String model;

    @Min(1900)
    @Max(2100)
    @NotNull
    private Integer modelYear;

    @Size(min = 17, max = 17)
    private String vin;

    private Integer seatNumber;

    @NotNull
    private String gearboxType;

    private Integer horsePower;

    @DecimalMax("30.0")
    @DecimalMin("0.0")
    private BigDecimal avgLiters;

    private String info;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getGearboxType() {
        return gearboxType;
    }

    public void setGearboxType(String gearboxType) {
        this.gearboxType = gearboxType;
    }

    public Integer getHorsePower() {
        return horsePower;
    }

    public void setHorsePower(Integer horsePower) {
        this.horsePower = horsePower;
    }

    public BigDecimal getAvgLiters() {
        return avgLiters;
    }

    public void setAvgLiters(BigDecimal avgLiters) {
        this.avgLiters = avgLiters;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
