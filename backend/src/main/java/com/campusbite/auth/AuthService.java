
package com.campusbite.auth;

import com.campusbite.users.AppUser;
import com.campusbite.users.Role;
import com.campusbite.users.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String staffSignupCode;
    private static final java.util.Set<String> OUTLETS = java.util.Set.of("Normal Canteen", "Nescafé", "Amul", "Juice Corner");

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${campusbite.signup.staff-code:}") String staffSignupCode) {
        this.staffSignupCode = staffSignupCode == null ? "" : staffSignupCode.trim();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists");
        }

        Role role = Role.CUSTOMER;
        String outlet = null;
        String requested = request.role() == null ? "" : request.role().trim().toUpperCase();
        if (requested.equals("STAFF") || requested.equals("ADMIN")) {
            // Staff/admin sign-up is protected by a shared access code (campusbite.signup.staff-code).
            // Leave the code empty in application.properties to make it open to everyone.
            if (!staffSignupCode.isEmpty() && !staffSignupCode.equals(request.accessCode() == null ? "" : request.accessCode().trim()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong staff/admin access code");
            role = Role.valueOf(requested);
            if (role == Role.STAFF) {
                outlet = request.assignedOutlet() == null ? "" : request.assignedOutlet().trim();
                if (!OUTLETS.contains(outlet))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose the outlet you work at");
            }
        } else if (!requested.isEmpty() && !requested.equals("CUSTOMER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot sign up as " + requested.toLowerCase());
        }

        AppUser user = new AppUser(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                role,
                outlet);

        AppUser savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getRole());

        return new AuthResponse(
                token,
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole());
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(
                request.password(),
                user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole());

        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole());
    }
}
