package com.example.unittestpractice.service;

import com.example.unittestpractice.entity.Order;
import com.example.unittestpractice.entity.OrderStatus;
import com.example.unittestpractice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest; //datajpatest
import org.testcontainers.containers.PostgreSQLContainer; //docker containerde postgreSQL acir
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// default olaraq h2 istifade edilir, bu ise deyir ki, men sene ozum databaza verecem ondan istifade et
class OrderRepositoryIntegrationTest {

    @Container //dockerda acir
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            //konteyner yaradiriq
            .withDatabaseName("testdb") //db adi
            .withUsername("test") //user
            .withPassword("test"); //parol

    @Autowired //ozu avtomatik orderRepo yaradir
    private OrderRepository orderRepository;

    @Test
    void shouldSaveAndFindOrder() { //save ve find olmasini yoxlamaq


        Order order = new Order();
        order.setBookId("BOOK-1");
        order.setCustomerId("CUST-1");
        order.setAmount(19.99);
        order.setStatus(OrderStatus.COMPLETED);

        Order saved = orderRepository.save(order);
        assertThat(saved.getId()).isNotNull(); //db id generate edibmi

        Order found = orderRepository.findById(saved.getId()).orElse(null); //dbden oxumaq
        assertThat(found).isNotNull(); //tapildigini yoxlayir, nulldursa fail
        assertThat(found.getBookId()).isEqualTo("BOOK-1"); //duzgunluyunu yoxlayir
    }
}