package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*

@Composable
fun HelpFaqScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenBotSpec: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            isOwner = isOwner,
            title = "❓ Help & Earning FAQ",
            onBack = onBack,
            onOpenAdmin = onOpenAdmin,
            onOpenBotSpec = onOpenBotSpec
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Rules Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = CoinGold)
                        Text("Official Coin Value & Payout Schedule", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• 100 Coins = ₹1 INR\n• 1,000 Coins = ₹10 INR\n• Minimum Withdrawal: 1,000 Coins (₹10)\n• Withdrawal Tiers: ₹10, ₹30, ₹50, ₹100\n• Payments are sent manually to your registered UPI ID by the Admin\n• Processing Window: Usually within 24 to 48 hours",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Anti-Fraud Policy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = WarningOrange)
                        Text("Zero-Tolerance Anti-Fraud Rules", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. No Fake Claims: We never produce fake payment screenshots or simulated success dialogs.\n2. No Multi-Accounting: Users creating multiple accounts to farm referral bonuses will have all accounts permanently frozen and coins forfeited.\n3. Daily Caps: Daily limits on Spins (100) and Math challenges (30) prevent programmatic spam.\n4. Pending Lock: Users cannot submit multiple simultaneous withdrawal requests.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Telegram Bot Commands Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = TgBlueLight)
                        Text("Telegram Bot Commands", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• /start - Launch the INR Earn WebApp and create your wallet\n• /balance - Check your live Coin balance and INR conversion\n• /spin - Direct link to today's free spin wheel\n• /tasks - View open tasks and verification status\n• /withdraw - Request manual UPI payout\n• /support - Connect with Telegram admin support team",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
