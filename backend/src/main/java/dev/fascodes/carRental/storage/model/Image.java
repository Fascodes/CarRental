package dev.fascodes.carRental.storage.model;

import jakarta.persistence.*;

import java.math.BigInteger;

public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="car_id", nullable = false)
    @Column(name="car_id")
    Long carId;

    @Column(name="storage_key")
    String storageKey;

    @Column(name="original_filename")
    String originalFilename;

    @Column(name="content_type")
    String contentType;

    String bucket;

    Long size;
}
