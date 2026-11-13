package com.clayjoe.backend.service;

import com.clayjoe.backend.entity.*;
import com.clayjoe.backend.dto.*;
import com.clayjoe.backend.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.support.*;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OrderNotificationServiceTest {
    private AnnotationConfigApplicationContext context;
    private MockRestServiceServer server;
    private OrderNotificationService notifications;
    private final TestTransactionManager manager = new TestTransactionManager();
    private TransactionTemplate transaction;

    @BeforeEach void setup() {
        var client = new RestTemplate();
        server = MockRestServiceServer.bindTo(client).build();
        context = new AnnotationConfigApplicationContext();
        context.registerBean(TransactionalEventListenerFactory.class);
        context.registerBean(OrderNotificationService.class, () -> new OrderNotificationService(
                context, client, "http://notifications.test", "test-key-for-isolated-tests-only"));
        context.refresh();
        notifications = context.getBean(OrderNotificationService.class);
        transaction = new TransactionTemplate(manager);
    }
    @AfterEach void close() { context.close(); server.verify(); }

    private ShopOrder order() {
        var order = mock(ShopOrder.class);
        var user = mock(AppUser.class);
        when(user.getId()).thenReturn(2L);
        when(order.getUser()).thenReturn(user);
        when(order.getId()).thenReturn(123L);
        return order;
    }

    @Test void sendsOnlyAfterCommitWithOwnerAndBilingualContent() {
        server.expect(requestTo("http://notifications.test/internal/notifications"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Internal-Key", "test-key-for-isolated-tests-only"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.orderId").value(123))
                .andExpect(jsonPath("$.titleZh").value("订单已创建"))
                .andExpect(jsonPath("$.titleEn").value("Order placed"))
                .andExpect(request -> assertTrue(manager.committed))
                .andRespond(withSuccess());
        transaction.executeWithoutResult(status -> notifications.created(order()));
    }

    @Test void rollbackDoesNotSendNotification() {
        transaction.executeWithoutResult(status -> {
            notifications.created(order());
            status.setRollbackOnly();
        });
        assertFalse(manager.committed);
        // No HTTP expectations: any attempted call fails this test.
    }

    @Test void absentTransactionDoesNotSendPrematureNotification() {
        notifications.created(order());
    }

    @Test void unavailableServiceDoesNotFailCommittedOperation() {
        server.expect(requestTo("http://notifications.test/internal/notifications"))
                .andRespond(withServiceUnavailable());
        assertDoesNotThrow(() -> transaction.executeWithoutResult(status -> notifications.paid(order())));
        assertTrue(manager.committed);
    }

    @Test void connectionFailureDoesNotFailCommittedOperation() {
        server.expect(requestTo("http://notifications.test/internal/notifications"))
                .andRespond(withException(new java.io.IOException("Connection unavailable")));
        assertDoesNotThrow(() -> transaction.executeWithoutResult(status -> notifications.created(order())));
        assertTrue(manager.committed);
    }

    @Test void snapshotsShippingDetailsWithStableDistinctEventIds() {
        var publisher = mock(ApplicationEventPublisher.class);
        var service = new OrderNotificationService(publisher, new RestTemplate(), "http://test", "unused");
        var order = order();
        when(order.getShippingCarrier()).thenReturn("Demo Carrier");
        when(order.getTrackingNumber()).thenReturn("TEST123");
        service.shipped(order);
        service.shipped(order);
        service.paid(order);
        var captor = org.mockito.ArgumentCaptor.forClass(OrderNotificationService.OrderNotificationEvent.class);
        verify(publisher, times(3)).publishEvent(captor.capture());
        var events = captor.getAllValues();
        assertEquals(events.get(0).eventId(), events.get(1).eventId());
        assertNotEquals(events.get(0).eventId(), events.get(2).eventId());
        assertTrue(events.get(0).contentEn().contains("TEST123"));
        assertTrue(events.get(2).contentEn().contains("No real money"));
    }

    @Test void repeatedShippingDoesNotPublishTwice() {
        var orders = mock(ShopOrderRepository.class);
        var notify = mock(OrderNotificationService.class);
        var order = new ShopOrder(mock(AppUser.class), "request");
        order.markPaid();
        when(orders.findOrderForUpdate(123L)).thenReturn(Optional.of(order));
        var service = new OrderService(mock(AppUserRepository.class), mock(CartItemRepository.class),
                orders, mock(EntityManager.class), new PricingService(new BigDecimal("5.30")), notify);
        var request = new ShipOrderRequest("Demo Carrier", "TEST123");
        service.shipOrder(123L, request);
        service.shipOrder(123L, request);
        verify(notify, times(1)).shipped(order);
    }

    @Test void onlySuccessfulNewPaymentPublishesNotification() {
        var users = mock(AppUserRepository.class);
        var orders = mock(ShopOrderRepository.class);
        var payments = mock(PaymentAttemptRepository.class);
        var notify = mock(OrderNotificationService.class);
        var user = mock(AppUser.class);
        when(user.getId()).thenReturn(2L);
        when(users.findByUsernameForUpdate("demo")).thenReturn(Optional.of(user));
        var order = new ShopOrder(user, "request");
        var product = new Product("Cup", new BigDecimal("10.00"), 3, "CUP", "", "");
        order.addItem(product, 1, new BigDecimal("10.00"), "SGD");
        when(orders.findOwnedOrderForUpdate(123L, 2L)).thenReturn(Optional.of(order));
        when(payments.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new PaymentService(users, orders, payments, mock(EntityManager.class), notify);
        var failure = java.util.Arrays.stream(PaymentStatus.values())
                .filter(s -> s != PaymentStatus.SUCCEEDED).findFirst().orElseThrow();
        service.simulate("demo", 123L, new SimulatePaymentRequest("failed", PaymentMethod.values()[0], failure));
        verifyNoInteractions(notify);
        var request = new SimulatePaymentRequest("paid", PaymentMethod.values()[0], PaymentStatus.SUCCEEDED);
        service.simulate("demo", 123L, request);
        when(payments.findByOrder_IdAndRequestKey(123L, "paid")).thenReturn(Optional.of(
                new PaymentAttempt(order, "paid", request.paymentMethod(), request.result())));
        service.simulate("demo", 123L, request);
        verify(notify, times(1)).paid(order);
    }

    private static class TestTransactionManager extends AbstractPlatformTransactionManager {
        boolean committed;
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object tx, TransactionDefinition definition) {}
        @Override protected void doCommit(DefaultTransactionStatus status) { committed = true; }
        @Override protected void doRollback(DefaultTransactionStatus status) {}
    }
}
