package dev.fascodes.carRental.reservation.repository;

import dev.fascodes.carRental.reservation.model.Reservation;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    boolean existsByListingIdAndStatusIn(Long listingId, List<ReservationStatus> statuses);

    List<Reservation> findByOwner_EmailOrderByDateStartAsc(String email);
    List<Reservation> findByOwner_EmailAndStatusOrderByDateStartAsc(String email, ReservationStatus status);

    List<Reservation> findByRenter_EmailOrderByDateStartAsc(String email);
    List<Reservation> findByRenter_EmailAndStatusOrderByDateStartAsc(String email, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.owner.email = :email OR r.renter.email = :email ORDER BY r.dateStart ASC")
    List<Reservation> findAllByUserEmail(@Param("email") String email);

    @Query("SELECT r FROM Reservation r WHERE (r.owner.email = :email OR r.renter.email = :email) AND r.status = :status ORDER BY r.dateStart ASC")
    List<Reservation> findAllByUserEmailAndStatus(@Param("email") String email, @Param("status") ReservationStatus status);
}
