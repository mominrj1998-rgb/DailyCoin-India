package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.components.SpinWheelView
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*

@Composable
fun FreeSpinScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    dailySpinsUsed: Int,
    maxDailySpins: Int = 100,
    isSpinning: Boolean,
    onSpinRequested: (onSpinReady: (rewardWon: Long, onAnimationComplete: () -> Unit) -> Unit) -> Unit,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenBotSpec: () -> Unit
) {
    val remaining = (maxDailySpins - dailySpinsUsed).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            isOwner = isOwner,
            title = "🎰 Free Spin Wheel",
            onBack = onBack,
            onOpenAdmin = onOpenAdmin,
            onOpenBotSpec = onOpenBotSpec
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Interactive Spin Wheel
            SpinWheelView(
                remainingSpins = remaining,
                maxDailySpins = maxDailySpins,
                isSpinning = isSpinning,
                onSpinRequested = onSpinRequested
            )

            // Reward Rules & Fair Odds Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Wheel Reward Table",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Possible Rewards: 5, 10, 15, 30, 40, 50, 75, 100 Coins\n• Maximum Spins: 100 per day\n• Every coin won is instantly recorded in your immutable ledger\n• No bots, automated clickers, or scripted hacks allowed",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TgSurfaceElevated
                    ) {
                        Text(
                            text = "Daily reset occurs every midnight (00:00 UTC)",
                            color = TgBlueLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
