package dev.fascodes.carRental.reservation.controller;

import dev.fascodes.carRental.common.security.WithMockAuthenticatedUser;
import dev.fascodes.carRental.reservation.dto.ReservationResponse;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import dev.fascodes.carRental.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Requires test DB (Docker: db-test on port 5433)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ReservationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ReservationService reservationService;

    // --- PATCH /cancel/{id} ---

    @Test
    @WithMockAuthenticatedUser
    void cancelReservation_returns200_whenAuthenticated() throws Exception {
        when(reservationService.cancelReservation(anyLong(), any())).thenReturn(new ReservationResponse());

        mockMvc.perform(patch("/api/reservation/cancel/1"))
                .andExpect(status().isOk());
    }

    // --- PATCH /{id}/admin ---

    @Test
    @WithMockAuthenticatedUser(role = "USER")
    void patchStatus_returns403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/reservation/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockAuthenticatedUser(role = "ADMIN")
    void patchStatus_returns200_whenUserIsAdmin() throws Exception {
        when(reservationService.patchStatusAdmin(anyLong(), any())).thenReturn(new ReservationResponse());

        mockMvc.perform(patch("/api/reservation/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk());
    }

    // --- GET /my/owner ---

    @Test
    @WithMockAuthenticatedUser
    void getReservationsAsOwner_returns200_whenAuthenticated() throws Exception {
        when(reservationService.getReservationsAsOwner(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/reservation/my/owner"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthenticatedUser
    void getReservationsAsOwner_returns200_withStatusFilter() throws Exception {
        when(reservationService.getReservationsAsOwner(any(), eq(ReservationStatus.PENDING)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reservation/my/owner?status=PENDING"))
                .andExpect(status().isOk());
    }

    // --- GET /my/renter ---

    @Test
    @WithMockAuthenticatedUser
    void getReservationsAsRenter_returns200_whenAuthenticated() throws Exception {
        when(reservationService.getReservationsAsRenter(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/reservation/my/renter"))
                .andExpect(status().isOk());
    }

    // --- GET /admin ---

    @Test
    @WithMockAuthenticatedUser(role = "USER")
    void getReservationsByUser_returns403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/reservation/admin?email=user@test.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockAuthenticatedUser(role = "ADMIN")
    void getReservationsByUser_returns200_whenUserIsAdmin() throws Exception {
        when(reservationService.getReservationsByUserAdmin(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/reservation/admin?email=user@test.com"))
                .andExpect(status().isOk());
    }
}
