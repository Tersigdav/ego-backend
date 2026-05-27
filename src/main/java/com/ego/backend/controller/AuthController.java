package com.ego.backend.controller;

import com.ego.backend.dto.*;
import com.ego.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return new AuthResponse(token);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request) {
        String token = authService.register(
                request.getUsername(),
                request.getPassword()
        );

        return new AuthResponse(token);
    }
}
