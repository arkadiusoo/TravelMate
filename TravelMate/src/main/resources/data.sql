-- First, insert users (must come before participants since participants reference users)
INSERT INTO users (id, email, first_name, last_name, password, role)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'alice@example.com', 'Alice', 'Smith', '$2a$10$dummyhashedpassword1234567890', 'USER'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'bob@example.com', 'Bob', 'Johnson', '$2a$10$dummyhashedpassword1234567890', 'USER'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'carol@example.com', 'Carol', 'Williams', '$2a$10$dummyhashedpassword1234567890', 'USER'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'dan@example.com', 'Dan', 'Brown', '$2a$10$dummyhashedpassword1234567890', 'USER'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'emma@example.com', 'Emma', 'Davis', '$2a$10$dummyhashedpassword1234567890', 'USER');

-- Sample Trips
INSERT INTO trip (id, name, start_date, end_date)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Wycieczka do Rzymu', '2025-04-07', '2025-04-12'),
    ('22222222-2222-2222-2222-222222222222', 'Weekend w Paryżu', '2025-04-06', '2025-04-09');

-- Sample Participants (must come after users and trips)
INSERT INTO participants (id, user_id, trip_id, email, role, status, created_at, updated_at)
VALUES
    -- Trip 1: Wycieczka do Rzymu
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'alice@example.com', 'ORGANIZER', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', 'bob@example.com', 'MEMBER', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ccccccc3-cccc-cccc-cccc-cccccccccccc', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111', 'carol@example.com', 'MEMBER', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Trip 2: Weekend w Paryżu
    ('ddddddd4-dddd-dddd-dddd-dddddddddddd', 'dddddddd-dddd-dddd-dddd-dddddddddddd', '22222222-2222-2222-2222-222222222222', 'dan@example.com', 'ORGANIZER', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '22222222-2222-2222-2222-222222222222', 'emma@example.com', 'MEMBER', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Expenses (must come after participants since payer_id references participants)
INSERT INTO expenses (id, trip_id, amount, category, description, date, payer_id, created_at, updated_at)
VALUES
    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 120.00, 'FOOD', 'Obiad w restauracji', '2025-04-10', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 350.00, 'ACCOMMODATION', 'Hotel 2 noce', '2025-04-09', 'ccccccc3-cccc-cccc-cccc-cccccccccccc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', 45.00, 'TRANSPORT', 'Taxi z lotniska', '2025-04-08', 'ddddddd4-dddd-dddd-dddd-dddddddddddd', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', 60.00, 'ACTIVITIES', 'Muzeum', '2025-04-11', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('77777777-7777-7777-7777-777777777777', '22222222-2222-2222-2222-222222222222', 80.00, 'OTHER', 'Pamiątki', '2025-04-07', 'eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Participant Shares (must come after expenses and participants)
INSERT INTO expense_participant_shares (expense_id, participant_id, share)
VALUES
    -- Expense 1 (Food)
    ('33333333-3333-3333-3333-333333333333', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0.5),
    ('33333333-3333-3333-3333-333333333333', 'ccccccc3-cccc-cccc-cccc-cccccccccccc', 0.5),

    -- Expense 2 (Hotel)
    ('44444444-4444-4444-4444-444444444444', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 0.33),
    ('44444444-4444-4444-4444-444444444444', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0.33),
    ('44444444-4444-4444-4444-444444444444', 'ccccccc3-cccc-cccc-cccc-cccccccccccc', 0.34),

    -- Expense 3 (Transport)
    ('55555555-5555-5555-5555-555555555555', 'ddddddd4-dddd-dddd-dddd-dddddddddddd', 0.5),
    ('55555555-5555-5555-5555-555555555555', 'eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', 0.5),

    -- Expense 4 (Museum)
    ('66666666-6666-6666-6666-666666666666', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 0.5),
    ('66666666-6666-6666-6666-666666666666', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0.5),

    -- Expense 5 (Souvenirs)
    ('77777777-7777-7777-7777-777777777777', 'eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', 1.0);