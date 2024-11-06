package com.shop.generic.shippingservice.services;

import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.common.kmos.OrderKMO;
import com.shop.generic.shippingservice.entities.Shipment;
import com.shop.generic.shippingservice.repositories.ShipmentsRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    private final ShipmentsRepository shipmentsRepository;
    private final OrderStatusUpdatesService orderStatusUpdatesService;

    //Number of orders in a shipment before updating the shipment status
    private final static int ORDER_COUNT_CUTOFF = 2;

    //Number of minutes a shipment must be in PENDING_DELIVERY before being set to DELIVERED
    private final static int SHIPMENT_UPDATE_CUTOFF_MINS = 2;

    public void createOrUpdatePendingShipmentManifest(final OrderKMO orderKMO) {
        //Check if there already is a shipment pending for this city
        final List<Shipment> shipments = this.shipmentsRepository.findAllByCityAndStatusIs(
                orderKMO.city(), OrderStatus.PENDING_DELIVERY);

        //Create a shipment if one doesn't exist
        if (shipments.isEmpty()) {
            final Shipment shipment = new Shipment(List.of(orderKMO.orderId()), orderKMO.city(),
                    OrderStatus.PENDING_DELIVERY, LocalDateTime.now(), LocalDateTime.now());
            this.shipmentsRepository.save(shipment);
            log.info("Created shipment {}", shipment);
        } else {
            //TODO: Add some smarter logic instead of just updating the first one. Something to do later
            final Shipment shipment = shipments.getFirst();
            shipment.getOrderIds().add(orderKMO.orderId());
            shipment.setLastUpdated(LocalDateTime.now());
            this.shipmentsRepository.save(shipment);
            log.info("Updated shipment {} with order id {}", shipment, orderKMO.orderId());
        }
    }

    public void updatePendingShipments() {
        final List<Shipment> shipmentsPendingDelivery = this.shipmentsRepository.findShipmentByStatus(
                OrderStatus.PENDING_DELIVERY);

        final var filteredList = shipmentsPendingDelivery.stream()
                .filter(shipment -> shipment.getOrderIds().size() >= ORDER_COUNT_CUTOFF)
                .peek(shipment -> shipment.setStatus(OrderStatus.DELIVERY_IN_PROGRESS))
                .toList();

        if (!filteredList.isEmpty()) {
            this.shipmentsRepository.saveAll(filteredList);
            log.info("Updated shipment status for {} deliveries to {}", filteredList.size(),
                    OrderStatus.DELIVERY_IN_PROGRESS);
            this.sendOrderStatusUpdateRequest(filteredList, OrderStatus.DELIVERY_IN_PROGRESS);
        } else {
            log.info("No pending deliveries meet the criteria for updating their status");
        }
    }

    public void updateInProgressShipments() {
        //TODO: Implement a type of audit trail for tracking the status updates, similar to rtgs
        final List<Shipment> shipmentsPendingDelivery = this.shipmentsRepository.findShipmentByStatus(
                OrderStatus.DELIVERY_IN_PROGRESS);

        //If a shipment has been in DELIVERY_IN_PROGRESS for more than x minutes, set it to delivered
        final var filteredList = shipmentsPendingDelivery.stream()
                .filter(shipment -> {
                    final Instant lastUpdatedInstant = shipment.getLastUpdated()
                            .atZone(ZoneId.systemDefault()).toInstant();
                    final Instant currentInstant = LocalDateTime.now()
                            .atZone(ZoneId.systemDefault()).toInstant();
                    final Duration duration = Duration.between(lastUpdatedInstant, currentInstant);
                    return duration.toMinutes() > SHIPMENT_UPDATE_CUTOFF_MINS;
                })
                .peek(shipment -> shipment.setStatus(OrderStatus.DELIVERED))
                .toList();

        if (!filteredList.isEmpty()) {
            this.shipmentsRepository.saveAll(filteredList);
            log.info("Updated shipment status for {} deliveries to {}", filteredList.size(),
                    OrderStatus.DELIVERED);
            this.sendOrderStatusUpdateRequest(filteredList, OrderStatus.DELIVERED);
        } else {
            log.info("No in progress deliveries meet the criteria for updating their status");
        }
    }

    /**
     * Update the order service about new status
     */
    private void sendOrderStatusUpdateRequest(final List<Shipment> shipments,
            final OrderStatus orderStatus) {
        //TODO: This is very inefficient. Instead of sending multiple requests, add an endpoint in the order service to process all requests
        //TODO: This can also be multithreaded so each thread doesn't need to block until the previous request completes

        shipments.forEach(shipment -> {
            shipment.getOrderIds().forEach(orderId -> {
                try {
                    this.orderStatusUpdatesService.updateOrderStatus(orderId, orderStatus);
                } catch (final Exception e) {
                    log.error("Failed to update order status in the order-service: {}",
                            e.getMessage(), e);
//                    throw new RuntimeException(e);
                }
            });
        });
    }
}
