# Production Security & Anti-Fraud Checklist

### 1. Server-Authoritative Ledger
- [x] Client never decides its own coin balance. All balances are verified against atomic ledger records (`transactions` table).
- [x] Concurrent withdrawal prevention: A user cannot submit a new withdrawal while one is `PENDING`.
- [x] Negative coin checks: Transactions and withdrawals verify that `balance >= coins_required` prior to execution using row locks (`SELECT ... FOR UPDATE`).

### 2. Cryptographic Telegram Verification
- [x] In production, all WebApp requests send the raw `window.Telegram.WebApp.initData` header.
- [x] Server validates `initData` with HMAC-SHA256 signature using the Telegram Bot Token. Requests with invalid signatures are rejected with HTTP 403.

### 3. Anti-Abuse Rate Limiting
- [x] Free Spin limit: Exactly 100 spins max per day per Telegram ID.
- [x] Math Challenge limit: Maximum 30 questions per day to prevent automated spamming.
- [x] Rewarded Ads: 100 max per day. Requires verified SDK callback (`onUserEarnedReward`) from AdMob or Unity Ads.
- [x] Anti-Self Referral: Users cannot refer their own account or referral code.
- [x] Referral Qualification Rule: Inviter is only rewarded after referee completes at least 3 spins or 1 task, defeating automated fake-account bots.

### 4. Zero-Fake-Confirmation Principle
- [x] No fake payment receipts or simulated success screens.
- [x] Withdrawals always start in `PENDING` status.
- [x] The `PAID` status is only permitted after an Admin manually enters the verified bank UPI UTR reference number.
- [x] Rejected withdrawals automatically refund coins back to the user's ledger with an explanatory audit note.

### 5. Administrative Audit Logging
- [x] Every administrative action (coin balance adjustment, ban/unban, withdrawal approval/rejection/payment) generates an immutable record in `admin_logs`.
- [x] Mandatory audit reason required for manual coin balance changes.
