package com.app.memorias.auth;

import com.app.memorias.auth.dto.AuthResponse;
import com.app.memorias.auth.dto.AvailabilityResponse;
import com.app.memorias.auth.dto.GoogleLoginRequest;
import com.app.memorias.auth.dto.LoginRequest;
import com.app.memorias.auth.dto.RegisterRequest;
import com.app.memorias.auth.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request);
    }

    @GetMapping("/check-email")
    public AvailabilityResponse checkEmail(@RequestParam String email) {
        return new AvailabilityResponse(authService.isEmailAvailable(email));
    }

    @GetMapping("/check-username")
    public AvailabilityResponse checkUsername(@RequestParam String username) {
        return new AvailabilityResponse(authService.isUsernameAvailable(username));
    }
}