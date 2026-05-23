package dev.fascodes.carRental.car.dto;

import dev.fascodes.carRental.car.model.GearboxType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PatchCarRequest {
    private String brand;
    private String model;

    @Min(1900) @Max(2100)
    private Integer modelYear;

    @Size(min = 17, max = 17)
    private String vin;

    private Integer seatNumber;
    private GearboxType gearboxType;
    private Integer horsePower;

    @DecimalMin("0.0") @DecimalMax("30.0")
    private BigDecimal avgLiters;

    private String info;

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public GearboxType getGearboxType() { return gearboxType; }
    public void setGearboxType(GearboxType gearboxType) { this.gearboxType = gearboxType; }

    public Integer getHorsePower() { return horsePower; }
    public void setHorsePower(Integer horsePower) { this.horsePower = horsePower; }

    public BigDecimal getAvgLiters() { return avgLiters; }
    public void setAvgLiters(BigDecimal avgLiters) { this.avgLiters = avgLiters; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }
}
