package com.example.unittestpractice.service;


import com.example.unittestpractice.entity.Order;
import com.example.unittestpractice.entity.OrderStatus;
import com.example.unittestpractice.exception.OutOfStockException;
import com.example.unittestpractice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) //buna istinaden
class OrderServiceTest {

    @Mock //fake obyektler yaratmaq ucun
    private PaymentService paymentService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService; //obyekti yaradir ve icindekileri inject edir

    private final String BOOK_ID = "BOOK-1";
    private final String CUSTOMER_ID = "CUST-1";
    private final double AMOUNT = 19.99;

    @Test
    void shouldThrowOutOfStockException_whenBookOutOfStock() { //kitabin varligini yoxla
        doThrow(new OutOfStockException(BOOK_ID))
                .when(inventoryService)
                .checkInStock(BOOK_ID);
        //eger inventoryde verilen book idye uygun kitab yoxdursa exception at

        assertThrows(OutOfStockException.class,
                () -> orderService.placeOrder(BOOK_ID, CUSTOMER_ID, AMOUNT));
        //placeOrder ucun exception

        verify(inventoryService, times(1)).checkInStock(BOOK_ID); //bir defe cagrilsin
        verify(paymentService, never()).checkPayment(any(), anyDouble()); //payment cagrilmasin
        verify(orderRepository, never()).save(any()); //dbye save getmesin
    }

    @Test
    void shouldThrowException_whenPaymentFails() { //payment ucun uygunluq
//stok var, payment yox
        when(paymentService.checkPayment(CUSTOMER_ID, AMOUNT))
                .thenReturn(false); //payment cagrilanda false qaytarsin

        assertThrows(IllegalStateException.class,
                () -> orderService.placeOrder(BOOK_ID, CUSTOMER_ID, AMOUNT)); //fail olduqda exception

        verify(inventoryService).checkInStock(BOOK_ID); //check
        verify(paymentService).checkPayment(CUSTOMER_ID, AMOUNT); //check
        verify(orderRepository, never()).save(any()); //save etme
    }

    @Test
    void shouldSaveOrder_whenPaymentAndInventorySuccessful() {
    //her sey okey olsa
        when(paymentService.checkPayment(CUSTOMER_ID, AMOUNT))
                .thenReturn(true); //payment okey

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setBookId(BOOK_ID);
        savedOrder.setCustomerId(CUSTOMER_ID);
        savedOrder.setAmount(AMOUNT);
        savedOrder.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder); //save

        Order result = orderService.placeOrder(BOOK_ID, CUSTOMER_ID, AMOUNT);

        assertNotNull(result); //netice
        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        assertEquals(BOOK_ID, result.getBookId()); //book id
        assertEquals(CUSTOMER_ID, result.getCustomerId()); //customer
        assertEquals(AMOUNT, result.getAmount()); //mebleg

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        //save olunanlara baxmaq ucun
        verify(orderRepository).save(captor.capture());

        Order captured = captor.getValue(); //tutulani qaytarmaq
        assertEquals(OrderStatus.COMPLETED, captured.getStatus()); //status duzgunluyu
        assertEquals(BOOK_ID, captured.getBookId());

        verify(inventoryService).checkInStock(BOOK_ID);
        // verify - returnu olmayan metodlarda istifade edilir
        verify(paymentService).checkPayment(CUSTOMER_ID, AMOUNT);
    }
}

