package com.clayjoe.backend.service;

import com.clayjoe.backend.entity.*;
import com.clayjoe.backend.dto.*;
import com.clayjoe.backend.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PricingServiceTest {
    private final PricingService pricing = new PricingService(new BigDecimal("5.30"));
    private Product product(String price) {
        return new Product("陶瓷杯", new BigDecimal(price), 10, "CUP", "手作", "");
    }
    @Test void convertsBothDirectionsAndPreservesBasePrice() {
        Product p = product("32.00");
        assertEquals(new BigDecimal("169.60"), pricing.prices(p).get("CNY"));
        assertEquals(new BigDecimal("32.00"), pricing.prices(p).get("SGD"));
        assertEquals(new BigDecimal("32.00"), p.getPrice());
        assertEquals(new BigDecimal("8.66"), pricing.convert(new BigDecimal("45.90"), "CNY", "SGD"));
    }
    @Test void roundsUnitBeforeQuantityAndFreezesOrderAndPayment() {
        Product p = product("10.05");
        ShopOrder order = new ShopOrder(null, "snapshot");
        order.addItem(p, 3, pricing.unitPrice(p, "CNY"), "CNY");
        assertEquals(new BigDecimal("53.27"), order.getItems().getFirst().getUnitPrice());
        assertEquals(new BigDecimal("159.81"), order.getTotalAmount());
        p.updateDetails("新名称", new BigDecimal("70.00"), 10, "CUP", "新介绍", "");
        var changedRate = new PricingService(new BigDecimal("6.00"));
        assertEquals(new BigDecimal("420.00"), changedRate.unitPrice(p, "CNY"));
        assertEquals(new BigDecimal("159.81"), order.getTotalAmount());
        assertEquals("陶瓷杯", order.getItems().getFirst().getProductName());
        var payment = new PaymentAttempt(order, "payment", PaymentMethod.values()[0], PaymentStatus.values()[0]);
        assertEquals(order.getTotalAmount(), payment.getAmount());
        assertEquals("CNY", payment.getCurrency());
    }
    @Test void rejectsUnsupportedCurrenciesAndInvalidRates() {
        assertThrows(ResponseStatusException.class, () -> pricing.convert(BigDecimal.ONE, "USD", "CNY"));
        assertThrows(ResponseStatusException.class, () -> pricing.convert(BigDecimal.ONE, "SGD", "USD"));
        assertThrows(IllegalArgumentException.class, () -> new PricingService(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new PricingService(new BigDecimal("101")));
    }
    @Test void unavailablePricesDoNotBecomeZeroPricedOrders() {
        assertTrue(pricing.prices(product("0")).isEmpty());
        assertThrows(ResponseStatusException.class, () -> pricing.unitPrice(product("0"), "CNY"));
        assertThrows(ResponseStatusException.class, () -> pricing.convert(new BigDecimal("0.01"), "CNY", "SGD"));
    }
    @Test void orderRejectsMixingSettlementCurrencies() {
        ShopOrder order = new ShopOrder(null, "mixed");
        Product p = product("10.00");
        order.addItem(p, 1, pricing.unitPrice(p, "CNY"), "CNY");
        assertThrows(IllegalArgumentException.class, () -> order.addItem(p, 1, pricing.unitPrice(p, "SGD"), "SGD"));
    }
    @Test void checkoutUsesRequestedCurrencyForMixedBasePrices() {
        AppUserRepository users = mock(AppUserRepository.class);
        CartItemRepository cart = mock(CartItemRepository.class);
        ShopOrderRepository orders = mock(ShopOrderRepository.class);
        EntityManager em = mock(EntityManager.class);
        AppUser user = mock(AppUser.class);
        when(user.getId()).thenReturn(1L);
        when(users.findByUsernameForUpdate("demo")).thenReturn(Optional.of(user));
        when(orders.findByUser_IdAndRequestKey(eq(1L), anyString())).thenReturn(Optional.empty());
        Product sgd = product("10.00");
        Product cny = product("53.00");
        ReflectionTestUtils.setField(sgd, "id", 1L);
        ReflectionTestUtils.setField(cny, "id", 2L);
        ReflectionTestUtils.setField(cny, "currency", "CNY");
        when(cart.findByUser_IdOrderByIdAsc(1L)).thenReturn(List.of(new CartItem(user, sgd, 2), new CartItem(user, cny, 1)));
        when(orders.saveAndFlush(any(ShopOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new OrderService(users, cart, orders, em, pricing, mock(OrderNotificationService.class));
        var result = service.checkout("demo", new CreateOrderRequest("f86f7e2d-dbf0-4cb8-88e6-448b16d0f789", "A", "123", "Singapore", "CNY", "CN", "CN"));
        assertEquals("CNY", result.currency());
        assertEquals("CN", result.market());
        assertEquals("CN", result.shippingCountry());
        assertEquals(new BigDecimal("159.00"), result.totalAmount());
        assertEquals(8, sgd.getStock());
        assertEquals(9, cny.getStock());
        verify(cart).deleteAll(anyList());
    }
    @Test void retryReturnsOriginalSnapshotButRejectsChangedCurrency() {
        AppUserRepository users = mock(AppUserRepository.class);
        CartItemRepository cart = mock(CartItemRepository.class);
        ShopOrderRepository orders = mock(ShopOrderRepository.class);
        AppUser user = mock(AppUser.class);
        when(user.getId()).thenReturn(1L);
        when(users.findByUsernameForUpdate("demo")).thenReturn(Optional.of(user));
        ShopOrder existing = new ShopOrder(user, "existing");
        existing.addItem(product("10.00"), 1, new BigDecimal("53.00"), "CNY");
        when(orders.findByUser_IdAndRequestKey(1L, "existing")).thenReturn(Optional.of(existing));
        var service = new OrderService(users, cart, orders, mock(EntityManager.class), pricing, mock(OrderNotificationService.class));
        assertEquals(new BigDecimal("53.00"), service.checkout("demo", new CreateOrderRequest("existing", "A", "123", "Singapore", "CNY", "CN", "CN")).totalAmount());
        assertThrows(ResponseStatusException.class, () -> service.checkout("demo", new CreateOrderRequest("existing", "A", "123", "Singapore", "SGD", "SG", "SG")));
        verifyNoInteractions(cart);
        verify(orders, never()).saveAndFlush(any());
    }
    @Test
    void rejectsDifferentShippingCountry() {
        // 中国站，却选择配送到新加坡：必须拒绝
        var request = new CreateOrderRequest(
                "f86f7e2d-dbf0-4cb8-88e6-448b16d0f789",
                "Test customer",
                "12345678",
                "Test address",
                "CNY",
                "CN",
                "SG"
        );

        assertRejectedBeforeDatabaseAccess(request);
    }

    @Test
    void rejectsCurrencyThatDoesNotMatchMarket() {
        // 新加坡站，却提交人民币结算：必须拒绝
        var request = new CreateOrderRequest(
                "f86f7e2d-dbf0-4cb8-88e6-448b16d0f789",
                "Test customer",
                "12345678",
                "Test address",
                "CNY",
                "SG",
                "SG"
        );

        assertRejectedBeforeDatabaseAccess(request);
    }

    private void assertRejectedBeforeDatabaseAccess(
            CreateOrderRequest request
    ) {
        var users = mock(AppUserRepository.class);
        var cart = mock(CartItemRepository.class);
        var orders = mock(ShopOrderRepository.class);
        var em = mock(EntityManager.class);

        var service = new OrderService(
                users, cart, orders, em, pricing, mock(OrderNotificationService.class)
        );

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.checkout("demo", request)
        );

        assertEquals(400, error.getStatusCode().value());

        verifyNoInteractions(users, cart, orders, em);
    }
}
