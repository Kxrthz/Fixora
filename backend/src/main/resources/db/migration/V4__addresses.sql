CREATE TABLE addresses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  label VARCHAR(40) NOT NULL,
  line1 VARCHAR(120) NOT NULL,
  line2 VARCHAR(120),
  city VARCHAR(80) NOT NULL,
  postal_code VARCHAR(20) NOT NULL,
  landmark VARCHAR(120),
  default_address BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_address_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_addresses_user(user_id, default_address)
);
