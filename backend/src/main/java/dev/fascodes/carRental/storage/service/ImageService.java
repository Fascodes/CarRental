package dev.fascodes.carRental.storage.service;

import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.common.config.MinIOProperties;
import dev.fascodes.carRental.listing.dto.DeleteImageRequest;
import dev.fascodes.carRental.storage.dto.DownloadImageRequest;
import dev.fascodes.carRental.storage.dto.DownloadImageResponse;
import dev.fascodes.carRental.storage.dto.UploadImageRequest;
import dev.fascodes.carRental.storage.dto.UploadImageResponse;
import dev.fascodes.carRental.storage.mapper.ImageMapper;
import dev.fascodes.carRental.storage.model.Image;
import dev.fascodes.carRental.storage.repository.ImageRepository;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final MinioClient minioClient;
    private final MinIOProperties minIOProperties;
    private final long MAX_SIZE = 5 * 1024 * 1024;
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

        extension = extension.toLowerCase(Locale.ROOT);

        String storageKey = String.format(
                "cars/%d/%s.%s",
                request.getCarId(),
                UUID.randomUUID(),
                extension
        );

        log.info(
                "Uploading image for car {} by user {}",
                request.getCarId(),
                email
        );
        // TODO: When adding the main photo and photo order upload a few different sizes of the main photo (regular, medium, small), regular-inspect photo, medium on listing, small on search
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
            log.info(
                    "Image uploaded successfully. key={}",
                    storageKey
            );
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

    public DownloadImageResponse download(String email, DownloadImageRequest request){
        // TODO: Current implementation attaches images to cars, but carId is supposed to be viewed by owner only - either attach to listingId - need refactor, or add a publicId to images, private - read only by services to identify, public - available to user that views listing/images this way previous API stays the same
        Car car = carRepository.findById(request.getCarId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Car not found")
        );

        Image image = imageRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Image not found"));
        // TODO: Add a cache for photos - simple browser 24h cache(not Redis - it can't cache images), better alternative use CDN
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder().
                    bucket(image.getBucket()).
                    object(image.getStorageKey()).
                    build());
        // TODO: Here possibly add a mapper
            return new DownloadImageResponse(new InputStreamResource(stream),
                    image.getContentType(),
                    image.getOriginalFilename());
        }
        catch(Exception e){
            log.error("Failed to download image {}", image.getId(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not download image");
        }
    }

    @Transactional
    public void delete(String email, DeleteImageRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"));

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Car not found"));

        if (!car.getOwner().equals(user)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Car not found");
        }

        Image image = imageRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Image not found"));

        if (!image.getCar().getId().equals(car.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Image not found");
        }

        /* TODO: Introduce a scheduled reconciliation job that periodically verifies
            consistency between PostgreSQL metadata and MinIO objects.
            The job should remove orphaned objects from MinIO and stale database
            records that reference non-existing objects. */

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(image.getBucket())
                            .object(image.getStorageKey())
                            .build());

            imageRepository.delete(image);

        } catch (Exception e) {
            log.error("Failed to delete image {}", image.getId(), e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to delete image");
        }
    }

}
