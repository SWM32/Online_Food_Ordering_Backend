package ijse.cmjd.cw3.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ijse.cmjd.cw3.dto.OrderItemResponse;
import ijse.cmjd.cw3.dto.OrderRequest;
import ijse.cmjd.cw3.dto.OrderResponse;
import ijse.cmjd.cw3.entity.Cart;
import ijse.cmjd.cw3.entity.CartItem;
import ijse.cmjd.cw3.entity.FoodItem;
import ijse.cmjd.cw3.entity.FoodStatus;
import ijse.cmjd.cw3.entity.Order;
import ijse.cmjd.cw3.entity.OrderItem;
import ijse.cmjd.cw3.entity.OrderStatus;
import ijse.cmjd.cw3.entity.Role;
import ijse.cmjd.cw3.entity.User;
import ijse.cmjd.cw3.exception.BadRequestException;
import ijse.cmjd.cw3.exception.ResourceNotFoundException;
import ijse.cmjd.cw3.exception.UnauthorizedAccessException;
import ijse.cmjd.cw3.repo.CartRepo;
import ijse.cmjd.cw3.repo.OrderRepo;
import ijse.cmjd.cw3.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service handling order placement, retrieval, and lifecycle transitions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final CartRepo cartRepo;
    private final UserRepo userRepo;

    @Transactional
    public OrderResponse placeOrder(String email, OrderRequest request) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepo.findByUserEmail(email)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart");
        }

        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PLACED)
                .deliveryAddress(request.getDeliveryAddress().trim())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            FoodItem foodItem = cartItem.getFoodItem();
            if (foodItem.getStatus() != FoodStatus.AVAILABLE) {
                throw new BadRequestException(
                        "Food item '" + foodItem.getName() + "' is currently out of stock");
            }

            BigDecimal lineSubtotal = foodItem.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(lineSubtotal);

            OrderItem orderItem = OrderItem.builder()
                    .foodItem(foodItem)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(foodItem.getPrice())
                    .build();
            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepo.save(order);

        // Clear the user's cart once the order is placed
        cart.getCartItems().clear();
        cartRepo.save(cart);

        log.info("Order id={} placed by user '{}' for totalAmount={}",
                savedOrder.getId(), email, savedOrder.getTotalAmount());
        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, String requesterEmail) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        User requester = userRepo.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));

        if (requester.getRole() != Role.ADMIN && !order.getUser().getId().equals(requester.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to view this order");
        }

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserEmail(String email) {
        return orderRepo.findByUserEmailOrderByOrderDateDesc(email).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(OrderStatus status) {
        List<Order> orders = (status != null)
                ? orderRepo.findByStatusOrderByOrderDateDesc(status)
                : orderRepo.findAllByOrderByOrderDateDesc();
        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException(
                    "Cannot change status of an order that is already " + order.getStatus());
        }

        order.setStatus(newStatus);
        Order updated = orderRepo.save(order);
        log.info("Updated order id={} status to {}", orderId, newStatus);
        return mapToResponse(updated);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String requesterEmail) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        User requester = userRepo.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));

        if (requester.getRole() != Role.ADMIN && !order.getUser().getId().equals(requester.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to cancel this order");
        }

        if (requester.getRole() != Role.ADMIN && order.getStatus() != OrderStatus.PLACED) {
            throw new BadRequestException("Customers can only cancel orders that are in PLACED status");
        }

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order cannot be cancelled in its current state: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepo.save(order);
        log.info("Order id={} cancelled by '{}'", orderId, requesterEmail);
        return mapToResponse(updated);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return OrderItemResponse.builder()
                            .id(item.getId())
                            .foodItemId(item.getFoodItem().getId())
                            .foodItemName(item.getFoodItem().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(subtotal)
                            .build();
                })
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .items(itemResponses)
                .paymentStatus(order.getPayment() != null ? order.getPayment().getStatus() : null)
                .build();
    }
}
