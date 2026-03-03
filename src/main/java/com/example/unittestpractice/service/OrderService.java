package com.example.unittestpractice.service;


import com.example.unittestpractice.entity.Order;
import com.example.unittestpractice.entity.OrderStatus;
import com.example.unittestpractice.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;

    public OrderService(PaymentService paymentService,
                        InventoryService inventoryService,
                        OrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(String bookId, String customerId, double amount) {

        inventoryService.checkInStock(bookId);

        boolean paymentSuccess = paymentService.checkPayment(customerId, amount);
        if (!paymentSuccess) {
            throw new IllegalStateException("Payment failed for customer " + customerId);
        }

        Order order = new Order();
        order.setBookId(bookId);
        order.setCustomerId(customerId);
        order.setAmount(amount);
        order.setStatus(OrderStatus.COMPLETED);

        return orderRepository.save(order);
    }
}