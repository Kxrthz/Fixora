INSERT INTO services (name,category,description,starting_price,icon) VALUES
('Electrical repair','Electrical','Safe, certified help for outlets, switches, fixtures, and diagnostics.',499.00,'ϟ'),
('Deep home cleaning','Cleaning','A detailed, calm reset for the rooms you live in.',899.00,'✦'),
('Plumbing repair','Plumbing','Fast help for leaks, clogs, fittings, and common repairs.',599.00,'◌'),
('AC service','Appliance','Professional cooling maintenance and repair.',699.00,'❄'),
('Carpentry','Carpentry','Furniture repair, installation, and custom small fixes.',649.00,'⌘');
INSERT INTO users(name,email,password_hash,role) VALUES
('Arjun Mehta','arjun@fixora.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','PROVIDER'),
('Priya Nair','priya@fixora.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','PROVIDER'),
('Fixora Admin','admin@fixora.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','ADMIN');
INSERT INTO provider_profiles(id,specialty,rating,completed_jobs,hourly_rate,city,verified) VALUES
(1,'Certified electrician',4.90,318,650.00,'Bengaluru',TRUE),
(2,'Home cleaning specialist',4.80,241,500.00,'Bengaluru',TRUE);
