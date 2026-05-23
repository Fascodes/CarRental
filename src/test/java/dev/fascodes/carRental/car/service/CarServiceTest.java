package dev.fascodes.carRental.car.service;

import dev.fascodes.carRental.car.mapper.CarMapper;
import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.common.utility.JwtUtil;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock private CarMapper carMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;
    @Mock private CarRepository carRepository;
    @InjectMocks private CarService carService;

    @Test
    void removeCar_throwsNotFound_whenCarDoesNotExist() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> carService.removeCar(1L, "user@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void removeCar_throwsForbidden_whenCallerIsNotOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Car car = new Car();
        car.setOwner(owner);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> carService.removeCar(1L, "other@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // This is a repository test if a repository exclusive file is added move it there
    @Test
    void removeCar_deletesCar_whenCallerIsOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Car car = new Car();
        car.setOwner(owner);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        carService.removeCar(1L, "owner@test.com");

        verify(carRepository).delete(car);
    }
}
