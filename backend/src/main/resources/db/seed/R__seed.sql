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



-- ============================================================
-- DODATKOWE SAMOCHODY
-- ============================================================

-- Marcinek owns 4 cars
INSERT INTO cars (owner_id, brand, model, model_year, vin, seat_number, gearbox, horse_power, avg_liters_per_hundred_km, addtional_information) VALUES
  ((SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'), 'Volkswagen', 'Golf',       2021, 'WVWZZZ1KZAM123010', 5, 'MANUAL',    150, 6.80, 'Stan idealny, serwisowany w ASO, opony zimowe w komplecie.'),
  ((SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'), 'Audi',       'A4',         2020, 'WAUZZZ8K9BA123011', 5, 'AUTOMATIC', 190, 7.50, 'Full opcja, skórzana tapicerka, nawigacja, kamera cofania.'),
  ((SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'), 'Skoda',      'Octavia',    2019, 'TMBJJ7NE0J0123012', 5, 'MANUAL',    116, 5.90, 'Ekonomiczny diesel, idealy na długie trasy, hak holowniczy.'),
  ((SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'), 'Mercedes',   'C 200',      2023, 'WDD2050011R123013', 5, 'AUTOMATIC', 204, 8.10, 'Rok stary, gwarancja fabryczna, pakiet AMG Line, dach panoramiczny.');

-- Dodatkowe samochody alice i bob (dla urozmaicenia rezerwacji)
INSERT INTO cars (owner_id, brand, model, model_year, vin, seat_number, gearbox, horse_power, avg_liters_per_hundred_km, addtional_information) VALUES
  ((SELECT id FROM users WHERE email = 'alice@dev.local'),   'Honda',   'Civic',   2021, '2HGFC2F50MH123014', 5, 'MANUAL',    158, 6.50, 'Bardzo oszczędny, bezwypadkowy, jeden właściciel.'),
  ((SELECT id FROM users WHERE email = 'bob@dev.local'),     'Peugeot', '308',     2020, 'VF3LBHZTEHS123015', 5, 'AUTOMATIC', 130, 6.20, 'Zadbany, klimatyzacja automatyczna, podgrzewane fotele.');

-- charlie owns 1 car
INSERT INTO cars (owner_id, brand, model, model_year, vin, seat_number, gearbox, horse_power, avg_liters_per_hundred_km, addtional_information) VALUES
  ((SELECT id FROM users WHERE email = 'charlie@dev.local'), 'Renault', 'Megane',  2018, 'VF1LM000555123016', 5, 'MANUAL',    115, 7.00, 'Używany codziennie, regularny serwis, dwie pary opon.');


-- ============================================================
-- DODATKOWE LISTINGS
-- ============================================================

-- Marcinek — 4 ogłoszenia (po jednym na każde auto)
INSERT INTO listings (car_id, user_id, price, status, title, localization, body, created_at, last_active) VALUES
  (
    (SELECT id FROM cars WHERE vin = 'WVWZZZ1KZAM123010'),
    (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
    130, 'ACTIVE', 'VW Golf — niezawodny i oszczędny', 'Kraków',
    'Zadbany Golf w idealnym stanie. Regularnie serwisowany w ASO, nigdy nie był w kolizji. Ideał na miasto i trasy.',
    NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day'
  ),
  (
    (SELECT id FROM cars WHERE vin = 'WAUZZZ8K9BA123011'),
    (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
    220, 'ACTIVE', 'Audi A4 — komfort klasy premium', 'Kraków',
    'Automatyczna A4 w pełnym wyposażeniu. Skóra, nawigacja, kamera 360°. Idealna na dłuższe wyjazdy służbowe i weekendowe.',
    NOW() - INTERVAL '4 days', NOW() - INTERVAL '1 day'
  ),
  (
    (SELECT id FROM cars WHERE vin = 'TMBJJ7NE0J0123012'),
    (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
    95, 'ACTIVE', 'Skoda Octavia diesel — tania w eksploatacji', 'Kraków',
    'Diesel 1.6 TDI — spalanie ~5.9L/100km. Hak holowniczy, duży bagażnik. Polecam na wakacje i długie trasy.',
    NOW() - INTERVAL '3 days', NOW()
  ),
  (
    (SELECT id FROM cars WHERE vin = 'WDD2050011R123013'),
    (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
    350, 'ACTIVE', 'Mercedes C 200 — luksus bez kompromisów', 'Kraków',
    'Prawie nowy Mercedes z 2023 roku. Pakiet AMG Line, dach panoramiczny, asystent parkowania. Dla wymagających.',
    NOW() - INTERVAL '2 days', NOW()
  );

-- Alice — dodatkowe listing
INSERT INTO listings (car_id, user_id, price, status, title, localization, body, created_at, last_active) VALUES
  (
    (SELECT id FROM cars WHERE vin = '2HGFC2F50MH123014'),
    (SELECT id FROM users WHERE email = 'alice@dev.local'),
    110, 'ACTIVE', 'Honda Civic — nowoczesna i dynamiczna', 'Warszawa',
    'Bezwypadkowa Civic z 2021r. Jeden właściciel, pełna historia serwisowa. Bardzo niskie spalanie.',
    NOW() - INTERVAL '6 days', NOW() - INTERVAL '2 days'
  );

-- Bob — dodatkowe listing
INSERT INTO listings (car_id, user_id, price, status, title, localization, body, created_at, last_active) VALUES
  (
    (SELECT id FROM cars WHERE vin = 'VF3LBHZTEHS123015'),
    (SELECT id FROM users WHERE email = 'bob@dev.local'),
    100, 'ACTIVE', 'Peugeot 308 — komfortowy automat', 'Gdańsk',
    'Automatyczny 308 w świetnym stanie. Klimatyzacja automatyczna, podgrzewane fotele, czujniki parkowania.',
    NOW() - INTERVAL '7 days', NOW() - INTERVAL '3 days'
  );

-- Charlie — listing swojego auta
INSERT INTO listings (car_id, user_id, price, status, title, localization, body, created_at, last_active) VALUES
  (
    (SELECT id FROM cars WHERE vin = 'VF1LM000555123016'),
    (SELECT id FROM users WHERE email = 'charlie@dev.local'),
    80, 'ACTIVE', 'Renault Megane — solidny i tani', 'Wrocław',
    'Regularnie serwisowany Megane. Dwie pary opon (letnia + zimowa). Dobry wybór dla oszczędnych.',
    NOW() - INTERVAL '8 days', NOW() - INTERVAL '4 days'
  );


-- ============================================================
-- DODATKOWE REZERWACJE
-- ============================================================

-- alice wynajmuje VW Golfa od Marcinka (CONFIRMED, nadchodząca)
INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
  (
    (SELECT id FROM listings WHERE title = 'VW Golf — niezawodny i oszczędny'),
    (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
    (SELECT id FROM users WHERE email = 'alice@dev.local'),
    'CONFIRMED',
    NOW() + INTERVAL '5 days',
    NOW() + INTERVAL '8 days',
    NOW() - INTERVAL '1 day'
  );

-- bob wynajmuje Audi A4 od Marcinka (PENDING)
INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
  (
    (SELECT id FROM listings WHERE title = 'Audi A4 — komfort klasy premium'),
    (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
    (SELECT id FROM users WHERE email = 'bob@dev.local'),
    'PENDING',
    NOW() + INTERVAL '12 days',
    NOW() + INTERVAL '15 days',
    NOW()
  );

-- charlie wynajmuje Mercedesa od Marcinka (COMPLETED, przeszłość)
INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
  (
    (SELECT id FROM listings WHERE title = 'Mercedes C 200 — luksus bez kompromisów'),
    (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
    (SELECT id FROM users WHERE email = 'charlie@dev.local'),
    'COMPLETED',
    NOW() - INTERVAL '20 days',
    NOW() - INTERVAL '17 days',
    NOW() - INTERVAL '25 days'
  );

-- alice wynajmuje Skodę od Marcinka (CANCELLED)
-- INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
--   (
--     (SELECT id FROM listings WHERE title = 'Skoda Octavia diesel — tania w eksploatacji'),
--     (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
--     (SELECT id FROM users WHERE email = 'alice@dev.local'),
--     'CANCELLED',
--     NOW() - INTERVAL '10 days',
--     NOW() - INTERVAL '8 days',
--     NOW() - INTERVAL '15 days'
--   );

-- charlie wynajmuje Hondę Civic od alice (RENTER_CONFIRMED)
-- INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
--   (
--     (SELECT id FROM listings WHERE title = 'Honda Civic — nowoczesna i dynamiczna'),
--     (SELECT id FROM users WHERE email = 'alice@dev.local'),
--     (SELECT id FROM users WHERE email = 'charlie@dev.local'),
--     'RENTER_CONFIRMED',
--     NOW() + INTERVAL '2 days',
--     NOW() + INTERVAL '5 days',
--     NOW() - INTERVAL '2 days'
--   );

-- Marcinek wynajmuje Forda Focus od boba (COMPLETED, przeszłość)
INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
  (
    (SELECT id FROM listings WHERE title = 'Ford Focus — budget friendly'),
    (SELECT id FROM users WHERE email = 'bob@dev.local'),
    (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
    'COMPLETED',
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '28 days',
    NOW() - INTERVAL '35 days'
  );

-- bob wynajmuje Renault Megane od charlie (ACTIVE — trwa teraz)
INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
  (
    (SELECT id FROM listings WHERE title = 'Renault Megane — solidny i tani'),
    (SELECT id FROM users WHERE email = 'charlie@dev.local'),
    (SELECT id FROM users WHERE email = 'bob@dev.local'),
    'ACTIVE',
    NOW() - INTERVAL '1 day',
    NOW() + INTERVAL '2 days',
    NOW() - INTERVAL '5 days'
  );

-- Marcinek wynajmuje Peugeota od boba (PENDING)
INSERT INTO reservations (listing_id, owner_id, renter_id, status, date_start, date_end, created_at) VALUES
  (
    (SELECT id FROM listings WHERE title = 'Peugeot 308 — komfortowy automat'),
    (SELECT id FROM users WHERE email = 'bob@dev.local'),
    (SELECT id FROM users WHERE email = 'marcin.lukasz@student.pk.edu.pl'),
    'PENDING',
    NOW() + INTERVAL '20 days',
    NOW() + INTERVAL '23 days',
    NOW()
  );