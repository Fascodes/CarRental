package dev.fascodes.carRental.storage.model;

import dev.fascodes.carRental.car.model.Car;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
// TODO: in the future add a photo order and primary photo functionality
@Getter
@Setter
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="car_id", nullable = false)
    @Column(name="car_id")
    Car car;

    @Column(name="storage_key")
    String storageKey;

    @Column(name="original_filename")
    String originalFilename;

    @Column(name="content_type")
    String contentType;

    String bucket;

    Long size;
}
