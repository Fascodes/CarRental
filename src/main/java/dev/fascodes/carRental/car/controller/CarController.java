package dev.fascodes.carRental.car.controller;

import dev.fascodes.carRental.car.dto.AddCarRequest;
import dev.fascodes.carRental.car.dto.AddCarResponse;
import dev.fascodes.carRental.car.service.CarService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;

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
        System.out.println("Email from token: " + email);
        return ResponseEntity.ok(carService.addCarToUser(req, email));
    }

    // TODO: Here there needs to be a mechanism/db trigger for deleting cars along with their listings - fk relation, while cascading remember to archive crucial elements
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCar(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        carService.removeCar(id, email);
        return ResponseEntity.noContent().build();
    }


    // TODO: PUT endpoint client side - client id fetched from db / session email etc.

    // TODO: PATCH endpoint admin side - admin can edit select data from entity so that owner id is not changed
}
