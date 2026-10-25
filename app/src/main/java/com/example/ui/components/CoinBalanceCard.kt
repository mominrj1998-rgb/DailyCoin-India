package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.*

@Composable
fun CoinBalanceCard(
    user: UserEntity?,
    todayEarned: Long,
    dailyLimit: Long = 500L,
    pendingInr: Long,
    paidInr: Long,
    onWithdrawClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val coins = user?.coinBalance ?: 0L
    val inrValue = coins / 100.0

    val progress = (todayEarned.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(600), label = "prog")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("coin_balance_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            CoinGold.copy(alpha = 0.6f),
                            TgBlueLight.copy(alpha = 0.4f),
                            Color(0xFF334155)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // Top Header Row: Balance Label & Conversion Rate Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(CoinGoldLight, CoinGoldDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₹",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "TOTAL BALANCE",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = "100 Coins = ₹1",
                            color = CoinGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Coins & INR Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "%,d".format(coins),
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Coins",
                                color = CoinGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "≈ ₹%.2f INR".format(inrValue),
                            color = SuccessGreenLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Quick Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onHistoryClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF334155),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("btn_quick_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "History",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Button(
                            onClick = onWithdrawClick,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("btn_quick_withdraw")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Withdraw",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Withdraw",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Today's Earning & Daily Limit Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Earned: $todayEarned Coins",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Limit: $dailyLimit Coins",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CoinGold,
                    trackColor = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Withdrawal Status Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pending Withdrawal Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (pendingInr > 0) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (pendingInr > 0) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color(0xFF334155)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Pending",
                                tint = if (pendingInr > 0) CoinGold else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "Pending",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "₹$pendingInr",
                                    color = if (pendingInr > 0) CoinGoldLight else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Total Paid Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Paid",
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "Paid to UPI",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "₹$paidInr",
                                    color = SuccessGreenLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
