package com.app.memorias.auth;

import com.app.memorias.auth.dto.AuthResponse;
import com.app.memorias.auth.dto.GoogleLoginRequest;
import com.app.memorias.auth.dto.LoginRequest;
import com.app.memorias.auth.dto.RegisterRequest;
import com.app.memorias.auth.dto.RegisterResponse;
import com.app.memorias.user.AppUser;
import com.app.memorias.user.AuthProvider;
import com.app.memorias.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, GoogleAuthService googleAuthService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleAuthService = googleAuthService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String username = request.username().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya está registrado");
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAuthProvider(AuthProvider.LOCAL);
        AppUser savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getId().toString(), savedUser.getEmail(), savedUser.getUsername(), "Usuario registrado correctamente");
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        AppUser user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleAuthService.GoogleUser googleUser = googleAuthService.verifyIdToken(request.idToken());
        String email = normalizeEmail(googleUser.email());
        AppUser user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> createGoogleUser(googleUser));
        if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
            user.setGoogleId(googleUser.googleId());
            userRepository.save(user);
        }
        return buildAuthResponse(user);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailIgnoreCase(normalizeEmail(email));
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    private AppUser createGoogleUser(GoogleAuthService.GoogleUser googleUser) {
        AppUser user = new AppUser();
        user.setEmail(normalizeEmail(googleUser.email()));
        user.setUsername(generateUniqueUsername(googleUser.email()));
        user.setGoogleId(googleUser.googleId());
        user.setAuthProvider(AuthProvider.GOOGLE);
        return userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, null, user.getId().toString(), user.getEmail(), user.getUsername());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        if (base.length() < 3) {
            base = "user";
        }
        String candidate = base;
        int counter = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = base + counter;
            counter++;
        }
        return candidate;
    }
}