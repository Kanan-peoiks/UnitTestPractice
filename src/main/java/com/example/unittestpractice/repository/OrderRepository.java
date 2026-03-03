package com.example.unittestpractice.repository;

import com.example.unittestpractice.entity.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order,Long> {
}
