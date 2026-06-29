package dev.fascodes.carRental.storage.dto;

import jakarta.validation.constraints.NotNull;

public class UploadImageRequest {
    @NotNull
    private Long carId;

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
}
