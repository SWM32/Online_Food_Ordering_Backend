package ijse.cmjd.cw3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ijse.cmjd.cw3.dto.CartItemRequest;
import ijse.cmjd.cw3.dto.CartResponse;
import ijse.cmjd.cw3.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing the authenticated user's shopping cart.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCartByUserEmail(authentication.getName()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(authentication.getName(), request));
    }

    @PutMapping("/items/{foodItemId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            Authentication authentication,
            @PathVariable Long foodItemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(
                cartService.updateCartItemQuantity(authentication.getName(), foodItemId, quantity));
    }

    @DeleteMapping("/items/{foodItemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            Authentication authentication,
            @PathVariable Long foodItemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(authentication.getName(), foodItemId));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.clearCart(authentication.getName()));
    }
}
