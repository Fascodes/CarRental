package dev.fascodes.carRental.storage.dto;

import jakarta.validation.constraints.NotNull;

public class DownloadImageRequest {
    @NotNull
    private Long id;

    @NotNull
    private Long carId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
}
