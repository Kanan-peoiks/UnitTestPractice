package com.example.unittestpractice.service;


import com.example.unittestpractice.entity.Order;
import com.example.unittestpractice.entity.OrderStatus;
import com.example.unittestpractice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private final String BOOK_ID = "BOOK-1";
    private final String CUSTOMER_ID = "CUST-1";
    private final double AMOUNT = 19.99;

    @Test
    void shouldThrowException_whenBookOutOfStock() {

        doThrow(new RuntimeException("Out of stock"))
                .when(inventoryService)
                .checkInStock(BOOK_ID);

        assertThrows(RuntimeException.class,
                () -> orderService.placeOrder(BOOK_ID, CUSTOMER_ID, AMOUNT));

        verify(inventoryService, times(1)).checkInStock(BOOK_ID);
        verify(paymentService, never()).checkPayment(any(), anyDouble());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenPaymentFails() {

        when(paymentService.checkPayment(CUSTOMER_ID, AMOUNT))
                .thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> orderService.placeOrder(BOOK_ID, CUSTOMER_ID, AMOUNT));

        verify(inventoryService).checkInStock(BOOK_ID);
        verify(paymentService).checkPayment(CUSTOMER_ID, AMOUNT);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldSaveOrder_whenPaymentAndInventorySuccessful() {

        when(paymentService.checkPayment(CUSTOMER_ID, AMOUNT))
                .thenReturn(true);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setBookId(BOOK_ID);
        savedOrder.setCustomerId(CUSTOMER_ID);
        savedOrder.setAmount(AMOUNT);
        savedOrder.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        Order result = orderService.placeOrder(BOOK_ID, CUSTOMER_ID, AMOUNT);

        assertNotNull(result);
        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        assertEquals(BOOK_ID, result.getBookId());
        assertEquals(CUSTOMER_ID, result.getCustomerId());
        assertEquals(AMOUNT, result.getAmount());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        Order captured = captor.getValue();
        assertEquals(OrderStatus.COMPLETED, captured.getStatus());
        assertEquals(BOOK_ID, captured.getBookId());

        verify(inventoryService).checkInStock(BOOK_ID);
        verify(paymentService).checkPayment(CUSTOMER_ID, AMOUNT);
    }
}

