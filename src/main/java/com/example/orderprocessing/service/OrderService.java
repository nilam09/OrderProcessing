package com.example.orderprocessing.service;

import com.example.orderprocessing.dto.CreateOrderProductRequest;
import com.example.orderprocessing.dto.CreateOrderRequest;
import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderProduct;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.model.Product;
import com.example.orderprocessing.model.Role;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.example.orderprocessing.model.User;
import com.example.orderprocessing.repository.OrderRepository;
import com.example.orderprocessing.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request, User user) {
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.PENDING);

        if (request.getItems() != null) {
            for (CreateOrderProductRequest itemRequest : request.getItems()) {
                OrderProduct op = new OrderProduct();
                op.setQuantity(itemRequest.getQuantity());
                Long productId = itemRequest.getProductId();
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product with ID " + productId + " does not exist"));
                op.setProduct(product);
                order.addProduct(op);
            }
        }

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrder(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Order> listOrders(Optional<OrderStatus> status, User user, Pageable pageable) {
        if (user.getRole() == Role.ADMIN) {
            return status.map(s -> orderRepository.findByStatus(s, pageable))
                    .orElseGet(() -> orderRepository.findAll(pageable));
        } else {
            return status.map(s -> orderRepository.findByUserAndStatus(user, s, pageable))
                    .orElseGet(() -> orderRepository.findByUser(user, pageable));
        }
    }

    @Transactional
    public Optional<Order> updateStatus(Long id, OrderStatus newStatus) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(newStatus);
            return orderRepository.save(order);
        });
    }

    @Transactional
    public Optional<Order> cancelOrder(Long id) {
        return orderRepository.findById(id).map(order -> {
            if (order.getStatus() != OrderStatus.PENDING) {
                return null;
            }
            order.setStatus(OrderStatus.CANCELLED);
            return orderRepository.save(order);
        });
    }

    @Transactional
    public void autoUpdatePendingOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        
        if (!pendingOrders.isEmpty()) {
            for (Order order : pendingOrders) {
                order.setStatus(OrderStatus.PROCESSING);
                order.setUpdatedAt(LocalDateTime.now());
            }
            orderRepository.saveAll(pendingOrders);
            System.out.println("Scheduler: Moved " + pendingOrders.size() + " PENDING orders to PROCESSING status");
        }
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return order.getProducts().stream()
            .map(op -> op.getProduct())
            .collect(Collectors.toList());
    }

    public com.example.orderprocessing.dto.OrderDTO toOrderDTO(Order order, boolean includeProducts) {
        return new com.example.orderprocessing.dto.OrderDTO(order, includeProducts);
    }

    public com.example.orderprocessing.dto.OrderSummaryDTO toOrderSummaryDTO(Order order) {
        return new com.example.orderprocessing.dto.OrderSummaryDTO(order);
    }
}

