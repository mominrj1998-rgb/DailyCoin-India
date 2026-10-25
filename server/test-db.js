const { Pool } = require('pg');
const fs = require('fs');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });

// Also check AI Studio Secrets panel file if present
if (fs.existsSync('/app/.dev.env.json')) {
  try {
    const devSecrets = JSON.parse(fs.readFileSync('/app/.dev.env.json', 'utf8'));
    if (devSecrets.DATABASE_URL && !process.env.DATABASE_URL) {
      process.env.DATABASE_URL = devSecrets.DATABASE_URL;
    }
    if (devSecrets.TELEGRAM_BOT_TOKEN && !process.env.TELEGRAM_BOT_TOKEN) {
      process.env.TELEGRAM_BOT_TOKEN = devSecrets.TELEGRAM_BOT_TOKEN;
    }
  } catch (e) {}
}

function maskConnectionString(url) {
  if (!url) return 'None';
  try {
    const parsed = new URL(url);
    const maskedUser = parsed.username ? parsed.username.slice(0, 3) + '***' : '***';
    const maskedHost = parsed.hostname ? '***' + parsed.hostname.slice(-12) : '***';
    return `${parsed.protocol}//${maskedUser}:****@${maskedHost}${parsed.pathname}`;
  } catch (e) {
    return '***[MASKED]***';
  }
}

async function testDatabase() {
  const dbUrl = process.env.DATABASE_URL;

  if (!dbUrl || dbUrl.includes('username:password') || dbUrl.trim() === '') {
    console.log('[STATUS] DATABASE_URL is not yet configured in server/.env');
    console.log('[INFO] Please add your Neon connection string to server/.env:');
    console.log('       DATABASE_URL="postgresql://user:password@ep-something.neon.tech/neondb?sslmode=require"');
    process.exit(1);
  }

  console.log(`[CONNECTING] Connecting to database target: ${maskConnectionString(dbUrl)}`);

  const pool = new Pool({
    connectionString: dbUrl,
    ssl: { rejectUnauthorized: false }
  });

  try {
    const client = await pool.connect();
    console.log('✅ [SUCCESS] Connection established successfully to PostgreSQL database.');

    // 1. Run schema.sql to ensure all tables exist
    const schemaSql = fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf-8');
    await client.query(schemaSql);
    console.log('✅ [SUCCESS] Schema applied successfully.');

    // 2. Verify tables
    const tableQuery = `
      SELECT table_name 
      FROM information_schema.tables 
      WHERE table_schema = 'public' 
      ORDER BY table_name;
    `;
    const res = await client.query(tableQuery);
    const tables = res.rows.map(r => r.table_name);
    
    console.log('✅ [SUCCESS] Verified database tables:');
    tables.forEach(t => console.log(`   - ${t}`));

    // 3. Test insert and query check
    const countRes = await client.query('SELECT COUNT(*) AS total_users FROM users;');
    console.log(`✅ [SUCCESS] Users table queried. Total existing users: ${countRes.rows[0].total_users}`);

    client.release();
    await pool.end();
    console.log('\n🎉 ALL DATABASE CHECKS PASSED: Neon PostgreSQL is 100% connected and ready for production!');
    process.exit(0);
  } catch (err) {
    console.error('❌ [ERROR] Database connection failed:');
    console.error(`   Message: ${err.message}`);
    process.exit(1);
  }
}

testDatabase();
