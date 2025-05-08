CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    category VARCHAR(255) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    payer_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE expense_participant_shares (
    expense_id UUID NOT NULL,
    participant_id UUID NOT NULL,
    share DECIMAL(5, 2) NOT NULL,
    PRIMARY KEY (expense_id, participant_id),
    FOREIGN KEY (expense_id) REFERENCES expenses(id)
);


-- Sample Expenses
INSERT INTO expenses (id, trip_id, amount, category, description, date, payer_id, created_at, updated_at)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 120.00, 'FOOD', 'Obiad w restauracji', '2025-04-10', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 350.00, 'ACCOMMODATION', 'Hotel 2 noce', '2025-04-09', 'cccccccc-cccc-cccc-cccc-cccccccccccc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('33333333-3333-3333-3333-333333333333', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 45.00, 'TRANSPORT', 'Taxi z lotniska', '2025-04-08', 'dddddddd-dddd-dddd-dddd-dddddddddddd', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('44444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 60.00, 'ACTIVITIES', 'Muzeum', '2025-04-11', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('55555555-5555-5555-5555-555555555555', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 80.00, 'OTHER', 'Pamiątki', '2025-04-07', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Participant Shares
INSERT INTO expense_participant_shares (expense_id, participant_id, share)
VALUES
  -- Expense 1
  ('11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0.5),
  ('11111111-1111-1111-1111-111111111111', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 0.5),

  -- Expense 2
  ('22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0.25),
  ('22222222-2222-2222-2222-222222222222', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 0.25),
  ('22222222-2222-2222-2222-222222222222', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 0.5),

  -- Expense 3
  ('33333333-3333-3333-3333-333333333333', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 1.0),

  -- Expense 4
  ('44444444-4444-4444-4444-444444444444', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0.5),
  ('44444444-4444-4444-4444-444444444444', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 0.5),

  -- Expense 5
  ('55555555-5555-5555-5555-555555555555', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 1.0);
