package ijse.cmjd.cw3.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ijse.cmjd.cw3.dto.PaymentRequest;
import ijse.cmjd.cw3.dto.PaymentResponse;
import ijse.cmjd.cw3.entity.Order;
import ijse.cmjd.cw3.entity.OrderStatus;
import ijse.cmjd.cw3.entity.Payment;
import ijse.cmjd.cw3.entity.PaymentStatus;
import ijse.cmjd.cw3.entity.Role;
import ijse.cmjd.cw3.entity.User;
import ijse.cmjd.cw3.exception.BadRequestException;
import ijse.cmjd.cw3.exception.DuplicateResourceException;
import ijse.cmjd.cw3.exception.ResourceNotFoundException;
import ijse.cmjd.cw3.exception.UnauthorizedAccessException;
import ijse.cmjd.cw3.repo.OrderRepo;
import ijse.cmjd.cw3.repo.PaymentRepo;
import ijse.cmjd.cw3.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service handling payment processing and payment status management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;

    @Transactional
    public PaymentResponse processPayment(String requesterEmail, PaymentRequest request) {
        Order order = orderRepo.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        User requester = userRepo.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));

        if (requester.getRole() != Role.ADMIN && !order.getUser().getId().equals(requester.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to pay for this order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot process payment for a cancelled order");
        }

        Payment existingPayment = paymentRepo.findByOrderId(order.getId()).orElse(null);
        if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.COMPLETED) {
            throw new DuplicateResourceException("Payment has already been completed for order id: " + order.getId());
        }

        String txRef = (request.getTransactionRef() != null && !request.getTransactionRef().isBlank())
                ? request.getTransactionRef().trim()
                : "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PaymentStatus initialStatus = "CASH_ON_DELIVERY".equalsIgnoreCase(request.getPaymentMethod().trim())
                ? PaymentStatus.PENDING
                : PaymentStatus.COMPLETED;

        Payment payment;
        if (existingPayment != null) {
            existingPayment.setPaymentMethod(request.getPaymentMethod().trim());
            existingPayment.setTransactionRef(txRef);
            existingPayment.setAmount(order.getTotalAmount());
            existingPayment.setStatus(initialStatus);
            existingPayment.setPaymentDate(LocalDateTime.now());
            payment = existingPayment;
        } else {
            payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentMethod(request.getPaymentMethod().trim())
                    .transactionRef(txRef)
                    .status(initialStatus)
                    .paymentDate(LocalDateTime.now())
                    .build();
        }

        Payment saved = paymentRepo.save(payment);
        log.info("Processed payment id={} for order id={} (method={}, status={})",
                saved.getId(), order.getId(), saved.getPaymentMethod(), saved.getStatus());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId, String requesterEmail) {
        Payment payment = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        verifyAccess(payment.getOrder(), requesterEmail);
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId, String requesterEmail) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        verifyAccess(payment.getOrder(), requesterEmail);
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        payment.setStatus(status);
        Payment updated = paymentRepo.save(payment);
        log.info("Updated payment id={} status to {}", paymentId, status);
        return mapToResponse(updated);
    }

    private void verifyAccess(Order order, String requesterEmail) {
        User requester = userRepo.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));
        if (requester.getRole() != Role.ADMIN && !order.getUser().getId().equals(requester.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to view this payment");
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionRef(payment.getTransactionRef())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}
