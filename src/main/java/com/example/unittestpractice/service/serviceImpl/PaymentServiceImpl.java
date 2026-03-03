package com.example.unittestpractice.service.serviceImpl;

import com.example.unittestpractice.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Override
    public boolean checkPayment(String customerId, double amount) {
        return true;
    }

}
