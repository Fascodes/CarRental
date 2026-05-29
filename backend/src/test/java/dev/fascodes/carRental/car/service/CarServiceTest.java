package dev.fascodes.carRental.car.service;

import dev.fascodes.carRental.car.dto.CarResponse;
import dev.fascodes.carRental.car.dto.PatchCarRequest;
import dev.fascodes.carRental.car.mapper.CarMapper;
import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.listing.repository.ListingRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock private CarMapper carMapper;
    @Mock private UserRepository userRepository;
    @Mock private CarRepository carRepository;
    @Mock private ListingRepository listingRepository;
    @InjectMocks private CarService carService;

    // --- removeCar ---

    @Test
    void removeCar_throwsNotFound_whenCarDoesNotExist() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> carService.removeCar(1L, "user@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void removeCar_throwsNotFound_whenCallerIsNotOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Car car = new Car();
        car.setOwner(owner);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> carService.removeCar(1L, "other@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void removeCar_throwsConflict_whenCarHasListings() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Car car = new Car();
        car.setId(1L);
        car.setOwner(owner);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(listingRepository.existsByCarId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> carService.removeCar(1L, "owner@test.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void removeCar_deletesCar_whenCallerIsOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Car car = new Car();
        car.setId(1L);
        car.setOwner(owner);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(listingRepository.existsByCarId(1L)).thenReturn(false);

        carService.removeCar(1L, "owner@test.com");

        verify(carRepository).delete(car);
    }

    // --- getCar ---

    @Test
    void getCar_throwsNotFound_whenCarDoesNotExist() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> carService.getCar(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- patchCar (user) ---

    @Test
    void patchCar_throwsNotFound_whenCarDoesNotExist() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> carService.patchCar(1L, new PatchCarRequest(), "user@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void patchCar_throwsNotFound_whenCallerIsNotOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Car car = new Car();
        car.setOwner(owner);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> carService.patchCar(1L, new PatchCarRequest(), "other@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void patchCar_appliesPatch_whenCallerIsOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Car car = new Car();
        car.setOwner(owner);
        car.setBrand("Old Brand");
        car.setModel("Old Model");

        PatchCarRequest request = new PatchCarRequest();
        request.setBrand("New Brand");
        // model is null - should not be changed

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carMapper.toCarResponse(car)).thenReturn(new CarResponse());

        carService.patchCar(1L, request, "owner@test.com");

        assertEquals("New Brand", car.getBrand());
        assertEquals("Old Model", car.getModel());
    }

    // --- patchCarAdmin ---

    @Test
    void patchCarAdmin_throwsNotFound_whenCarDoesNotExist() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> carService.patchCarAdmin(1L, new PatchCarRequest()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void patchCarAdmin_appliesPatch_whenCarExists() {
        Car car = new Car();
        car.setModel("Old Model");
        car.setHorsePower(100);

        PatchCarRequest request = new PatchCarRequest();
        request.setModel("New Model");
        // horsePower is null - should not be changed

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carMapper.toCarResponse(car)).thenReturn(new CarResponse());

        carService.patchCarAdmin(1L, request);

        assertEquals("New Model", car.getModel());
        assertEquals(100, car.getHorsePower());
    }

    @Test
    void patchCarAdmin_doesNotCheckOwnership() {
        Car car = new Car();
        User owner = new User();
        owner.setEmail("owner@test.com");
        car.setOwner(owner);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carMapper.toCarResponse(any())).thenReturn(new CarResponse());

        // admin can patch a car they don't own - no exception should be thrown
        carService.patchCarAdmin(1L, new PatchCarRequest());

        verify(carMapper).toCarResponse(car);
    }
}
