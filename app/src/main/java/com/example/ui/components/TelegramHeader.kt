package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.*

@Composable
fun TelegramHeader(
    user: UserEntity?,
    isOwner: Boolean = false,
    onOpenAdmin: () -> Unit,
    onOpenBotSpec: () -> Unit,
    onBack: (() -> Unit)? = null,
    title: String = "INR Earn Bot"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TgBgDark)
    ) {
        // Telegram Mini App System Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("nav_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TgBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Bot",
                            tint = TgBlueLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "bot",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Actions: Admin & Bot Architecture
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bot Spec / Code button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TgSurfaceElevated,
                    modifier = Modifier
                        .clickable { onOpenBotSpec() }
                        .testTag("header_bot_spec_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Bot Spec",
                            tint = TgBlueLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "API Spec",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Admin Dashboard toggle (Strictly rendered only for verified bot owner)
                if (isOwner) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF7C3AED).copy(alpha = 0.25f),
                        modifier = Modifier
                            .clickable { onOpenAdmin() }
                            .testTag("header_admin_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Admin",
                                color = Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Telegram WebApp user sub-bar if available
        if (user != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF374151)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(1),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${user.fullName} (${user.username})",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isOwner) Color(0xFF7C3AED).copy(alpha = 0.2f) else if (user.isBanned) ErrorRed.copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isOwner) "VERIFIED OWNER" else if (user.isBanned) "BANNED" else "VIP EARNER",
                        color = if (isOwner) Color(0xFFA78BFA) else if (user.isBanned) ErrorRed else SuccessGreenLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = TgBorderDark,
            thickness = 1.dp
        )
    }
}
