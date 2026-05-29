package dev.fascodes.carRental.user.controller;

import dev.fascodes.carRental.common.security.AuthenticatedUser;
import dev.fascodes.carRental.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(@AuthenticationPrincipal AuthenticatedUser user) {
        String username = userService.getUsername(user.email());
        return ResponseEntity.ok(new UserProfileResponse(username, user.role()));
    }

    record UserProfileResponse(String username, String role) {}
}
