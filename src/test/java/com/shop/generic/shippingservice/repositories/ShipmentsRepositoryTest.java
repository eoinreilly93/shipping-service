package com.shop.generic.shippingservice.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.shippingservice.entities.Shipment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class ShipmentsRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ShipmentsRepository shipmentsRepository;

    @Test
    @DisplayName("Repository should save a shipment")
    public void should_saveAShipment() {
        // Given
        final UUID orderId = UUID.randomUUID();
        final Shipment shipment = new Shipment();
        shipment.setCity("London");
        shipment.setOrderIds(List.of(orderId));
        shipment.setStatus(OrderStatus.CREATED);
        shipment.setLastUpdated(LocalDateTime.now());
        shipment.setTimestamp(LocalDateTime.now());

        // When
        this.shipmentsRepository.save(shipment);

        // Then
        assertThat(shipment.getShipmentId()).isNotNull();
        assertThat(this.shipmentsRepository.findById(shipment.getShipmentId())).isNotNull();
    }

    @Test
    @DisplayName("Repository should retrieve a shipment by its ID")
    public void should_retrieveShipment_byShipmentId() {
        // Given
        final Shipment shipment = new Shipment();
        shipment.setCity("London");
        shipment.setOrderIds(List.of(UUID.randomUUID()));
        shipment.setStatus(OrderStatus.CREATED);
        shipment.setLastUpdated(LocalDateTime.now());
        shipment.setTimestamp(LocalDateTime.now());
        this.testEntityManager.persist(shipment);

        // When
        final Optional<Shipment> persistedShipment = this.shipmentsRepository.findShipmentByShipmentId(
                1);

        // Then
        assertThat(persistedShipment).isPresent();
        assertEquals(persistedShipment.get().getCity(), "London");
        assertEquals(persistedShipment.get().getCity(), "London");
    }

    @Test
    @DisplayName("Repository should find shipments by city and status")
    public void should_findShipments_byCityAndStatus() {
        // Given
        final Shipment shipment1 = new Shipment();
        shipment1.setCity("London");
        shipment1.setOrderIds(List.of(UUID.randomUUID()));
        shipment1.setStatus(OrderStatus.PENDING_DELIVERY);
        shipment1.setLastUpdated(LocalDateTime.now());
        shipment1.setTimestamp(LocalDateTime.now());

        final Shipment shipment2 = new Shipment();
        shipment2.setCity("London");
        shipment2.setOrderIds(List.of(UUID.randomUUID()));
        shipment2.setStatus(OrderStatus.PENDING_DELIVERY);
        shipment2.setLastUpdated(LocalDateTime.now());
        shipment2.setTimestamp(LocalDateTime.now());

        final Shipment shipment3 = new Shipment();
        shipment3.setCity("Galway");
        shipment3.setOrderIds(List.of(UUID.randomUUID()));
        shipment3.setStatus(OrderStatus.PENDING_DELIVERY);
        shipment3.setLastUpdated(LocalDateTime.now());
        shipment3.setTimestamp(LocalDateTime.now());

        this.testEntityManager.persist(shipment1);
        this.testEntityManager.persist(shipment2);
        this.testEntityManager.persist(shipment3);

        // When
        final List<Shipment> shipments = this.shipmentsRepository.findAllByCityAndStatusIs(
                "London", OrderStatus.PENDING_DELIVERY);

        // Then
        assertThat(shipments).isNotEmpty();
        assertEquals(2, shipments.size());
        shipments.forEach(shipment -> {
            assertThat(shipment.getCity()).isEqualTo("London");
        });
    }

    @Test
    @DisplayName("Repository should find shipments by status")
    public void should_findShipments_byStatus() {
        // Given
        final Shipment shipment1 = new Shipment();
        shipment1.setCity("London");
        shipment1.setOrderIds(List.of(UUID.randomUUID()));
        shipment1.setStatus(OrderStatus.DELIVERY_IN_PROGRESS);
        shipment1.setLastUpdated(LocalDateTime.now());
        shipment1.setTimestamp(LocalDateTime.now());

        final Shipment shipment2 = new Shipment();
        shipment2.setCity("London");
        shipment2.setOrderIds(List.of(UUID.randomUUID()));
        shipment2.setStatus(OrderStatus.DELIVERED);
        shipment2.setLastUpdated(LocalDateTime.now());
        shipment2.setTimestamp(LocalDateTime.now());

        this.testEntityManager.persist(shipment1);
        this.testEntityManager.persist(shipment2);

        // When
        final List<Shipment> shipments = this.shipmentsRepository.findShipmentByStatus(
                OrderStatus.DELIVERY_IN_PROGRESS);

        // Then
        assertThat(shipments).isNotEmpty();
        assertEquals(1, shipments.size());
        assertThat(shipments.getFirst().getStatus()).isEqualTo(OrderStatus.DELIVERY_IN_PROGRESS);
    }
}