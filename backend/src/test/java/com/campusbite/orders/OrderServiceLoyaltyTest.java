package com.campusbite.orders;

import com.campusbite.loyalty.LoyaltyService;
import com.campusbite.menu.MenuService;
import com.campusbite.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceLoyaltyTest {
    private OrderRepository orders;
    private LoyaltyService loyalty;
    private OrderService service;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class);
        loyalty = mock(LoyaltyService.class);
        service = new OrderService(orders, mock(MenuService.class), mock(UserRepository.class), loyalty,
                mock(MongoTemplate.class));
        when(orders.findByOutletAndQueueDateAndQueueNumberLessThanAndStatusInOrderByQueueNumberAsc(
            anyString(), anyString(), anyLong(), anyList())).thenReturn(List.of());
        when(orders.save(any(CampusOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void doesNotAwardStampForOrderAtOrBelowOneHundredRupees() {
        CampusOrder order = readyOrder(100.0);
        when(orders.findById("order-100")).thenReturn(Optional.of(order));

        service.updateStatus("order-100", OrderStatus.DELIVERED);

        verify(loyalty, never()).awardStamp(anyString(), anyString());
    }

    @Test
    void awardsStampForDeliveredOrderOverOneHundredRupees() {
        CampusOrder order = readyOrder(100.01);
        when(orders.findById("order-100-01")).thenReturn(Optional.of(order));

        service.updateStatus("order-100-01", OrderStatus.DELIVERED);

        verify(loyalty).awardStamp("customer@example.com", order.getId());
    }

    @Test
    void customerCollectionMarksReadyOrderDelivered() {
        CampusOrder order = readyOrder(150.0);
        order.setPaymentStatus("PAID");
        when(orders.findById("ready-order")).thenReturn(Optional.of(order));

        OrderView result = service.markCollected("ready-order", "customer@example.com");

        assertEquals(OrderStatus.DELIVERED, result.order().getStatus());
        verify(loyalty).awardStamp("customer@example.com", order.getId());
    }

    @Test
    void customerCannotCollectUntilStaffMarksPaymentPaid() {
        CampusOrder order = readyOrder(150.0);
        when(orders.findById("unpaid-order")).thenReturn(Optional.of(order));

        assertThrows(ResponseStatusException.class,
                () -> service.markCollected("unpaid-order", "customer@example.com"));
        verify(orders, never()).save(order);
    }

    @Test
    void staffCannotMarkReadyOrderDeliveredWithoutCustomerCollection() {
        CampusOrder order = readyOrder(150.0);
        when(orders.findById("staff-delivery-order")).thenReturn(Optional.of(order));

        assertThrows(ResponseStatusException.class,
                () -> service.updateStatusForStaff("staff-delivery-order", OrderStatus.DELIVERED, "Amul"));
        verify(orders, never()).save(order);
    }

    @Test
    void staffPaymentConfirmationEnablesCollection() {
        CampusOrder order = readyOrder(150.0);
        when(orders.findById("payment-order")).thenReturn(Optional.of(order));
        when(orders.findByOutletAndQueueDateAndQueueNumberLessThanAndStatusInOrderByQueueNumberAsc(
            anyString(), anyString(), anyLong(), anyList())).thenReturn(List.of());

        service.markPaymentPaid("payment-order", "Amul");
        OrderView collected = service.markCollected("payment-order", "customer@example.com");

        assertEquals("PAID", collected.order().getPaymentStatus());
        assertEquals(OrderStatus.DELIVERED, collected.order().getStatus());
    }

    @Test
    void queueCounterRestartsPerOutletOnEachDate() {
        assertEquals("2026-11-16:amul", OrderService.queueCounterId("Amul", "2026-11-16"));
        assertEquals("2026-11-17:amul", OrderService.queueCounterId("Amul", "2026-11-17"));
        assertEquals("2026-11-16:nescafé", OrderService.queueCounterId("Nescafé", "2026-11-16"));
    }

    private CampusOrder readyOrder(double subtotal) {
        CampusOrder order = new CampusOrder("customer@example.com", "Customer", "Amul", 1,
                List.of(), subtotal, 0, 0, subtotal, "DEMO");
        order.setStatus(OrderStatus.READY);
        return order;
    }
}
