CREATE TABLE payments (
     id             BIGSERIAL PRIMARY KEY,
     price          FLOAT8 NOT NULL,
     price_modifier FLOAT4 NOT NULL,
     points         INT4,
     payment_method VARCHAR(255) NOT NULL,
     date_time      TIMESTAMP,
     created_at     TIMESTAMP
);