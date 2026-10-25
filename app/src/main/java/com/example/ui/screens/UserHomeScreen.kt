package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.components.CoinBalanceCard
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

data class HomeActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBg: Color,
    val screen: AppScreen,
    val badge: String? = null
)

@Composable
fun UserHomeScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    todayEarned: Long,
    pendingInr: Long,
    paidInr: Long,
    remainingSpins: Int,
    onNavigate: (AppScreen) -> Unit
) {
    val menuItems = listOf(
        HomeActionItem(
            title = "1. Free Spin",
            subtitle = "$remainingSpins/100 spins today",
            icon = Icons.Default.Casino,
            iconBg = Color(0xFFF59E0B),
            screen = AppScreen.FREE_SPIN,
            badge = "HOT"
        ),
        HomeActionItem(
            title = "2. Math/Pizza",
            subtitle = "Solve & earn coins",
            icon = Icons.Default.LocalPizza,
            iconBg = Color(0xFFEF4444),
            screen = AppScreen.MATH_PIZZA
        ),
        HomeActionItem(
            title = "3. Watch Ads",
            subtitle = "Rewarded ad (Demo)",
            icon = Icons.Default.LiveTv,
            iconBg = Color(0xFF3B82F6),
            screen = AppScreen.WATCH_ADS,
            badge = "DEMO"
        ),
        HomeActionItem(
            title = "4. Daily Bonus",
            subtitle = "7-Day streak rewards",
            icon = Icons.Default.CardGiftcard,
            iconBg = Color(0xFF10B981),
            screen = AppScreen.DAILY_BONUS
        ),
        HomeActionItem(
            title = "5. Daily Tasks",
            subtitle = "Complete & verify",
            icon = Icons.Default.CheckCircle,
            iconBg = Color(0xFF8B5CF6),
            screen = AppScreen.DAILY_TASKS
        ),
        HomeActionItem(
            title = "6. Referral",
            subtitle = "+50 Coins/qualified",
            icon = Icons.Default.GroupAdd,
            iconBg = Color(0xFF06B6D4),
            screen = AppScreen.REFERRAL
        ),
        HomeActionItem(
            title = "7. Withdraw",
            subtitle = "UPI manual payout",
            icon = Icons.Default.AccountBalanceWallet,
            iconBg = Color(0xFF059669),
            screen = AppScreen.WITHDRAW
        ),
        HomeActionItem(
            title = "8. Ledger History",
            subtitle = "All coin transactions",
            icon = Icons.Default.ReceiptLong,
            iconBg = Color(0xFF6366F1),
            screen = AppScreen.TRANSACTIONS
        ),
        HomeActionItem(
            title = "9. Help & FAQ",
            subtitle = "Rules & payout info",
            icon = Icons.Default.HelpOutline,
            iconBg = Color(0xFF64748B),
            screen = AppScreen.HELP
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            isOwner = isOwner,
            onOpenAdmin = { onNavigate(AppScreen.ADMIN_PANEL) },
            onOpenBotSpec = { onNavigate(AppScreen.BOT_SPEC) }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Full Width Coin Balance Card
            item(span = { GridItemSpan(2) }) {
                CoinBalanceCard(
                    user = user,
                    todayEarned = todayEarned,
                    pendingInr = pendingInr,
                    paidInr = paidInr,
                    onWithdrawClick = { onNavigate(AppScreen.WITHDRAW) },
                    onHistoryClick = { onNavigate(AppScreen.TRANSACTIONS) }
                )
            }

            // Trust & Policy Notice
            item(span = { GridItemSpan(2) }) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SuccessGreenLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Legitimate rewards system: 100 Coins = ₹1. No fake payments or automated illusions. Admin manually verifies and transfers real INR to your UPI.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Section Title
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "EARNING & WALLET MODULES",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                )
            }

            // 9 Main Cards
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onNavigate(item.screen) }
                        .testTag("home_card_${item.screen.name.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(item.iconBg.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.iconBg,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            if (item.badge != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = item.iconBg.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = item.badge,
                                        color = item.iconBg,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = item.subtitle,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
