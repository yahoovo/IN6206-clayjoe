package com.clayjoe.backend.controller;
import org.springframework.web.bind.annotation.PathVariable;
import com.clayjoe.backend.entity.Product;
import com.clayjoe.backend.dto.ProductResponse;
import com.clayjoe.backend.service.PricingService;
import com.clayjoe.backend.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import com.clayjoe.backend.dto.CreateProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.clayjoe.backend.dto.UpdateProductRequest;
import org.springframework.web.bind.annotation.PutMapping;
import java.net.URI;
import com.clayjoe.backend.dto.UpdateProductStatusRequest;
import org.springframework.web.bind.annotation.PatchMapping;
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    private final PricingService pricing;

    public ProductController(ProductService service, PricingService pricing) {
        this.service = service;
        this.pricing = pricing;
    }

    @GetMapping
    public List<ProductResponse> getProducts(
            @RequestParam(name = "category", required = false) String category) {
        return service.getProducts(category).stream().map(p -> ProductResponse.from(p, pricing)).toList();
    }
    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        return ProductResponse.from(service.updateProduct(id, request), pricing);
    }
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable("id") Long id) {
        return ProductResponse.from(service.getVisibleProductById(id), pricing);
    }
    @GetMapping("/manage")
    public List<ProductResponse> getManagementProducts() {
        return service.getAllProductsForManagement().stream().map(p -> ProductResponse.from(p, pricing)).toList();
    }

    @GetMapping("/manage/{id}")
    public ProductResponse getManagementProduct(@PathVariable("id") Long id) {
        return ProductResponse.from(service.getProductById(id), pricing);
    }

    @PatchMapping("/{id}/status")
    public ProductResponse changeProductStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductStatusRequest request) {

        return ProductResponse.from(service.changeProductStatus(id, request.active()), pricing);
    }
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        Product saved = service.createProduct(request);

        return ResponseEntity
                .created(URI.create("/api/products/" + saved.getId()))
                .body(ProductResponse.from(saved, pricing));
    }
}