package com.shop.generic.shippingservice.services;

import com.shop.generic.common.dtos.OrderStatusDTO;
import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.common.rest.request.RestTemplateUtil;
import com.shop.generic.common.rest.response.RestApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusUpdatesService {

    private static final String UPDATE_ORDER_URI = "/orders/order";

    @Value("${services.order-service.url}")
    private String orderServiceUrl;

    private final RestTemplateUtil restTemplateUtil;

    public RestApiResponse<OrderStatusDTO> updateOrderStatus(final UUID orderId,
            final OrderStatus orderStatus) throws Exception {
        final String url = UriComponentsBuilder.fromHttpUrl(orderServiceUrl)
                .path(UPDATE_ORDER_URI)
                .pathSegment(orderId.toString(), orderStatus.name())
                .build()
                .toUriString();

        return restTemplateUtil.putForObject(
                url,
                null,
                new ParameterizedTypeReference<>() {
                });
    }
}
