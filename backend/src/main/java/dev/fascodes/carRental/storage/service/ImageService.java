package dev.fascodes.carRental.storage.service;

import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.common.config.MinIOProperties;
import dev.fascodes.carRental.storage.dto.UploadImageRequest;
import dev.fascodes.carRental.storage.dto.UploadImageResponse;
import dev.fascodes.carRental.storage.mapper.ImageMapper;
import dev.fascodes.carRental.storage.model.Image;
import dev.fascodes.carRental.storage.repository.ImageRepository;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final MinioClient minioClient;
    private final MinIOProperties minIOProperties;
    private final int MAX_SIZE = 5000000;
    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public ImageService(ImageRepository imageRepository, ImageMapper imageMapper, UserRepository userRepository, CarRepository carRepository, MinioClient minioClient, MinIOProperties minIOProperties) {
        this.imageRepository = imageRepository;
        this.imageMapper = imageMapper;
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.minioClient = minioClient;
        this.minIOProperties = minIOProperties;
    }

    @Transactional
    public UploadImageResponse upload(String email, UploadImageRequest request, MultipartFile file){
        User user = userRepository.findByEmail(email).
                orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"));

        Car car = carRepository.findById(request.getCarId()).
                orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Car not found"
                ));

        if(!car.getOwner().equals(user)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found");
        }

        if(file.isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file available");
            }

        if(file.getSize() > MAX_SIZE){
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "File exceeding max size(5MB)");
        }

        if (!SUPPORTED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported image type."
            );
        }

        String extension = FilenameUtils.getExtension(
                file.getOriginalFilename()
        );

        String storageKey = String.format(
                "cars/%d/%s.%s",
                request.getCarId(),
                UUID.randomUUID(),
                extension
        );


        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(minIOProperties.getBucket())
                    .object(storageKey)
                    .stream(
                            file.getInputStream(),
                            file.getSize(),
                            -1
                    )
                    .contentType(file.getContentType())
                    .build()
            );
        }
        catch (Exception e){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to upload image.",
                    e
            );
        }

        try {
            Image image = imageMapper.toEntity(car,
                    storageKey,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    minIOProperties.getBucket(),
                    file.getSize());

            imageRepository.save(image);

            return imageMapper.imageToUploadResponse(image);
        } catch (Exception e) {
            deleteFromStorage(storageKey);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to upload image.",
                    e);
        }
    }

    private void deleteFromStorage(String storageKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minIOProperties.getBucket())
                            .object(storageKey)
                            .build());
        } catch (Exception e) {
            log.warn("Failed to rollback uploaded object in MinIO. key={}", storageKey, e);
        }
    }

    }
