package dev.fascodes.carRental.listing.repository;

import dev.fascodes.carRental.listing.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

}
