package com.shop.generic.shippingservice.kafka;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.shop.generic.common.CommonKafkaConsumerAutoConfiguration;
import com.shop.generic.common.entities.Order;
import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.common.kmos.OrderKMO;
import com.shop.generic.common.rest.request.RestTemplateUtil;
import com.shop.generic.shippingservice.entities.Shipment;
import com.shop.generic.shippingservice.repositories.ShipmentsRepository;
import com.shop.generic.shippingservice.services.OrderStatusUpdatesService;
import com.shop.generic.shippingservice.services.ShippingService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * {@link CommonKafkaConsumerAutoConfiguration} is not actually necessary here as the properties in
 * the application.yaml are sufficient for spring to auto creat all the necessary beans. But it is
 * still useful to define your own in case you need any bespoke configurations
 */
@SpringBootTest(classes = {OrderListener.class, CommonKafkaConsumerAutoConfiguration.class,
        ShippingService.class, OrderStatusUpdatesService.class, RestTemplateUtil.class,
        RestTemplate.class, KafkaAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "com.shop.generic.shippingservice.repositories")
@EntityScan(basePackages = "com.shop.generic.shippingservice.entities")
@AutoConfigureDataJpa
@EmbeddedKafka(partitions = 1, topics = {"orders"})
@Slf4j
class OrderListenerTest {

    private KafkaTemplate<String, OrderKMO> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ShipmentsRepository shipmentsRepository;

    @BeforeEach
    void setup() {

        final Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        final DefaultKafkaProducerFactory<String, OrderKMO> producerFactory = new DefaultKafkaProducerFactory<>(
                producerProps);

        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @DisplayName("Process kafka message, transform it and persist it as a shipment to the database")
    @Test
    void should_ProcessOrder() throws InterruptedException {
        // Given
        final Order order = new Order(UUID.randomUUID(), new BigDecimal("100.00"), "123,456",
                OrderStatus.CREATED, "London", LocalDateTime.now());
        final OrderKMO orderKMO = new OrderKMO(order);

        //Required to give kafka time to register the consumer
        TimeUnit.SECONDS.sleep(1);

        // When
        kafkaTemplate.send("orders", orderKMO);
        kafkaTemplate.flush();
        log.info("Sent order to topic...");

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(10, SECONDS)
                .untilAsserted(() -> {
                    final Optional<Shipment> optionalProduct = shipmentsRepository.findShipmentByShipmentId(
                            1);
                    assertThat(optionalProduct).isPresent();
                    final Shipment shipment = optionalProduct.get();
                    assertThat(shipment.getOrderIds().getFirst()).isEqualTo(order.getOrderId());
                    assertThat(shipment.getCity()).isEqualTo(order.getCity());
                    assertThat(shipment.getStatus()).isEqualTo(OrderStatus.PENDING_DELIVERY);
                    assertThat(shipment.getLastUpdated()).isNotNull();
                });
    }
}