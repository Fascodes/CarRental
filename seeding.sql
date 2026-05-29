-- Users (passwords: bcrypt of 'password')
INSERT INTO users (username, email, password, role) VALUES
  ('alice',   'alice@dev.local',   '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2JB0MsX1ZCDsGMFRTmI4P6VVHe1sDm', 'USER'),
  ('bob',     'bob@dev.local',     '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2JB0MsX1ZCDsGMFRTmI4P6VVHe1sDm', 'USER'),
  ('charlie', 'charlie@dev.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2JB0MsX1ZCDsGMFRTmI4P6VVHe1sDm', 'USER'),
  ('admin',   'admin@dev.local',   '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2JB0MsX1ZCDsGMFRTmI4P6VVHe1sDm', 'ADMIN'),
  ('Marcinek', 'marcin.lukasz@student.pk.edu.pl', '$2a$10$n8786D3nL1nliXz8Z/HzdumlMG.pwgdTHUgGbYSzQFPV1t5KTmCt6', 'USER');

-- Cars (alice owns 2, bob owns 1)
INSERT INTO cars (owner_id, brand, model, model_year, vin, seat_number, gearbox, horse_power, avg_liters_per_hundred_km) VALUES
  ((SELECT id FROM users WHERE email = 'alice@dev.local'),   'Toyota',     'Corolla',  2020, 'JT2BF22K1W0123001', 5, 'MANUAL',    122, 7.20),
  ((SELECT id FROM users WHERE email = 'alice@dev.local'),   'BMW',        '3 Series', 2022, 'WBA5A5C5XFD123002', 5, 'AUTOMATIC', 184, 8.50),
  ((SELECT id FROM users WHERE email = 'bob@dev.local'),     'Ford',       'Focus',    2019, '1FADP3F24JL123003', 5, 'MANUAL',    115, 6.90);

-- Listings (alice lists both cars, bob lists his car)
INSERT INTO listings (car_id, user_id, price, status, title, localization, body, created_at, last_active) VALUES
  (
    (SELECT id FROM cars WHERE vin = 'JT2BF22K1W0123001'),
    (SELECT id FROM users WHERE email = 'alice@dev.local'),
    120, 'ACTIVE', 'Toyota Corolla — reliable city car', 'Warszawa',
    'Well-maintained 2020 Corolla. Perfect for city and highway. Non-smoker, garage kept.',
    NOW(), NOW()
  ),
  (
    (SELECT id FROM cars WHERE vin = 'WBA5A5C5XFD123002'),
    (SELECT id FROM users WHERE email = 'alice@dev.local'),
    250, 'ACTIVE', 'BMW 3 Series — premium comfort', 'Krakow',
    'Automatic 2022 BMW. Full leather interior, heated seats, parking sensors.',
    NOW(), NOW()
  ),
  (
    (SELECT id FROM cars WHERE vin = '1FADP3F24JL123003'),
    (SELECT id FROM users WHERE email = 'bob@dev.local'),
    90, 'ACTIVE', 'Ford Focus — budget friendly', 'Gdansk',
    'Solid 2019 Focus manual. Great fuel economy, very reliable.',
    NOW(), NOW()
  );

-- Reservations
-- charlie rents alice's Corolla (CONFIRMED, upcoming)
INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
  (
    (SELECT id FROM listings WHERE title = 'Toyota Corolla — reliable city car'),
    (SELECT id FROM users WHERE email = 'alice@dev.local'),
    (SELECT id FROM users WHERE email = 'charlie@dev.local'),
    'CONFIRMED',
    NOW() + INTERVAL '3 days',
    NOW() + INTERVAL '7 days',
    NOW()
  );

-- bob rents alice's BMW (PENDING)
INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
  (
    (SELECT id FROM listings WHERE title = 'BMW 3 Series — premium comfort'),
    (SELECT id FROM users WHERE email = 'alice@dev.local'),
    (SELECT id FROM users WHERE email = 'bob@dev.local'),
    'PENDING',
    NOW() + INTERVAL '10 days',
    NOW() + INTERVAL '14 days',
    NOW()
  );

-- charlie rents bob's Focus (COMPLETED, past)
INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
  (
    (SELECT id FROM listings WHERE title = 'Ford Focus — budget friendly'),
    (SELECT id FROM users WHERE email = 'bob@dev.local'),
    (SELECT id FROM users WHERE email = 'charlie@dev.local'),
    'COMPLETED',
    NOW() - INTERVAL '14 days',
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '20 days'
  );
