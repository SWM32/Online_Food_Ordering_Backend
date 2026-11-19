package ijse.cmjd.cw3.service;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ijse.cmjd.cw3.dto.AuthRequest;
import ijse.cmjd.cw3.dto.AuthResponse;
import ijse.cmjd.cw3.dto.RegisterRequest;
import ijse.cmjd.cw3.dto.UserResponse;
import ijse.cmjd.cw3.entity.Cart;
import ijse.cmjd.cw3.entity.Role;
import ijse.cmjd.cw3.entity.User;
import ijse.cmjd.cw3.exception.DuplicateResourceException;
import ijse.cmjd.cw3.exception.ResourceNotFoundException;
import ijse.cmjd.cw3.repo.UserRepo;
import ijse.cmjd.cw3.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service handling user registration, authentication, and user management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already taken: " + email);
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.CUSTOMER;

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .build();

        Cart cart = Cart.builder().user(newUser).build();
        newUser.setCart(cart);

        User saved = userRepo.save(newUser);
        log.info("Registered new user '{}' with role={}", saved.getEmail(), saved.getRole());
        return mapToResponse(saved);
    }

    public AuthResponse authenticateUser(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String jwt = jwtUtil.generateToken(userDetails);
        log.info("Authenticated user '{}'", email);
        return new AuthResponse(jwt);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepo.delete(user);
        log.info("Deleted user id={} ('{}')", id, user.getEmail());
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
