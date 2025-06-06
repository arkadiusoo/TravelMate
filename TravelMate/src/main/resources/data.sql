-- ==========================================
-- 1. Tworzenie wycieczek (trip)
-- ==========================================
INSERT INTO trip (id, name, start_date, end_date) VALUES
  -- Wycieczka w Tatry
  ('11111111-1111-1111-1111-111111111111', 'Wycieczka w Tatry',    '2025-06-01', '2025-06-05'),
  -- Wakacje nad morzem
  ('22222222-2222-2222-2222-222222222222', 'Wakacje nad morzem', '2025-07-10', '2025-07-15');

-- ==========================================
-- 2. Tworzenie uczestników (participants)
--    Każdy uczestnik wskazuje na istniejące trip_id.
-- ==========================================
INSERT INTO participants (
    id, user_id, trip_id, email, role, status, created_at, updated_at
) VALUES
  -- Uczestnicy wycieczki "Wycieczka w Tatry"
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '10000000-0000-0000-0000-000000000001',
   '11111111-1111-1111-1111-111111111111', 'alice@example.com', 'Leader',   'pending',
   '2025-05-01 10:00:00', '2025-05-01 10:00:00'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '10000000-0000-0000-0000-000000000002',
   '11111111-1111-1111-1111-111111111111', 'bob@example.com',   'Member',   'pending',
   '2025-05-01 10:00:00', '2025-05-01 10:00:00'),

  -- Uczestnicy wycieczki "Wakacje nad morzem"
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '10000000-0000-0000-0000-000000000003',
   '22222222-2222-2222-2222-222222222222', 'carol@example.com', 'Leader',   'pending',
   '2025-05-10 14:30:00', '2025-05-10 14:30:00'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', '10000000-0000-0000-0000-000000000004',
   '22222222-2222-2222-2222-222222222222', 'dave@example.com',  'Member',   'pending',
   '2025-05-10 14:30:00', '2025-05-10 14:30:00');

-- ==========================================
-- 3. Dodanie punktów trasy (point)
--    Każdy punkt wskazuje na istniejące trip_id, daty mieszczą się w ramach danej wycieczki.
-- ==========================================
INSERT INTO point (
    id, trip_id, date, latitude, longitude, title, description
) VALUES
  -- Punkty dla "Wycieczka w Tatry"
  (1, '11111111-1111-1111-1111-111111111111', '2025-06-02', 49.2000, 19.9500,
     'Hala Kondratowa', 'Schronisko w Hali Kondratowej'),
  (2, '11111111-1111-1111-1111-111111111111', '2025-06-03', 49.2200, 19.9800,
     'Kasprowy Wierch',   'Wejście na Kasprowy Wierch'),

  -- Punkty dla "Wakacje nad morzem"
  (3, '22222222-2222-2222-2222-222222222222', '2025-07-11', 54.3500, 18.6800,
     'Sopot Beach',      'Relaks na plaży w Sopocie'),
  (4, '22222222-2222-2222-2222-222222222222', '2025-07-13', 54.4400, 18.5600,
     'Stawa Młyny',      'Odwiedziny przy Stawie Młyny w Gdańsku');

-- ==========================================
-- 4. Dodanie wydatków (expenses)
--    Każdy wydatek powiązany jest z trip_id oraz payer_id będącym jednym z uczestników.
-- ==========================================
INSERT INTO expenses (
    id, trip_id, payer_id, amount, date, description, category, created_at, updated_at
) VALUES
  -- Wydatki dla "Wycieczka w Tatry"
  ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 200.00, '2025-06-02',
   'Nocleg w schronisku',       'Accommodation',
   '2025-06-02 12:00:00', '2025-06-02 12:00:00'),

  ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111',
   'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 100.00, '2025-06-03',
   'Wynajem sprzętu górskiego', 'Equipment',
   '2025-06-03 15:30:00', '2025-06-03 15:30:00'),

  -- Wydatki dla "Wakacje nad morzem"
  ('55555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222',
   'cccccccc-cccc-cccc-cccc-cccccccccccc', 300.00, '2025-07-11',
   'Apartament nad morzem',      'Accommodation',
   '2025-07-11 11:00:00', '2025-07-11 11:00:00'),

  ('66666666-6666-6666-6666-666666666666', '22222222-2222-2222-2222-222222222222',
   'dddddddd-dddd-dddd-dddd-dddddddddddd',  80.00, '2025-07-13',
   'Rejs statkiem',              'Activity',
   '2025-07-13 17:45:00', '2025-07-13 17:45:00');

-- ==========================================
-- 5. Dodanie udziałów w wydatkach (expense_participant_shares)
--    Każdy wiersz wskazuje expense_id oraz participant_id wraz z wartością share.
--    Suma share dla danego expense_id powinna równać się pola amount w tabeli expenses.
-- ==========================================
INSERT INTO expense_participant_shares (
    expense_id, participant_id, share
) VALUES
  -- Udziały dla wydatku '33333333-3333-3333-3333-333333333333' (200 PLN)
  ('33333333-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 100.00),
  ('33333333-3333-3333-3333-333333333333', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 100.00),

  -- Udziały dla wydatku '44444444-4444-4444-4444-444444444444' (100 PLN)
  ('44444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',  50.00),
  ('44444444-4444-4444-4444-444444444444', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',  50.00),

  -- Udziały dla wydatku '55555555-5555-5555-5555-555555555555' (300 PLN)
  ('55555555-5555-5555-5555-555555555555', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 150.00),
  ('55555555-5555-5555-5555-555555555555', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 150.00),

  -- Udziały dla wydatku '66666666-6666-6666-6666-666666666666' (80 PLN)
  ('66666666-6666-6666-6666-666666666666', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 40.00),
  ('66666666-6666-6666-6666-666666666666', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 40.00);