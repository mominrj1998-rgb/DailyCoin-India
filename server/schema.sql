-- PostgreSQL / SQLite Production Database Schema for INR Earn Bot
-- Matches Room entities in Android prototype

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    telegram_id BIGINT UNIQUE NOT NULL,
    username VARCHAR(64),
    full_name VARCHAR(128) NOT NULL,
    coin_balance BIGINT NOT NULL DEFAULT 0,
    total_earned BIGINT NOT NULL DEFAULT 0,
    total_withdrawn_inr BIGINT NOT NULL DEFAULT 0,
    is_banned BOOLEAN NOT NULL DEFAULT FALSE,
    referral_code VARCHAR(32) UNIQUE NOT NULL,
    referred_by VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount BIGINT NOT NULL, -- Positive for credit, negative for debit
    type VARCHAR(32) NOT NULL, -- SPIN, MATH, AD, DAILY_BONUS, TASK, REFERRAL, WITHDRAWAL, REFUND, ADMIN_ADJUST
    description TEXT NOT NULL,
    balance_after BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS withdrawals (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_name VARCHAR(128) NOT NULL,
    amount_inr BIGINT NOT NULL, -- 10, 30, 50, 100
    coins_used BIGINT NOT NULL, -- 1000, 3000, 5000, 10000
    upi_id VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, PAID, REJECTED
    request_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_date TIMESTAMP WITH TIME ZONE,
    payment_reference VARCHAR(64), -- Bank UTR / UPI Ref from manual transfer
    admin_note TEXT
);

CREATE TABLE IF NOT EXISTS spins (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reward_coins BIGINT NOT NULL,
    date_string VARCHAR(10) NOT NULL, -- YYYY-MM-DD
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game_records (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    game_type VARCHAR(32) NOT NULL,
    question TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    reward_coins BIGINT NOT NULL,
    date_string VARCHAR(10) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS daily_rewards (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_number INT NOT NULL,
    coins_awarded BIGINT NOT NULL,
    claim_date_string VARCHAR(10) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    reward_coins BIGINT NOT NULL,
    action_url TEXT NOT NULL,
    icon_name VARCHAR(32) NOT NULL DEFAULT 'task',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_tasks (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_id VARCHAR(64) NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED', -- NOT_STARTED, VERIFYING, COMPLETED, CLAIMED
    verification_start_time TIMESTAMP WITH TIME ZONE,
    claimed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS referrals (
    id BIGSERIAL PRIMARY KEY,
    referrer_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    referee_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    referee_name VARCHAR(128) NOT NULL,
    reward_coins BIGINT NOT NULL DEFAULT 50,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, QUALIFIED, PAID
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_id VARCHAR(64) NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    target_user_id VARCHAR(64),
    details TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for anti-abuse and fast lookups
CREATE INDEX IF NOT EXISTS idx_spins_daily ON spins(user_id, date_string);
CREATE INDEX IF NOT EXISTS idx_games_daily ON game_records(user_id, date_string);
CREATE INDEX IF NOT EXISTS idx_withdrawals_status ON withdrawals(status);
CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id, created_at DESC);
