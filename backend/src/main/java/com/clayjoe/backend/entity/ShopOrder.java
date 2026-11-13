package com.clayjoe.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "shop_orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_user_request",
                columnNames = {"user_id", "request_key"}
        )
)
public class ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "shipping_carrier", length = 100)
    private String shippingCarrier;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "shipped_at")
    private Instant shippedAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @Column(name = "request_key", nullable = false, length = 36)
    private String requestKey;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "market", length = 2, updatable = false)
    private String market;

    @Column(name = "shipping_country", length = 2, updatable = false)
    private String shippingCountry;
    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 30)
    private String recipientPhone;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.PERSIST
    )
    private List<OrderItem> items = new ArrayList<>();

    protected ShopOrder() {
    }

    public ShopOrder(AppUser user, String requestKey) {
        this.user = user;
        this.requestKey = requestKey;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.totalAmount = new BigDecimal("0.00");
        this.currency = "CNY";
        this.createdAt = Instant.now();
    }
    public String getRequestKey() {
        return requestKey;
    }
    public void cancel() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("只有待付款订单可以取消");
        }

        status = OrderStatus.CANCELLED;
    }
    public void markPaid() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("只有待付款订单可以支付");
        }

        status = OrderStatus.PAID;
    }
    public void ship(String carrier, String trackingNumber) {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("只有已付款订单可以发货");
        }

        if (carrier == null || carrier.isBlank()
                || trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("物流公司和运单号不能为空");
        }

        String cleanCarrier = carrier.strip();
        String cleanTracking = trackingNumber.strip();

        if (cleanCarrier.length() > 100 || cleanTracking.length() > 100) {
            throw new IllegalArgumentException("物流信息过长");
        }

        this.shippingCarrier = cleanCarrier;
        this.trackingNumber = cleanTracking;
        this.shippedAt = Instant.now();
        this.status = OrderStatus.SHIPPED;
    }

    public void complete() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("只有已发货订单可以确认收货");
        }
        status = OrderStatus.COMPLETED;
    }
    public void addItem(Product product, int quantity, BigDecimal unitPrice, String settlementCurrency) {
        if (!"CNY".equals(settlementCurrency) && !"SGD".equals(settlementCurrency)) {
            throw new IllegalArgumentException("Unsupported settlement currency");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("数量必须至少为1");
        }

        if (unitPrice == null || unitPrice.signum() <= 0 || unitPrice.scale() != 2) {
            throw new IllegalArgumentException("商品价格不可用");
        }

        if (items.isEmpty()) {
            currency = settlementCurrency;
        } else if (!currency.equals(settlementCurrency)) {
            throw new IllegalArgumentException("同一订单不能混合币种");
        }
        OrderItem item = new OrderItem(this, product, quantity, unitPrice);
        items.add(item);
        totalAmount = totalAmount.add(item.getSubtotal());
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public String getShippingCarrier() {
        return shippingCarrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }
    public String getCurrency() {
        return currency;
    }
    public void setShippingDetails(
            String recipientName,
            String recipientPhone,
            String shippingAddress) {

        if (recipientName == null || recipientName.isBlank()
                || recipientPhone == null || recipientPhone.isBlank()
                || shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("收货信息不能为空");
        }

        String name = recipientName.strip();
        String phone = recipientPhone.strip();
        String address = shippingAddress.strip();

        if (name.length() > 100
                || phone.length() > 30
                || address.length() > 500) {
            throw new IllegalArgumentException("收货信息超过允许长度");
        }

        this.recipientName = name;
        this.recipientPhone = phone;
        this.shippingAddress = address;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }
    public String getMarket() {
        return market;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setMarketDetails(String market, String shippingCountry) {
        if (!"CN".equals(market) && !"SG".equals(market)) {
            throw new IllegalArgumentException("购物地区不合法");
        }

        if (!market.equals(shippingCountry)) {
            throw new IllegalArgumentException("购物地区与收货国家不一致");
        }

        if (this.market != null || this.shippingCountry != null) {
            throw new IllegalStateException("订单地区已经确定，不能修改");
        }

        this.market = market;
        this.shippingCountry = shippingCountry;
    }
}