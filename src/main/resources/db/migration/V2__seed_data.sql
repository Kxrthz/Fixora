-- Seed Initial Demo Data

INSERT INTO users (id, name, email, password, role, phone, address)
VALUES 
(1, 'John Customer', 'customer@fixora.com', '$2a$10$e8W/2nI36p/0tW7yH2bEGe9rTz7R1Q9KxYn5m8bV4lX8nJ1k2m3yS', 'CUSTOMER', '+1234567890', '123 Main St'),
(2, 'Alex Provider', 'provider@fixora.com', '$2a$10$e8W/2nI36p/0tW7yH2bEGe9rTz7R1Q9KxYn5m8bV4lX8nJ1k2m3yS', 'PROVIDER', '+1987654321', '456 Service Ave')
ON CONFLICT (id) DO NOTHING;

INSERT INTO services (id, title, category, description, price, image_url)
VALUES 
(1, 'AC Repair & Servicing', 'Appliance', 'Complete cleaning and diagnostic checkup for household AC units.', 49.99, 'https://images.unsplash.com/photo-1581092918056-0c4c3acd3789'),
(2, 'Full House Plumbing Check', 'Plumbing', 'Fix leaks, check water pressure, and repair pipes.', 75.00, 'https://images.unsplash.com/photo-1607472586893-edb57bdc0e39')
ON CONFLICT (id) DO NOTHING;

INSERT INTO providers (id, user_id, business_name, category, bio, rating, hourly_rate, experience_years)
VALUES 
(1, 2, 'Alex Plumbing & AC Repairs', 'Home Maintenance', 'Over 8 years of certified home service experience.', 4.85, 45.00, 8)
ON CONFLICT (id) DO NOTHING;

-- Reset sequence explicitly to match manually seeded IDs
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1)) FROM users;
SELECT setval(pg_get_serial_sequence('services', 'id'), COALESCE(MAX(id), 1)) FROM services;
SELECT setval(pg_get_serial_sequence('providers', 'id'), COALESCE(MAX(id), 1)) FROM providers;