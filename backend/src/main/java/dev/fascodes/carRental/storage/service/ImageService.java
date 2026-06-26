package dev.fascodes.carRental.storage.service;

import dev.fascodes.carRental.storage.repository.ImageRepository;

public class ImageService {
    private ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    
}
