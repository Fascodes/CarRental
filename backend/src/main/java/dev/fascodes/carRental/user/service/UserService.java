package dev.fascodes.carRental.user.service;


import dev.fascodes.carRental.common.utility.JwtUtil;
import dev.fascodes.carRental.user.dto.AuthResponse;
import dev.fascodes.carRental.user.dto.LoginRequest;
import dev.fascodes.carRental.user.dto.RegisterRequest;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.model.UserRole;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public String register(RegisterRequest req){
        User user = new User();
        user.setRole(UserRole.USER);
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.findByEmail(user.getEmail())
                .ifPresent(u -> { throw new RuntimeException("Email already registered"); });
        userRepository.save(user);

        return "User registered";
    }

    public AuthResponse login(LoginRequest req){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token);
    }
}
