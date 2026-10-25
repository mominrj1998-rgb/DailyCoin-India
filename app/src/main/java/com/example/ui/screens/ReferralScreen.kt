package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ReferralEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*

@Composable
fun ReferralScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    referrals: List<ReferralEntity>,
    qualifiedCount: Int,
    onSubmitReferralCode: (String) -> Unit,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenBotSpec: () -> Unit
) {
    val context = LocalContext.current
    val refCode = user?.referralCode ?: "INR7892"
    val telegramDeepLink = "https://t.me/INREarn_Bot?start=ref_$refCode"

    var inputCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            isOwner = isOwner,
            title = "👥 Referral Program",
            onBack = onBack,
            onOpenAdmin = onOpenAdmin,
            onOpenBotSpec = onOpenBotSpec
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Hero Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF06B6D4).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = null,
                                tint = Color(0xFF22D3EE),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Invite Friends & Earn Real INR",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Earn 50 Coins (= ₹0.50) when your invited referral qualifies by completing 3 spins or 1 task.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = TgSurfaceElevated
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Total Invited", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text("${referrals.size}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Qualified", color = SuccessGreenLight, fontSize = 11.sp)
                                    Text("$qualifiedCount", color = SuccessGreenLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Share & Referral Link Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your Telegram Deep Link",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = TgSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = telegramDeepLink,
                                color = TgBlueLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Referral Link", telegramDeepLink)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Referral link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("btn_copy_ref_link"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Link", fontSize = 12.5.sp)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Earn genuine real INR with INR Earn Bot! Use my invite link: $telegramDeepLink")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Referral Link"))
                                },
                                modifier = Modifier.weight(1f).testTag("btn_share_ref_link"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TgBlue)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Anti-Fraud Policy Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(18.dp))
                            Text("Anti-Abuse Protections Active", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Self-referrals are cryptographically prevented.\n• Bonus is only awarded after referee passes activity qualification (at least 3 spins or 1 verified task).\n• Multi-accounting emulator farms are detected by server device hash and banned.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // List of Referred Friends
            item {
                Text(
                    text = "YOUR REFERRALS (${referrals.size})",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (referrals.isEmpty()) {
                item {
                    Text(
                        text = "No referrals yet. Share your Telegram link above to start earning!",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(referrals) { ref ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = TgSurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = ref.refereeName,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "Reward: +${ref.rewardCoins} Coins on qualification",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.5.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (ref.status) {
                                    "QUALIFIED" -> SuccessGreen.copy(alpha = 0.2f)
                                    "PAID" -> SuccessGreen.copy(alpha = 0.2f)
                                    else -> WarningOrange.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = ref.status,
                                    color = when (ref.status) {
                                        "QUALIFIED" -> SuccessGreenLight
                                        "PAID" -> SuccessGreenLight
                                        else -> WarningOrange
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
