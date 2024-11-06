package com.shop.generic.shippingservice.repositories;

import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.shippingservice.entities.Shipment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentsRepository extends ListCrudRepository<Shipment, Integer> {

    List<Shipment> findAllByCityAndStatusIs(final String city, OrderStatus status);

    List<Shipment> findShipmentByStatus(final OrderStatus status);

    Optional<Shipment> findShipmentByShipmentId(final Integer id);
}
