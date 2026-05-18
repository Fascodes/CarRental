package dev.fascodes.carRental.reservation.repository;

import dev.fascodes.carRental.reservation.model.Reservation;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByListingIdAndStatusAndDateStartLessThanAndDateEndGreaterThan(
            Long listingId, ReservationStatus status, LocalDateTime dateEnd, LocalDateTime dateStart);

    @Modifying
    @Query("UPDATE Reservation r SET r.status = 'CANCELLED' WHERE r.listing.id = :listingId " +
           "AND r.status = 'PENDING' AND r.dateStart < :dateEnd AND r.dateEnd > :dateStart AND r.id != :excludeId")
    void cancelOverlappingPending(@Param("listingId") Long listingId,
                                  @Param("dateStart") LocalDateTime dateStart,
                                  @Param("dateEnd") LocalDateTime dateEnd,
                                  @Param("excludeId") Long excludeId);
}
