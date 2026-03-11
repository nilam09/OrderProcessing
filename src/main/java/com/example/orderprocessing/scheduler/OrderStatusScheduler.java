package com.example.orderprocessing.scheduler;

import com.example.orderprocessing.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    private final OrderService orderService;

    public OrderStatusScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 300_000) // 5 minutes = 300,000 milliseconds
    public void updatePendingOrdersToProcessing() {
        log.info("=== Order Status Scheduler Running ===");
        log.info("Attempting to move all PENDING orders to PROCESSING status...");
        try {
            orderService.autoUpdatePendingOrders();
            log.info("=== Scheduler execution completed ===");
        } catch (Exception e) {
            log.error("Error during scheduler execution: ", e);
        }
    }
}

