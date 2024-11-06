package com.shop.generic.shippingservice.entities;

import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.shippingservice.repositories.UUIDListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHIPMENT_ID")
    private Integer shipmentId;

    @Convert(converter = UUIDListConverter.class)
    @Column(name = "ORDER_IDS", nullable = false)
    @NonNull
    private List<UUID> orderIds;

    @Column(name = "CITY", nullable = false)
    @NonNull
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    @NonNull
    private OrderStatus status;

    @NonNull
    @Column(name = "CREATED", nullable = false)
    private LocalDateTime timestamp;

    @NonNull
    @Column(name = "LAST_UPDATED", nullable = false)
    private LocalDateTime lastUpdated;
}
