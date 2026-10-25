const express = require('express');
const crypto = require('crypto');
const { Pool } = require('pg');
require('dotenv').config();
const fs = require('fs');

if (fs.existsSync('/app/.dev.env.json')) {
  try {
    const devSecrets = JSON.parse(fs.readFileSync('/app/.dev.env.json', 'utf8'));
    for (const [k, v] of Object.entries(devSecrets)) {
      if (!process.env[k]) process.env[k] = v;
    }
  } catch (e) {}
}

const app = express();
app.use(express.json());

const pool = new Pool({
  connectionString: process.env.DATABASE_URL
});

// Helper: UPI ID Masking for User Privacy
function maskUpi(upi) {
  if (!upi) return '';
  const at = upi.indexOf('@');
  if (at <= 2) return upi;
  return upi.slice(0, 2) + '****' + upi.slice(at);
}

// Middleware: Validate Telegram WebApp initData HMAC signature
function verifyTelegramWebAppData(req, res, next) {
  const initData = req.headers['x-telegram-init-data'];
  if (!initData) return res.status(401).json({ error: 'Missing Telegram initData' });

  const urlParams = new URLSearchParams(initData);
  const hash = urlParams.get('hash');
  urlParams.delete('hash');

  const dataCheckString = Array.from(urlParams.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([key, val]) => `${key}=${val}`)
    .join('\n');

  const secretKey = crypto.createHmac('sha256', 'WebAppData')
    .update(process.env.TELEGRAM_BOT_TOKEN || '')
    .digest();

  const calculatedHash = crypto.createHmac('sha256', secretKey)
    .update(dataCheckString)
    .digest('hex');

  if (calculatedHash !== hash) {
    return res.status(403).json({ error: 'HMAC signature verification failed. Untrusted request.' });
  }

  try {
    req.telegramUser = JSON.parse(urlParams.get('user'));
    next();
  } catch (err) {
    res.status(400).json({ error: 'Invalid user payload in initData' });
  }
}

// Middleware: Strict Owner-Only Admin Authentication
function verifyAdminOwner(req, res, next) {
  const adminTelegramId = process.env.ADMIN_TELEGRAM_ID;
  if (!adminTelegramId || !adminTelegramId.trim()) {
    return res.status(403).json({
      error: 'Access Denied: ADMIN_TELEGRAM_ID environment variable is missing on server. Admin access is denied by default.'
    });
  }

  const requesterId = req.telegramUser?.id?.toString();
  if (!requesterId || requesterId !== adminTelegramId.trim()) {
    return res.status(403).json({
      error: 'Access Denied: Only the verified bot owner (ADMIN_TELEGRAM_ID) can access administrative endpoints.'
    });
  }

  next();
}

// ==========================================
// USER PRIVATE ENDPOINTS (Scoped to req.telegramUser)
// ==========================================

// Route: User Profile & Balance
app.get('/api/user/me', verifyTelegramWebAppData, async (req, res) => {
  const tgId = req.telegramUser.id;
  try {
    const { rows } = await pool.query('SELECT id, telegram_id, username, full_name, coin_balance, total_earned, total_withdrawn_inr, is_banned, referral_code, created_at FROM users WHERE telegram_id = $1', [tgId]);
    if (rows.length === 0) {
      // Auto-register
      const refCode = 'INR' + Math.floor(1000 + Math.random() * 9000);
      const newUser = await pool.query(
        'INSERT INTO users (id, telegram_id, username, full_name, referral_code) VALUES ($1, $2, $3, $4, $5) RETURNING id, telegram_id, username, full_name, coin_balance, total_earned, total_withdrawn_inr, is_banned, referral_code, created_at',
        [`usr_${tgId}`, tgId, req.telegramUser.username || '', req.telegramUser.first_name || 'User', refCode]
      );
      return res.json(newUser.rows[0]);
    }
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Route: User Transactions (Strictly restricted to own account)
app.get('/api/user/transactions', verifyTelegramWebAppData, async (req, res) => {
  const tgId = req.telegramUser.id;
  try {
    const userRes = await pool.query('SELECT id FROM users WHERE telegram_id = $1', [tgId]);
    if (userRes.rows.length === 0) return res.status(404).json({ error: 'User not registered' });
    const userId = userRes.rows[0].id;

    const { rows } = await pool.query(
      'SELECT id, amount, type, description, balance_after, created_at FROM transactions WHERE user_id = $1 ORDER BY id DESC LIMIT 100',
      [userId]
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Route: User Withdrawals (Strictly own records, UPI ID masked)
app.get('/api/user/withdrawals', verifyTelegramWebAppData, async (req, res) => {
  const tgId = req.telegramUser.id;
  try {
    const userRes = await pool.query('SELECT id FROM users WHERE telegram_id = $1', [tgId]);
    if (userRes.rows.length === 0) return res.status(404).json({ error: 'User not registered' });
    const userId = userRes.rows[0].id;

    const { rows } = await pool.query(
      'SELECT id, amount_inr, coins_used, upi_id, status, request_date, processed_date, payment_reference, admin_note FROM withdrawals WHERE user_id = $1 ORDER BY id DESC',
      [userId]
    );

    // Mask UPI ID for privacy protection
    const sanitized = rows.map(w => ({
      ...w,
      upi_id: maskUpi(w.upi_id)
    }));

    res.json(sanitized);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Route: User Referrals
app.get('/api/user/referrals', verifyTelegramWebAppData, async (req, res) => {
  const tgId = req.telegramUser.id;
  try {
    const userRes = await pool.query('SELECT id FROM users WHERE telegram_id = $1', [tgId]);
    if (userRes.rows.length === 0) return res.status(404).json({ error: 'User not registered' });
    const userId = userRes.rows[0].id;

    const { rows } = await pool.query(
      'SELECT id, referee_name, reward_coins, status, created_at FROM referrals WHERE referrer_id = $1 ORDER BY id DESC',
      [userId]
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Route: Request UPI Withdrawal (Atomic Transaction & Lock)
app.post('/api/withdraw', verifyTelegramWebAppData, async (req, res) => {
  const { amountInr, upiId, userName } = req.body;
  const tgId = req.telegramUser.id;

  if (![10, 30, 50, 100].includes(amountInr)) {
    return res.status(400).json({ error: 'Invalid tier. Allowed: 10, 30, 50, 100 INR' });
  }
  if (!/^[\w.-]+@[\w.-]+$/.test(upiId)) {
    return res.status(400).json({ error: 'Invalid UPI VPA address' });
  }

  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    // 1. Lock user row
    const userRes = await client.query('SELECT * FROM users WHERE telegram_id = $1 FOR UPDATE', [tgId]);
    const user = userRes.rows[0];
    if (!user) throw new Error('User not found');
    if (user.is_banned) throw new Error('Account is suspended');

    const coinsRequired = amountInr * 100;
    if (user.coin_balance < coinsRequired) {
      throw new Error(`Insufficient coins. Required: ${coinsRequired}, Current: ${user.coin_balance}`);
    }

    // 2. Anti-fraud check: Pending withdrawal lock
    const pendingRes = await client.query(
      "SELECT * FROM withdrawals WHERE user_id = $1 AND status = 'PENDING'",
      [user.id]
    );
    if (pendingRes.rows.length > 0) {
      throw new Error('You already have a pending withdrawal request in queue.');
    }

    // 3. Deduct coins & record withdrawal
    const newBalance = user.coin_balance - coinsRequired;
    await client.query('UPDATE users SET coin_balance = $1 WHERE id = $2', [newBalance, user.id]);

    const wRes = await client.query(
      "INSERT INTO withdrawals (user_id, user_name, amount_inr, coins_used, upi_id, status) VALUES ($1, $2, $3, $4, $5, 'PENDING') RETURNING id",
      [user.id, userName || user.full_name, amountInr, coinsRequired, upiId]
    );

    // 4. Record ledger transaction
    await client.query(
      "INSERT INTO transactions (user_id, amount, type, description, balance_after) VALUES ($1, $2, 'WITHDRAWAL', $3, $4)",
      [user.id, -coinsRequired, `Withdrawal Request #${wRes.rows[0].id} (₹${amountInr} to ${maskUpi(upiId)})`, newBalance]
    );

    await client.query('COMMIT');
    res.json({ success: true, withdrawalId: wRes.rows[0].id, status: 'PENDING' });
  } catch (err) {
    await client.query('ROLLBACK');
    res.status(400).json({ error: err.message });
  } finally {
    client.release();
  }
});

// ==========================================
// OWNER-ONLY ADMIN ENDPOINTS (Restricted to verified ADMIN_TELEGRAM_ID)
// ==========================================

// 1. Owner: View & Search all registered users
app.get('/api/admin/users', verifyTelegramWebAppData, verifyAdminOwner, async (req, res) => {
  const query = (req.query.q || '').trim();
  try {
    let result;
    if (query) {
      result = await pool.query(
        `SELECT id, telegram_id, username, full_name, coin_balance, total_earned, total_withdrawn_inr, is_banned, referral_code, created_at
         FROM users
         WHERE username ILIKE $1 OR full_name ILIKE $1 OR id ILIKE $1 OR telegram_id::text ILIKE $1
         ORDER BY created_at DESC`,
        [`%${query}%`]
      );
    } else {
      result = await pool.query(
        `SELECT id, telegram_id, username, full_name, coin_balance, total_earned, total_withdrawn_inr, is_banned, referral_code, created_at
         FROM users
         ORDER BY created_at DESC`
      );
    }
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 2. Owner: View single user complete profile, history & referrals
app.get('/api/admin/user/:userId/details', verifyTelegramWebAppData, verifyAdminOwner, async (req, res) => {
  const { userId } = req.params;
  try {
    const userRes = await pool.query('SELECT * FROM users WHERE id = $1', [userId]);
    if (userRes.rows.length === 0) return res.status(404).json({ error: 'User not found' });

    const txRes = await pool.query('SELECT * FROM transactions WHERE user_id = $1 ORDER BY id DESC LIMIT 50', [userId]);
    const withRes = await pool.query('SELECT * FROM withdrawals WHERE user_id = $1 ORDER BY id DESC', [userId]);
    const refRes = await pool.query('SELECT * FROM referrals WHERE referrer_id = $1 ORDER BY id DESC', [userId]);

    res.json({
      user: userRes.rows[0],
      transactions: txRes.rows,
      withdrawals: withRes.rows,
      referrals: refRes.rows
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 3. Owner: View all withdrawal requests & stats
app.get('/api/admin/withdrawals', verifyTelegramWebAppData, verifyAdminOwner, async (req, res) => {
  try {
    const { rows: withdrawals } = await pool.query(
      `SELECT w.*, u.coin_balance as user_current_coins, u.is_banned
       FROM withdrawals w
       LEFT JOIN users u ON w.user_id = u.id
       ORDER BY w.id DESC`
    );

    const statsRes = await pool.query(`
      SELECT 
        COUNT(*) FILTER (WHERE status = 'PENDING') as pending_count,
        COALESCE(SUM(amount_inr) FILTER (WHERE status = 'PAID'), 0) as total_paid_inr,
        COALESCE(SUM(amount_inr) FILTER (WHERE status = 'PENDING'), 0) as total_pending_inr
      FROM withdrawals
    `);

    res.json({
      withdrawals,
      stats: statsRes.rows[0]
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 4. Owner: Approve withdrawal
app.post('/api/admin/withdrawals/:id/approve', verifyTelegramWebAppData, verifyAdminOwner, async (req, res) => {
  const { id } = req.params;
  const adminId = `admin_${req.telegramUser.id}`;

  try {
    const wRes = await pool.query('SELECT * FROM withdrawals WHERE id = $1', [id]);
    if (wRes.rows.length === 0) return res.status(404).json({ error: 'Withdrawal not found' });
    const w = wRes.rows[0];
    if (w.status !== 'PENDING') return res.status(400).json({ error: 'Cannot approve non-pending withdrawal' });

    await pool.query(
      "UPDATE withdrawals SET status = 'APPROVED', processed_date = CURRENT_TIMESTAMP WHERE id = $1",
      [id]
    );

    await pool.query(
      'INSERT INTO admin_logs (admin_id, action_type, target_user_id, details) VALUES ($1, $2, $3, $4)',
      [adminId, 'WITHDRAW_APPROVE', w.user_id, `Approved withdrawal #${id} for ₹${w.amount_inr}`]
    );

    res.json({ success: true, message: `Withdrawal #${id} approved` });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 5. Owner: Mark withdrawal as PAID (Requires verified Bank UTR)
app.post('/api/admin/withdrawals/:id/mark-paid', verifyTelegramWebAppData, verifyAdminOwner, async (req, res) => {
  const { id } = req.params;
  const { paymentReference, adminNote } = req.body;
  const adminId = `admin_${req.telegramUser.id}`;

  if (!paymentReference || !paymentReference.trim()) {
    return res.status(400).json({ error: 'Bank Transaction Reference / UTR is required to mark as PAID.' });
  }

  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    const wRes = await client.query('SELECT * FROM withdrawals WHERE id = $1 FOR UPDATE', [id]);
    if (wRes.rows.length === 0) throw new Error('Withdrawal not found');
    const w = wRes.rows[0];
    if (w.status === 'PAID') throw new Error('Withdrawal is already marked as PAID');

    await client.query(
      "UPDATE withdrawals SET status = 'PAID', payment_reference = $1, admin_note = $2, processed_date = CURRENT_TIMESTAMP WHERE id = $3",
      [paymentReference.trim(), adminNote || null, id]
    );

    await client.query(
      'UPDATE users SET total_withdrawn_inr = total_withdrawn_inr + $1 WHERE id = $2',
      [w.amount_inr, w.user_id]
    );

    await client.query(
      'INSERT INTO admin_logs (admin_id, action_type, target_user_id, details) VALUES ($1, $2, $3, $4)',
      [adminId, 'WITHDRAW_PAID', w.user_id, `Marked withdrawal #${id} as PAID. UTR: ${paymentReference.trim()}, Note: ${adminNote || 'None'}`]
    );

    await client.query('COMMIT');
    res.json({ success: true, message: `Withdrawal #${id} marked as PAID with UTR ${paymentReference}` });
  } catch (err) {
    await client.query('ROLLBACK');
    res.status(400).json({ error: err.message });
  } finally {
    client.release();
  }
});

// 6. Owner: Reject withdrawal & Refund coins
app.post('/api/admin/withdrawals/:id/reject', verifyTelegramWebAppData, verifyAdminOwner, async (req, res) => {
  const { id } = req.params;
  const { reason } = req.body;
  const adminId = `admin_${req.telegramUser.id}`;

  if (!reason || !reason.trim()) {
    return res.status(400).json({ error: 'Rejection reason is required.' });
  }

  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    const wRes = await client.query('SELECT * FROM withdrawals WHERE id = $1 FOR UPDATE', [id]);
    if (wRes.rows.length === 0) throw new Error('Withdrawal not found');
    const w = wRes.rows[0];
    if (w.status === 'PAID') throw new Error('Cannot reject an already paid withdrawal');
    if (w.status === 'REJECTED') throw new Error('Withdrawal is already rejected');

    // Refund coins to user
    const userRes = await client.query('SELECT * FROM users WHERE id = $1 FOR UPDATE', [w.user_id]);
    const user = userRes.rows[0];
    const newBalance = user.coin_balance + w.coins_used;

    await client.query('UPDATE users SET coin_balance = $1 WHERE id = $2', [newBalance, w.user_id]);

    await client.query(
      "UPDATE withdrawals SET status = 'REJECTED', admin_note = $1, processed_date = CURRENT_TIMESTAMP WHERE id = $2",
      [`Rejected: ${reason.trim()}`, id]
    );

    await client.query(
      "INSERT INTO transactions (user_id, amount, type, description, balance_after) VALUES ($1, $2, 'REFUND', $3, $4)",
      [w.user_id, w.coins_used, `Refund for rejected withdrawal #${id}: ${reason.trim()}`, newBalance]
    );

    await client.query(
      'INSERT INTO admin_logs (admin_id, action_type, target_user_id, details) VALUES ($1, $2, $3, $4)',
      [adminId, 'WITHDRAW_REJECT', w.user_id, `Rejected withdrawal #${id}. Reason: ${reason.trim()}`]
    );

    await client.query('COMMIT');
    res.json({ success: true, message: `Withdrawal #${id} rejected and ${w.coins_used} coins refunded to user.` });
  } catch (err) {
    await client.query('ROLLBACK');
    res.status(400).json({ error: err.message });
  } finally {
    client.release();
  }
});

// 7. Owner: Adjust user coins
app.post('/api/admin/user/:userId/adjust-coins', verifyTelegramWebAppData, verifyAdminOwner, async (req, res) => {
  const { userId } = req.params;
  const { deltaCoins, reason } = req.body;
  const adminId = `admin_${req.telegramUser.id}`;

  if (typeof deltaCoins !== 'number' || deltaCoins === 0) {
    return res.status(400).json({ error: 'deltaCoins must be a non-zero number' });
  }

  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    const userRes = await client.query('SELECT * FROM users WHERE id = $1 FOR UPDATE', [userId]);
    if (userRes.rows.length === 0) throw new Error('User not found');
    const user = userRes.rows[0];
    const newBalance = Math.max(0, user.coin_balance + deltaCoins);

    await client.query('UPDATE users SET coin_balance = $1 WHERE id = $2', [newBalance, userId]);

    await client.query(
      "INSERT INTO transactions (user_id, amount, type, description, balance_after) VALUES ($1, $2, 'ADMIN_ADJUST', $3, $4)",
      [userId, deltaCoins, `Admin adjustment: ${reason || 'Manual correction'}`, newBalance]
    );

    await client.query(
      'INSERT INTO admin_logs (admin_id, action_type, target_user_id, details) VALUES ($1, $2, $3, $4)',
      [adminId, 'COIN_ADJUST', userId, `Adjusted coins by ${deltaCoins} (${reason || 'None'}). Balance: ${newBalance}`]
    );

    await client.query('COMMIT');
    res.json({ success: true, newBalance });
  } catch (err) {
    await client.query('ROLLBACK');
    res.status(400).json({ error: err.message });
  } finally {
    client.release();
  }
});

// 8. Owner: Toggle Ban on user
app.post('/api/admin/user/:userId/toggle-ban', verifyTelegramWebAppData, verifyAdminOwner, async (req, res) => {
  const { userId } = req.params;
  const { isBanned } = req.body;
  const adminId = `admin_${req.telegramUser.id}`;

  try {
    const userRes = await pool.query('SELECT * FROM users WHERE id = $1', [userId]);
    if (userRes.rows.length === 0) return res.status(404).json({ error: 'User not found' });

    await pool.query('UPDATE users SET is_banned = $1 WHERE id = $2', [Boolean(isBanned), userId]);

    await pool.query(
      'INSERT INTO admin_logs (admin_id, action_type, target_user_id, details) VALUES ($1, $2, $3, $4)',
      [adminId, Boolean(isBanned) ? 'BAN_USER' : 'UNBAN_USER', userId, `Set is_banned=${Boolean(isBanned)}`]
    );

    res.json({ success: true, isBanned: Boolean(isBanned) });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 9. Owner: View audit logs
app.get('/api/admin/logs', verifyTelegramWebAppData, verifyAdminOwner, async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM admin_logs ORDER BY id DESC LIMIT 100');
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`API Server listening on port ${PORT}`));
