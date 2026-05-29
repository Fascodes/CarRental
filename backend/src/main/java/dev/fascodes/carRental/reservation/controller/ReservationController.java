package dev.fascodes.carRental.reservation.controller;

import dev.fascodes.carRental.common.security.AuthenticatedUser;
import dev.fascodes.carRental.reservation.dto.AddReservationRequest;
import dev.fascodes.carRental.reservation.dto.PatchReservationStatusRequest;
import dev.fascodes.carRental.reservation.dto.ReservationResponse;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import dev.fascodes.carRental.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ReservationResponse> addReservation(@Valid @RequestBody AddReservationRequest request,
                                                              @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(reservationService.addReservation(request, user.email()));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponse> renterConfirmReservation(@PathVariable Long id,
                                                                        @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(reservationService.renterConfirmReservation(id, user.email()));
    }

    @PostMapping("/{id}/owner-confirm")
    public ResponseEntity<ReservationResponse> ownerConfirmReservation(@PathVariable Long id,
                                                                       @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(reservationService.ownerConfirmReservation(id, user.email()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable Long id,
                                                              @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(reservationService.getReservation(id, user.email()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/admin")
    public ResponseEntity<ReservationResponse> patchStatus(@PathVariable Long id,
                                                           @RequestBody PatchReservationStatusRequest request) {
        return ResponseEntity.ok(reservationService.patchStatusAdmin(id, request.getStatus()));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id,
                                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, user.email()));
    }

    @GetMapping("/my/owner")
    public ResponseEntity<List<ReservationResponse>> getReservationsAsOwner(
            @RequestParam(required = false) ReservationStatus status,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(reservationService.getReservationsAsOwner(user.email(), status));
    }

    @GetMapping("/my/renter")
    public ResponseEntity<List<ReservationResponse>> getReservationsAsRenter(
            @RequestParam(required = false) ReservationStatus status,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(reservationService.getReservationsAsRenter(user.email(), status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<ReservationResponse>> getReservationsByUser(
            @RequestParam String email,
            @RequestParam(required = false) ReservationStatus status) {
        return ResponseEntity.ok(reservationService.getReservationsByUserAdmin(email, status));
    }
}
