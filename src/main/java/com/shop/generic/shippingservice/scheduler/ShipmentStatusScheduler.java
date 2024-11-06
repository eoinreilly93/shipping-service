package com.shop.generic.shippingservice.scheduler;

import com.shop.generic.shippingservice.services.ShippingService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentStatusScheduler {

    private final ShippingService shippingService;

    /**
     * Updates the pending shipments
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void updatePendingShipmentStatus() {
        log.info("Executing updatePendingShipmentStatus scheduled task");
        this.shippingService.updatePendingShipments();
    }

    /**
     * Updates the out of delivery shipments
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void updateInProgressShipmentStatus() {
        log.info("Executing updateInProgressShipmentStatus scheduled task");
        this.shippingService.updateInProgressShipments();
    }

}
