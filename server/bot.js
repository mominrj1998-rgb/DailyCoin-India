const { Telegraf, Markup } = require('telegraf');
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

const bot = new Telegraf(process.env.TELEGRAM_BOT_TOKEN);
const WEBAPP_URL = process.env.WEBAPP_URL || 'https://inr-earn-webapp.example.com';

bot.command('start', async (ctx) => {
  const telegramId = ctx.from.id;
  const username = ctx.from.username || '';
  const fullName = [ctx.from.first_name, ctx.from.last_name].filter(Boolean).join(' ');
  const startPayload = ctx.message.text.split(' ')[1] || '';

  const webAppLaunchUrl = `${WEBAPP_URL}?tg_id=${telegramId}&ref=${startPayload}`;

  await ctx.reply(
    `👋 *Welcome to INR Earn Bot*, ${fullName}!\n\n` +
    `💰 *Fair Coin Rates:* 100 Coins = ₹1 INR\n` +
    `🎰 *100 Free Spins Daily:* Win 5 to 100 Coins\n` +
    `🍕 *Math Pizza Challenge:* Quick brain math for coins\n` +
    `👥 *Referral Program:* Earn 50 Coins per qualified friend\n` +
    `💵 *Real UPI Payouts:* Min ₹10 (1,000 Coins) manual payout by Admin\n\n` +
    `Click below to launch your WebApp dashboard:`,
    {
      parse_mode: 'Markdown',
      ...Markup.inlineKeyboard([
        [Markup.button.webApp('🚀 Open INR Earn App', webAppLaunchUrl)],
        [Markup.button.url('📢 Official Telegram Channel', 'https://t.me/INR_Earn_Official')]
      ])
    }
  );
});

bot.command('balance', async (ctx) => {
  await ctx.reply(
    `💰 *Your Live Balance:*\n\n` +
    `To view your verified coin ledger and request a UPI withdrawal, open the WebApp below:`,
    {
      parse_mode: 'Markdown',
      ...Markup.inlineKeyboard([
        [Markup.button.webApp('💳 Open Wallet', `${WEBAPP_URL}?tg_id=${ctx.from.id}`)]
      ])
    }
  );
});

bot.launch().then(() => {
  console.log('INR Earn Telegram Bot is running...');
});

process.once('SIGINT', () => bot.stop('SIGINT'));
process.once('SIGTERM', () => bot.stop('SIGTERM'));
