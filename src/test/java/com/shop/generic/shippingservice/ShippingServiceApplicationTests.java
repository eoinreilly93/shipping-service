package com.shop.generic.shippingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * @DirtiesContext is required here, otherwise the context will be shared with other tests
 * (specifically OrderListener at time of writing), and that test will fail because the Shipments
 * table will already exist.
 */
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ShippingServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
