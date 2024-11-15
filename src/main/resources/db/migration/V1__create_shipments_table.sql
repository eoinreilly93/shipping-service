CREATE SEQUENCE IF NOT EXISTS shipment_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE shipments
(
    shipment_id BIGINT NOT NULL,
    order_ids    TEXT         NOT NULL,
    city         VARCHAR(255) NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    created      TIMESTAMP    NOT NULL,
    last_updated TIMESTAMP    NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (shipment_id)
);