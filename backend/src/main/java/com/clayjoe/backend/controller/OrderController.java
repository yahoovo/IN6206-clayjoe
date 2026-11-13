package com.clayjoe.backend.controller;

import com.clayjoe.backend.dto.CreateOrderRequest;
import com.clayjoe.backend.dto.OrderResponse;
import com.clayjoe.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public OrderResponse checkout(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {

        return service.checkout(authentication.getName(), request);
    }

    @GetMapping
    public List<OrderResponse> getMyOrders(
            Authentication authentication) {

        return service.getMyOrders(authentication.getName());
    }

    @GetMapping("/{id}")
    public OrderResponse getMyOrder(
            Authentication authentication,
            @PathVariable("id") Long id) {

        return service.getMyOrder(authentication.getName(), id);
    }
    @PostMapping("/{id}/complete")
    public OrderResponse completeOrder(
            Authentication authentication,
            @PathVariable("id") Long id) {

        return service.completeOrder(authentication.getName(), id);
    }
    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(
            Authentication authentication,
            @PathVariable("id") Long id) {

        return service.cancelOrder(authentication.getName(), id);
    }
}