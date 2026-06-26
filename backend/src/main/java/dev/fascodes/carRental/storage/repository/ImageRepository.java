package dev.fascodes.carRental.storage.repository;

import dev.fascodes.carRental.storage.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
