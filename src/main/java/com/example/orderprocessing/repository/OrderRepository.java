package com.example.orderprocessing.repository;

import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    // Pageable versions for pagination
    // For list endpoints we only fetch the `user` to avoid loading items/products (avoids N+1)
    @EntityGraph(attributePaths = {"products", "products.product", "user"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"products", "products.product", "user"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"products", "products.product", "user"})
    Page<Order> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"products", "products.product", "user"})
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"products", "products.product", "user"})
    Optional<Order> findById(Long id);
}

