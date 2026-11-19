package ijse.cmjd.cw3.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ijse.cmjd.cw3.dto.CartItemRequest;
import ijse.cmjd.cw3.dto.CartItemResponse;
import ijse.cmjd.cw3.dto.CartResponse;
import ijse.cmjd.cw3.entity.Cart;
import ijse.cmjd.cw3.entity.CartItem;
import ijse.cmjd.cw3.entity.FoodItem;
import ijse.cmjd.cw3.entity.FoodStatus;
import ijse.cmjd.cw3.entity.User;
import ijse.cmjd.cw3.exception.BadRequestException;
import ijse.cmjd.cw3.exception.ResourceNotFoundException;
import ijse.cmjd.cw3.repo.CartRepo;
import ijse.cmjd.cw3.repo.FoodItemRepo;
import ijse.cmjd.cw3.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service managing customer shopping cart operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepo cartRepo;
    private final UserRepo userRepo;
    private final FoodItemRepo foodItemRepo;

    @Transactional
    public CartResponse getCartByUserEmail(String email) {
        Cart cart = getOrCreateCart(email);
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse addItemToCart(String email, CartItemRequest request) {
        Cart cart = getOrCreateCart(email);

        FoodItem foodItem = foodItemRepo.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food item not found with id: " + request.getFoodItemId()));

        if (foodItem.getStatus() != FoodStatus.AVAILABLE) {
            throw new BadRequestException("Food item '" + foodItem.getName() + "' is currently out of stock");
        }

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getFoodItem().getId().equals(foodItem.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .foodItem(foodItem)
                    .quantity(request.getQuantity())
                    .build();
            cart.addCartItem(newItem);
        }

        Cart saved = cartRepo.save(cart);
        log.info("Added foodItemId={} (qty={}) to cart for user '{}'",
                foodItem.getId(), request.getQuantity(), email);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse updateCartItemQuantity(String email, Long foodItemId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new BadRequestException("Quantity must be 0 or greater");
        }

        Cart cart = getOrCreateCart(email);
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getFoodItem().getId().equals(foodItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food item with id " + foodItemId + " is not in the cart"));

        if (quantity == 0) {
            cart.removecartItem(cartItem);
            log.info("Removed foodItemId={} from cart for user '{}'", foodItemId, email);
        } else {
            cartItem.setQuantity(quantity);
            log.info("Updated foodItemId={} quantity={} in cart for user '{}'", foodItemId, quantity, email);
        }

        Cart saved = cartRepo.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse removeItemFromCart(String email, Long foodItemId) {
        Cart cart = getOrCreateCart(email);
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getFoodItem().getId().equals(foodItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food item with id " + foodItemId + " is not in the cart"));

        cart.removecartItem(cartItem);
        Cart saved = cartRepo.save(cart);
        log.info("Removed foodItemId={} from cart for user '{}'", foodItemId, email);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getCartItems().clear();
        Cart saved = cartRepo.save(cart);
        log.info("Cleared cart for user '{}'", email);
        return mapToResponse(saved);
    }

    private Cart getOrCreateCart(String email) {
        return cartRepo.findByUserEmail(email)
                .orElseGet(() -> {
                    User user = userRepo.findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
                    Cart newCart = Cart.builder().user(user).build();
                    user.setCart(newCart);
                    return cartRepo.save(newCart);
                });
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getCartItems().stream()
                .map(item -> {
                    BigDecimal unitPrice = item.getFoodItem().getPrice();
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                    return CartItemResponse.builder()
                            .id(item.getId())
                            .foodItemId(item.getFoodItem().getId())
                            .foodItemName(item.getFoodItem().getName())
                            .unitPrice(unitPrice)
                            .quantity(item.getQuantity())
                            .subtotal(subtotal)
                            .build();
                })
                .toList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .build();
    }
}
