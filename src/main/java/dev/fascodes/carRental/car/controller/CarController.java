package dev.fascodes.carRental.car.controller;

import dev.fascodes.carRental.car.dto.AddCarRequest;
import dev.fascodes.carRental.car.dto.AddCarResponse;
import dev.fascodes.carRental.car.dto.CarDetailResponse;
import dev.fascodes.carRental.car.dto.CarResponse;
import dev.fascodes.carRental.car.dto.PatchCarRequest;
import dev.fascodes.carRental.car.service.CarService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/car")
public class CarController {
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping("/add")
    public ResponseEntity<AddCarResponse> addCarToUser(@Valid @RequestBody AddCarRequest req){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(carService.addCarToUser(req, email));
    }

    // TODO: Here there needs to be a mechanism/db trigger for deleting cars along with their listings - fk relation, while cascading remember to archive crucial elements
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCar(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        carService.removeCar(id, email);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<CarDetailResponse> getCar(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getCar(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CarResponse> patchCar(@PathVariable Long id,
                                                 @Valid @RequestBody PatchCarRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(carService.patchCar(id, request, email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/admin")
    public ResponseEntity<CarResponse> patchCarAdmin(@PathVariable Long id,
                                                      @Valid @RequestBody PatchCarRequest request) {
        return ResponseEntity.ok(carService.patchCarAdmin(id, request));
    }
}
