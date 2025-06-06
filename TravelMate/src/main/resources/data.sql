-- Sample Trips
INSERT INTO trip (id, name, start_date, end_date)
VALUES
  (1, 'Wycieczka do Rzymu', '2025-04-07', '2025-04-12'),
  (2, 'Weekend w Paryżu', '2025-04-06', '2025-04-09');

-- Sample Expenses
INSERT INTO expenses (id, trip_id, amount, category, description, date, payer_id, created_at, updated_at)
VALUES
  ('11111111-1111-1111-1111-111111111111', 1, 120.00, 'FOOD', 'Obiad w restauracji', '2025-04-10', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('22222222-2222-2222-2222-222222222222', 1, 350.00, 'ACCOMMODATION', 'Hotel 2 noce', '2025-04-09', 'cccccccc-cccc-cccc-cccc-cccccccccccc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('33333333-3333-3333-3333-333333333333', 2, 45.00, 'TRANSPORT', 'Taxi z lotniska', '2025-04-08', 'cccccccc-cccc-cccc-cccc-cccccccccccc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('44444444-4444-4444-4444-444444444444', 1, 60.00, 'ACTIVITIES', 'Muzeum', '2025-04-11', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('55555555-5555-5555-5555-555555555555', 2, 80.00, 'OTHER', 'Pamiątki', '2025-04-07', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Participant Shares
INSERT INTO expense_participant_shares (expense_id, participant_id, share)
VALUES
  -- Expense 1
  ('11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0.5),
  ('11111111-1111-1111-1111-111111111111', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 0.5),

  -- Expense 2
  ('22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0.25),
  ('22222222-2222-2222-2222-222222222222', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 0.25),
  ('22222222-2222-2222-2222-222222222222', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 0.5),

  -- Expense 3
  ('33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 1.0),

  -- Expense 4
  ('44444444-4444-4444-4444-444444444444', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0.5),
  ('44444444-4444-4444-4444-444444444444', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 0.5),

  -- Expense 5
  ('55555555-5555-5555-5555-555555555555', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 1.0);

-- Sample Participants
INSERT INTO participants (id, user_id, trip_id, email, role, status, created_at, updated_at)
VALUES
  -- Trip 1: Wycieczka do Rzymu
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'user-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1, 'alice@example.com', 'ORGANIZER', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'user-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 1, 'bob@example.com', 'PARTICIPANT', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ccccccc3-cccc-cccc-cccc-cccccccccccc', 'user-cccc-cccc-cccc-cccccccccccc', 1, 'carol@example.com', 'PARTICIPANT', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  -- Trip 2: Weekend w Paryżu
  ('ddddddd4-dddd-dddd-dddd-dddddddddddd', 'user-dddd-dddd-dddd-dddddddddddddd', 2, 'dan@example.com', 'ORGANIZER', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', 'user-eeee-eeee-eeee-eeeeeeeeeeee', 2, 'emma@example.com', 'PARTICIPANT', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);