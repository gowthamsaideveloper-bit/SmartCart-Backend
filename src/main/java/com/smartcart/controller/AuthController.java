package com.smartcart.controller;

import com.smartcart.config.CurrentUser;
import com.smartcart.dto.ApiResponse;
import com.smartcart.dto.LoginRequest;
import com.smartcart.dto.RegisterRequest;
import com.smartcart.model.User;
import com.smartcart.repository.UserRepository;
import com.smartcart.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public AuthController(UserRepository userRepository, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody RegisterRequest request) {
        String name = clean(request.getName());
        String email = normaliseEmail(request.getEmail());
        String mobile = clean(request.getMobile());
        String password = request.getPassword();

        String validationError = validateRegister(name, email, password);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(validationError));
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("This email is already registered. Please login."));
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setMobile(mobile);
        newUser.setRole("CUSTOMER");
        newUser.setPassword(PasswordUtil.hash(password));
        newUser.setAuthToken(generateToken());

        User saved = userRepository.save(newUser);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", authPayload(saved)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        String email = normaliseEmail(request.getEmail());
        String password = request.getPassword();

        if (email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Please enter email and password."));
        }

        return userRepository.findByEmailIgnoreCase(email)
                .filter(user -> PasswordUtil.matches(password, user.getPassword()))
                .map(user -> {
                    user.setEmail(normaliseEmail(user.getEmail()));
                    user.setAuthToken(generateToken());
                    User saved = userRepository.save(user);
                    return ResponseEntity.ok(ApiResponse.ok("Login successful", authPayload(saved)));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(ApiResponse.fail("Invalid email or password")));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> profile(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile fetched", toSafeUser(currentUser.get(request))));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        User user = currentUser.get(request);
        user.setAuthToken(null);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Logout successful", "logged_out"));
    }

    private String validateRegister(String name, String email, String password) {
        if (name.isBlank()) return "Please enter your name.";
        if (email.isBlank()) return "Please enter your email.";
        if (!EMAIL_PATTERN.matcher(email).matches()) return "Please enter a valid email address.";
        if (password == null || password.isBlank()) return "Please enter a password.";
        if (password.length() < 6) return "Password must be at least 6 characters.";
        return null;
    }

    private String normaliseEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private Map<String, Object> authPayload(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", user.getAuthToken());
        data.put("user", toSafeUser(user));
        return data;
    }

    private Map<String, Object> toSafeUser(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("mobile", user.getMobile());
        data.put("role", user.getRole());
        return data;
    }
}
