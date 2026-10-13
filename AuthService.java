
package com.ticketbooking.service;

import com.ticketbooking.dto.AuthDtos;
import com.ticketbooking.model.User;
import com.ticketbooking.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(
            UserRepository users,
            PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public AuthDtos.UserResponse register(
            AuthDtos.RegisterRequest req) {

        String username = req.username()
                .trim()
                .toLowerCase();

        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists");
        }

        String encodedPassword =
                encoder.encode(req.password());

        User user = new User(
                req.name().trim(),
                username,
                encodedPassword,
                "CUSTOMER"
        );

        User savedUser = users.save(user);

        return response(savedUser);
    }

    @Transactional(readOnly = true)
    public User authenticate(
            AuthDtos.LoginRequest req) {

        String username = req.username()
                .trim()
                .toLowerCase();

        User user = users.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid username or password"));

        String storedHash = user.getPasswordHash();

        if (storedHash == null
                || storedHash.isBlank()
                || !encoder.matches(
                        req.password(),
                        storedHash)) {

            throw new IllegalArgumentException(
                    "Invalid username or password");
        }

        return user;
    }

    public static AuthDtos.UserResponse response(User user) {
        return new AuthDtos.UserResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole()
        );
    }
}
