package dev.fascodes.carRental.listing.repository;

import dev.fascodes.carRental.listing.model.Listing;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Listing l WHERE l.id = :id")
    Optional<Listing> findByIdWithLock(@Param("id") Long id);

    @EntityGraph(attributePaths = {"car", "user"})
    Page<Listing> findAll(Specification<Listing> spec, Pageable pageable);

    boolean existsByCarId(Long carId);

}
