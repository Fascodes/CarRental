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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/car")
public class CarController {
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @Valid
    @PostMapping("/add")
    public ResponseEntity<AddCarResponse> addCarToUser(@RequestBody AddCarRequest req){

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        System.out.println("Email from token: " + email);
        return ResponseEntity.ok(carService.addCarToUser(req, email));
    }
}
