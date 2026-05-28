package dev.fascodes.carRental.common.security;

public record AuthenticatedUser(String email, String role) {
}
