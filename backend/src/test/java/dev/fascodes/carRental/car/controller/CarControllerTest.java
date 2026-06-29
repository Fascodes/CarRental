package dev.fascodes.carRental.car.controller;

import dev.fascodes.carRental.car.dto.CarDetailResponse;
import dev.fascodes.carRental.car.dto.CarResponse;
import dev.fascodes.carRental.car.service.CarService;
import dev.fascodes.carRental.common.config.SecurityConfig;
import dev.fascodes.carRental.common.security.JwtAuthenticationFilter;
import dev.fascodes.carRental.common.security.WithMockAuthenticatedUser;
import dev.fascodes.carRental.common.utility.JwtUtil;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Web-layer slice: only the controller + security chain are loaded. The service is mocked,
// so no database, RabbitMQ or other infrastructure is required.
@WebMvcTest(CarController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class CarControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CarService carService;

    // Collaborators pulled in by SecurityConfig; mocked because no real DB/JWT is wired in the slice.
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean UserDetailsService userDetailsService;

    // --- GET /my ---

    @Test
    void getMyCars_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/car/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockAuthenticatedUser
    void getMyCars_returns200_whenAuthenticated() throws Exception {
        when(carService.getMyCars(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/car/my"))
                .andExpect(status().isOk());
    }

    // --- GET /{id} ---

    @Test
    void getCar_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/car/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockAuthenticatedUser
    void getCar_returns200_whenAuthenticated() throws Exception {
        when(carService.getCar(anyLong())).thenReturn(new CarDetailResponse());

        mockMvc.perform(get("/api/car/1"))
                .andExpect(status().isOk());
    }

    // --- PATCH /{id} (user) ---

    @Test
    void patchCar_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(patch("/api/car/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockAuthenticatedUser
    void patchCar_returns200_whenAuthenticated() throws Exception {
        when(carService.patchCar(anyLong(), any(), any())).thenReturn(new CarResponse());

        mockMvc.perform(patch("/api/car/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    // --- PATCH /{id}/admin ---

    @Test
    void patchCarAdmin_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(patch("/api/car/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockAuthenticatedUser(role = "USER")
    void patchCarAdmin_returns403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/car/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockAuthenticatedUser(role = "ADMIN")
    void patchCarAdmin_returns200_whenUserIsAdmin() throws Exception {
        when(carService.patchCarAdmin(anyLong(), any())).thenReturn(new CarResponse());

        mockMvc.perform(patch("/api/car/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}
