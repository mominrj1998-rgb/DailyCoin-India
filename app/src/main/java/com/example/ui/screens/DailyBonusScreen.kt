package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyRewardEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyBonusScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    lastReward: DailyRewardEntity?,
    onClaimBonus: (onSuccess: (Int, Long) -> Unit) -> Unit,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenBotSpec: () -> Unit
) {
    val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isAlreadyClaimedToday = lastReward?.claimDateString == todayString
    val currentDayStreak = lastReward?.dayNumber ?: 0

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

    val nextDayToClaim = when {
        lastReward == null -> 1
        isAlreadyClaimedToday -> currentDayStreak
        lastReward.claimDateString == yesterdayString -> if (currentDayStreak >= 7) 1 else currentDayStreak + 1
        else -> 1 // Streak broken
    }

    val schedule = listOf(
        1 to 10L,
        2 to 15L,
        3 to 20L,
        4 to 30L,
        5 to 40L,
        6 to 50L,
        7 to 100L
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            isOwner = isOwner,
            title = "🎁 Daily Login Bonus",
            onBack = onBack,
            onOpenAdmin = onOpenAdmin,
            onOpenBotSpec = onOpenBotSpec
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header streak badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = SuccessGreenLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isAlreadyClaimedToday) "Streak: Day $currentDayStreak Active" else "Ready to claim Day $nextDayToClaim",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isAlreadyClaimedToday) "You have already collected today's reward! Return tomorrow." else "Log in daily without missing a day to reach the 100 Coin mega reward!",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7 Days Grid / List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                schedule.forEach { (day, coins) ->
                    val isPastClaimed = (isAlreadyClaimedToday && day <= currentDayStreak) ||
                            (!isAlreadyClaimedToday && day < nextDayToClaim)
                    val isCurrentTarget = !isAlreadyClaimedToday && day == nextDayToClaim

                    val cardBorder = when {
                        isCurrentTarget -> androidx.compose.foundation.BorderStroke(1.5.dp, CoinGold)
                        isPastClaimed -> androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                        else -> androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                    }

                    val bgColor = when {
                        isCurrentTarget -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                        isPastClaimed -> Color(0xFF10B981).copy(alpha = 0.08f)
                        else -> TgSurfaceDark
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = bgColor,
                        border = cardBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPastClaimed) SuccessGreen else TgSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPastClaimed) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Claimed",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else if (isCurrentTarget) {
                                        Text(
                                            text = "$day",
                                            color = CoinGoldLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Day $day",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (day == 7) "Mega 7-Day Jackpot" else "Daily Check-in",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+$coins",
                                    color = if (isCurrentTarget) CoinGoldLight else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Coins",
                                    color = CoinGold,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Claim Button
            Button(
                onClick = {
                    onClaimBonus { day, coins -> }
                },
                enabled = !isAlreadyClaimedToday,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_claim_daily_bonus"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoinGold,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF334155),
                    disabledContentColor = Color(0xFF94A3B8)
                )
            ) {
                Text(
                    text = if (isAlreadyClaimedToday) "CLAIMED FOR TODAY (COME BACK TOMORROW)" else "CLAIM DAY $nextDayToClaim REWARD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
