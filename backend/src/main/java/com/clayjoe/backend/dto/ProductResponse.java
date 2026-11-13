package com.clayjoe.backend.dto;

import com.clayjoe.backend.entity.Product;
import com.clayjoe.backend.service.PricingService;
import java.math.BigDecimal;
import java.util.Map;

public record ProductResponse(Long id, String name, String nameEn, String description,
        String descriptionEn, BigDecimal price, String currency, Integer stock,
        String category, String imageUrl, Boolean active,
        Map<String, BigDecimal> prices, BigDecimal sgdToCnyRate) {
    public static ProductResponse from(Product p, PricingService pricing) {
        return new ProductResponse(p.getId(), p.getName(), p.getNameEn(), p.getDescription(),
                p.getDescriptionEn(), p.getPrice(), p.getCurrency(), p.getStock(),
                p.getCategory(), p.getImageUrl(), p.getActive(), pricing.prices(p), pricing.getRate());
    }
}
