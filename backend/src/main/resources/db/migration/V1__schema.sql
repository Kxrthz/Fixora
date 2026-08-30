CREATE TABLE users (
  id BIGINT PRIMARY KEY , name VARCHAR(100) NOT NULL, email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL, role VARCHAR(20) NOT NULL, enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), INDEX idx_users_role(role)
);
CREATE TABLE services (
  id BIGINT PRIMARY KEY , name VARCHAR(100) NOT NULL, category VARCHAR(60) NOT NULL, description VARCHAR(600) NOT NULL,
  starting_price DECIMAL(10,2) NOT NULL, icon VARCHAR(12) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE, INDEX idx_services_category(category)
);
CREATE TABLE provider_profiles (
  id BIGINT PRIMARY KEY, specialty VARCHAR(120) NOT NULL, rating DECIMAL(3,2) NOT NULL DEFAULT 5.00, completed_jobs INT NOT NULL DEFAULT 0,
  hourly_rate DECIMAL(10,2) NOT NULL, city VARCHAR(80) NOT NULL, verified BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_provider_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE bookings (
  id BIGINT PRIMARY KEY , customer_id BIGINT NOT NULL, provider_id BIGINT NOT NULL, service_id BIGINT NOT NULL,
  address VARCHAR(300) NOT NULL, notes VARCHAR(1200), scheduled_at TIMESTAMP NOT NULL, status VARCHAR(20) NOT NULL, total DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_booking_customer FOREIGN KEY(customer_id) REFERENCES users(id), CONSTRAINT fk_booking_provider FOREIGN KEY(provider_id) REFERENCES users(id), CONSTRAINT fk_booking_service FOREIGN KEY(service_id) REFERENCES services(id),
  INDEX idx_bookings_customer(customer_id, scheduled_at), INDEX idx_bookings_provider(provider_id, scheduled_at)
);
CREATE TABLE chat_rooms (id BIGINT PRIMARY KEY , booking_id BIGINT NOT NULL UNIQUE, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), CONSTRAINT fk_room_booking FOREIGN KEY(booking_id) REFERENCES bookings(id) ON DELETE CASCADE);
CREATE TABLE messages (id BIGINT PRIMARY KEY , room_id BIGINT NOT NULL, sender_id BIGINT NOT NULL, body VARCHAR(2000) NOT NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), CONSTRAINT fk_message_room FOREIGN KEY(room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE, CONSTRAINT fk_message_sender FOREIGN KEY(sender_id) REFERENCES users(id), INDEX idx_messages_room(room_id, created_at));
CREATE TABLE notifications (id BIGINT PRIMARY KEY , user_id BIGINT NOT NULL, title VARCHAR(120) NOT NULL, body VARCHAR(600) NOT NULL, `read` BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), CONSTRAINT fk_notification_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, INDEX idx_notifications_user(user_id, created_at));
CREATE TABLE payments (id BIGINT PRIMARY KEY , booking_id BIGINT NOT NULL UNIQUE, method VARCHAR(30) NOT NULL, status VARCHAR(20) NOT NULL, amount DECIMAL(10,2) NOT NULL, reference VARCHAR(64) NOT NULL UNIQUE, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), CONSTRAINT fk_payment_booking FOREIGN KEY(booking_id) REFERENCES bookings(id), INDEX idx_payments_status(status));

