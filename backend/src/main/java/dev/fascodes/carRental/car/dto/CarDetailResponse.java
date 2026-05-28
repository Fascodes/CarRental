package dev.fascodes.carRental.car.dto;

import dev.fascodes.carRental.car.model.GearboxType;

import java.math.BigDecimal;

public class CarDetailResponse {
    private Long id;
    private String brand;
    private String model;
    private Integer modelYear;
    private GearboxType gearboxType;
    private Integer seatNumber;
    private Integer horsePower;
    private BigDecimal avgLiters;
    private String vin;
    private String info;

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }

    public GearboxType getGearboxType() { return gearboxType; }
    public void setGearboxType(GearboxType gearboxType) { this.gearboxType = gearboxType; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public Integer getHorsePower() { return horsePower; }
    public void setHorsePower(Integer horsePower) { this.horsePower = horsePower; }

    public BigDecimal getAvgLiters() { return avgLiters; }
    public void setAvgLiters(BigDecimal avgLiters) { this.avgLiters = avgLiters; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
