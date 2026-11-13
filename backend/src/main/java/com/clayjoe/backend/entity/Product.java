package com.clayjoe.backend.entity;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 2000)
    private String description;
    private Integer stock;
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    @Column(name = "active")
    private Boolean active = true;
    public String getImageUrl() {
        return imageUrl;
    }
    private String name;
    @Column(name = "name_en", length = 255)
    private String nameEn;
    @Column(name = "description_en", length = 2000)
    private String descriptionEn;
    public String getNameEn() { return nameEn; }
    public String getDescriptionEn() { return descriptionEn; }
    @Column(length = 30)
    private String category;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    @Column(length = 3)
    private String currency = "SGD";
    public String getCurrency() {
        return currency == null ? "CNY" : currency;
    }
    protected Product() {
    }
    public Product(
            String name,
            BigDecimal price,
            Integer stock,
            String category,
            String description,
            String imageUrl) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
    }
    public Boolean getActive() {
        return active;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }
    public void restoreStock(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("恢复数量必须大于0");
        }

        if (stock == null || stock < 0) {
            throw new IllegalStateException("商品库存状态异常");
        }

        stock = Math.addExact(stock, quantity);
    }
    public void decreaseStock(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("扣减数量必须大于0");
        }

        if (stock == null || stock < quantity) {
            throw new IllegalStateException("库存不足");
        }

        stock -= quantity;
    }
    public void updateDetails(
            String name,
            BigDecimal price,
            Integer stock,
            String category,
            String description,
            String imageUrl) {

        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
    }
    public Product(String name) {
        this.name = name;
    }
    public String getCategory() {
        return category;
    }
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public Integer getStock() {
        return stock;
    }
    public String getDescription() {
        return description;
    }
    public void updateEnglishContent(
            String nameEn,
            String descriptionEn) {

        if (nameEn != null) {
            this.nameEn = nameEn.strip();
        }

        if (descriptionEn != null) {
            this.descriptionEn = descriptionEn.strip();
        }
    }
}