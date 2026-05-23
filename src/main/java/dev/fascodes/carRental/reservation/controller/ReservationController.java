package dev.fascodes.carRental.reservation.controller;

import dev.fascodes.carRental.reservation.dto.AddReservationRequest;
import dev.fascodes.carRental.reservation.dto.PatchReservationStatusRequest;
import dev.fascodes.carRental.reservation.dto.ReservationResponse;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import dev.fascodes.carRental.reservation.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody AddReservationRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(reservationService.addReservation(request, email));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponse> confirmReservation(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(reservationService.confirmReservation(id, email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/admin")
    public ResponseEntity<ReservationResponse> patchStatus(@PathVariable Long id,
                                                            @RequestBody PatchReservationStatusRequest request) {
        return ResponseEntity.ok(reservationService.patchStatusAdmin(id, request.getStatus()));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(reservationService.cancelReservation(id, email));
    }

    @GetMapping("/my/owner")
    public ResponseEntity<List<ReservationResponse>> getReservationsAsOwner(
            @RequestParam(required = false) ReservationStatus status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(reservationService.getReservationsAsOwner(email, status));
    }

    @GetMapping("/my/renter")
    public ResponseEntity<List<ReservationResponse>> getReservationsAsRenter(
            @RequestParam(required = false) ReservationStatus status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(reservationService.getReservationsAsRenter(email, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<ReservationResponse>> getReservationsByUser(
            @RequestParam String email,
            @RequestParam(required = false) ReservationStatus status) {
        return ResponseEntity.ok(reservationService.getReservationsByUserAdmin(email, status));
    }
}
