package dev.fascodes.carRental.storage.mapper;

import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.storage.dto.UploadImageResponse;
import dev.fascodes.carRental.storage.model.Image;

public class ImageMapper {
    public UploadImageResponse imageToUploadResponse(Image image){
        UploadImageResponse response = new UploadImageResponse();
        response.setOriginalFilename(image.getOriginalFilename());

        return response;
    }

    public Image toEntity(Car car, String storageKey, String originalFilename, String contentType, String bucket, Long size){
        Image image = new Image();
        image.setCar(car);
        image.setStorageKey(storageKey);
        image.setOriginalFilename(originalFilename);
        image.setContentType(contentType);
        image.setBucket(bucket);
        image.setSize(size);
        return image;
    }

}
