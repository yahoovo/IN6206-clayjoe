package com.clayjoe.backend.controller;
import com.clayjoe.backend.dto.UpdateCartQuantityRequest;
import org.springframework.http.ResponseEntity;
import com.clayjoe.backend.dto.AddCartItemRequest;
import com.clayjoe.backend.dto.CartItemResponse;
import com.clayjoe.backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }
    @PutMapping("/items/{itemId}")
    public CartItemResponse updateQuantity(
            Authentication authentication,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody UpdateCartQuantityRequest request) {

        return service.updateQuantity(
                authentication.getName(),
                itemId,
                request
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            Authentication authentication,
            @PathVariable("itemId") Long itemId) {

        service.removeItem(authentication.getName(), itemId);

        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public List<CartItemResponse> getCart(Authentication authentication) {
        return service.getCart(authentication.getName());
    }

    @PostMapping("/items")
    public CartItemResponse addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request) {

        return service.addItem(authentication.getName(), request);
    }
}