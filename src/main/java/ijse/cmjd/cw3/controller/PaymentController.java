package ijse.cmjd.cw3.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ijse.cmjd.cw3.dto.PaymentRequest;
import ijse.cmjd.cw3.dto.PaymentResponse;
import ijse.cmjd.cw3.dto.PaymentStatusUpdateRequest;
import ijse.cmjd.cw3.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for processing and managing order payments.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse created = paymentService.processPayment(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(paymentService.getPaymentById(id, authentication.getName()));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            Authentication authentication) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId, authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, request.getStatus()));
    }
}
