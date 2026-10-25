package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*

@Composable
fun BotSpecScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    var selectedSpecTab by remember { mutableIntStateOf(0) } // 0: Bot API, 1: Backend Architecture, 2: Database Schema, 3: Security & Anti-Fraud

    val botJsCode = """
// ==========================================
// TELEGRAM BOT SERVER (bot.js) - Telegraf / Node.js
// ==========================================
const { Telegraf, Markup } = require('telegraf');
require('dotenv').config();

const bot = new Telegraf(process.env.TELEGRAM_BOT_TOKEN);
const WEBAPP_URL = process.env.WEBAPP_URL; // e.g. https://earn.example.com

// Command: /start (Supports referral parameter)
bot.command('start', async (ctx) => {
  const telegramId = ctx.from.id;
  const username = ctx.from.username || '';
  const fullName = [ctx.from.first_name, ctx.from.last_name].filter(Boolean).join(' ');
  const startPayload = ctx.message.text.split(' ')[1]; // e.g. 'ref_12345'

  // Register or fetch user from database
  await syncUserWithBackend({ telegramId, username, fullName, startPayload });

  ctx.reply(
    "👋 Welcome to *INR Earn Bot*!\n\n" +
    `💰 Rate: 100 Coins = ₹1 INR\n` +
    `🎰 100 Free Daily Spins\n` +
    `🍕 Math Pizza Challenge\n` +
    `👥 ₹0.50 (50 Coins) per qualified referral\n` +
    `💵 Real Manual UPI Withdrawals (Min ₹10 / 1,000 Coins)\n\n` +
    `Click below to open the WebApp:`,
    {
      parse_mode: 'Markdown',
      ...Markup.inlineKeyboard([
        [Markup.button.webApp('🚀 Open INR Earn App', `${'$'}WEBAPP_URL?user=${'$'}telegramId`)],
        [Markup.button.url('📢 Official Channel', 'https://t.me/INR_Earn_Official')]
      ])
    }
  );
});

bot.command('balance', async (ctx) => {
  const user = await fetchUserFromBackend(ctx.from.id);
  ctx.reply(
    `💳 *Your Wallet Balance*\n\n` +
    `• Coins: ${'$'}user.coinBalance.toLocaleString()} Coins\n` +
    `• Equivalent: ₹${'$'}(user.coinBalance / 100).toFixed(2)} INR\n` +
    `• Pending Withdrawal: ₹${'$'}user.pendingInr}\n` +
    `• Total Withdrawn: ₹${'$'}user.totalWithdrawnInr}\n\n` +
    `Open the WebApp to earn more or withdraw.`,
    { parse_mode: 'Markdown' }
  );
});

bot.launch().then(() => console.log('Telegram Bot running...'));
"""

    val apiJsCode = """
// ==========================================
// BACKEND API & HMAC VALIDATION (api.js)
// ==========================================
const express = require('express');
const crypto = require('crypto');
const app = express();
app.use(express.json());

// TELEGRAM INITDATA HMAC VERIFICATION MIDDLEWARE
function verifyTelegramWebAppData(req, res, next) {
  const initData = req.headers['x-telegram-init-data'];
  if (!initData) return res.status(401).json({ error: 'Missing initData header' });

  const urlParams = new URLSearchParams(initData);
  const hash = urlParams.get('hash');
  urlParams.delete('hash');

  const dataCheckString = Array.from(urlParams.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([key, val]) => `${'$'}key}=${'$'}val}`)
    .join('\n');

  const secretKey = crypto.createHmac('sha256', 'WebAppData')
    .update(process.env.TELEGRAM_BOT_TOKEN)
    .digest();

  const calculatedHash = crypto.createHmac('sha256', secretKey)
    .update(dataCheckString)
    .digest('hex');

  if (calculatedHash !== hash) {
    return res.status(403).json({ error: 'Invalid HMAC signature. Anti-fraud blocked.' });
  }

  req.telegramUser = JSON.parse(urlParams.get('user'));
  next();
}

// ATOMIC WITHDRAWAL ROUTE
app.post('/api/withdraw', verifyTelegramWebAppData, async (req, res) => {
  const { amountInr, upiId } = req.body;
  const userId = req.telegramUser.id;

  // 1. Validate UPI regex
  if (!/^[\w.-]+@[\w.-]+$/.test(upiId)) {
    return res.status(400).json({ error: 'Invalid UPI format' });
  }

  // 2. Atomic SQL Transaction:
  // - Check for existing PENDING request
  // - Lock user row (SELECT ... FOR UPDATE)
  // - Deduct coins (amountInr * 100)
  // - Insert into withdrawals with status = 'PENDING'
  // - Insert into ledger transactions with balance_after
  res.json({ success: true, status: 'PENDING' });
});
"""

    val sqlSchema = """
-- ==========================================
-- PRODUCTION SQL SCHEMA (PostgreSQL / SQLite)
-- ==========================================

CREATE TABLE users (
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

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) REFERENCES users(id),
    amount BIGINT NOT NULL, -- Positive: credit, Negative: debit
    type VARCHAR(32) NOT NULL, -- SPIN, MATH, AD, BONUS, TASK, REFERRAL, WITHDRAWAL, REFUND, ADMIN_ADJUST
    description TEXT NOT NULL,
    balance_after BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE withdrawals (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) REFERENCES users(id),
    user_name VARCHAR(128) NOT NULL,
    amount_inr BIGINT NOT NULL, -- 10, 30, 50, 100
    coins_used BIGINT NOT NULL, -- 1000, 3000, 5000, 10000
    upi_id VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, PAID, REJECTED
    request_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_date TIMESTAMP WITH TIME ZONE,
    payment_reference VARCHAR(64), -- Real bank UTR
    admin_note TEXT
);

CREATE TABLE spins (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) REFERENCES users(id),
    reward_coins BIGINT NOT NULL,
    date_string VARCHAR(10) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_spins_daily ON spins(user_id, date_string);
"""

    val securityChecklist = """
==========================================
PRODUCTION SECURITY & ANTI-FRAUD CHECKLIST
==========================================

1. SERVER-SIDE BALANCE ARBITRATION:
   • The mobile client/WebApp NEVER computes or dictates balances.
   • Balances are derived from atomic SQL ledger transactions (SELECT ... FOR UPDATE).
   • Every credit or debit creates an immutable transaction entry.

2. CRYPTOGRAPHIC TELEGRAM AUTHENTICATION:
   • The backend validates the `initData` query string using HMAC-SHA256 with the bot token.
   • Prevents forged requests or unauthorized client injection.

3. WITHDRAWAL CONCURRENCY LOCK:
   • Strict rule: A user cannot submit a new withdrawal request if another is PENDING.
   • Prevents race condition double-spending before ledger sync.

4. REAL UPI PAYOUT INTEGRATION:
   • Admin manually initiates UPI payments (Google Pay, PhonePe, BHIM, Paytm).
   • Status changes from PENDING -> PAID ONLY after the admin registers the 12-digit UTR.
   • No fake payment receipts or simulated transfers.

5. RATE LIMITING & ABUSE MITIGATION:
   • Daily Spin Limit: 100 spins max/day per Telegram ID.
   • Math Game Daily Limit: 30 challenges max/day.
   • Rewarded Ad Limit: 100 ads max/day (requires verified AdMob/Unity SDK callback).
   • Referral qualification rule: Referee must complete 3 spins or 1 task to prevent fake bots.
"""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            isOwner = isOwner,
            title = "💻 Telegram Bot & API Spec",
            onBack = onBack,
            onOpenAdmin = onOpenAdmin,
            onOpenBotSpec = { /* Already here */ }
        )

        ScrollableTabRow(
            selectedTabIndex = selectedSpecTab,
            containerColor = TgSurfaceDark,
            contentColor = Color.White,
            edgePadding = 12.dp
        ) {
            listOf("Telegram Bot (bot.js)", "Backend API (api.js)", "Database SQL Schema", "Security Checklist").forEachIndexed { i, title ->
                Tab(
                    selected = selectedSpecTab == i,
                    onClick = { selectedSpecTab = i },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedSpecTab == i) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val codeContent = when (selectedSpecTab) {
                        0 -> botJsCode.trimIndent()
                        1 -> apiJsCode.trimIndent()
                        2 -> sqlSchema.trimIndent()
                        else -> securityChecklist.trimIndent()
                    }

                    Text(
                        text = codeContent,
                        color = Color(0xFFE2E8F0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}
