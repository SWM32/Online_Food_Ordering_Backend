package ijse.cmjd.cw3.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ijse.cmjd.cw3.dto.AuthRequest;
import ijse.cmjd.cw3.dto.AuthResponse;
import ijse.cmjd.cw3.dto.RegisterRequest;
import ijse.cmjd.cw3.dto.UserResponse;
import ijse.cmjd.cw3.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller handling user registration (Sign Up) and authentication (Sign In).
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse created = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> createAuthenticationToken(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }
}
