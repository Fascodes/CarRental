package dev.fascodes.carRental.user.controller;


import dev.fascodes.carRental.user.dto.AuthResponse;
import dev.fascodes.carRental.user.dto.LoginRequest;
import dev.fascodes.carRental.user.dto.RegisterRequest;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @Valid
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(service.register(req));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(service.login(req));
    }

}