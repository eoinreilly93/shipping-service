package com.shop.generic.shippingservice.kafka;

import com.shop.generic.common.kmos.OrderKMO;
import com.shop.generic.shippingservice.services.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@KafkaListener(topics = "orders", groupId = "orders-group")
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderListener {

    private final ShippingService shippingService;

    @KafkaHandler
    public void processOrder(final OrderKMO order) {
        log.info("Processing order: {}", order);
        this.shippingService.createOrUpdatePendingShipmentManifest(order);
        log.info("Order has been processed ");
    }
}
