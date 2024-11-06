package com.shop.generic.shippingservice;

import com.shop.generic.common.CommonKafkaProducerAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableKafka
@EnableScheduling
@EnableWebSecurity //Required as this is the only app that doesn't use a web server
@SpringBootApplication(exclude = {CommonKafkaProducerAutoConfiguration.class})
public class ShippingServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ShippingServiceApplication.class, args);
    }
}
