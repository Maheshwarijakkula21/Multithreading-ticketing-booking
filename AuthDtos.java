package com.ticketbooking.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(@NotBlank @Size(max=100) String name, @NotBlank @Size(max=80) String username,
                                  @NotBlank @Size(min=8,max=72) String password) {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record UserResponse(Long id, String name, String username, String role) {}
}
