package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WithdrawalEntity
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

fun maskUpiId(upi: String): String {
    val atIdx = upi.indexOf('@')
    if (atIdx <= 2) return upi
    val prefix = upi.take(2)
    val suffix = upi.substring(atIdx)
    return "$prefix****$suffix"
}

@Composable
fun WithdrawalScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    pendingWithdrawal: WithdrawalEntity?,
    withdrawals: List<WithdrawalEntity>,
    onRequestWithdrawal: (name: String, upiId: String, amountInr: Long, onSuccess: () -> Unit) -> Unit,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenBotSpec: () -> Unit
) {
    val currentBalance = user?.coinBalance ?: 0L
    var userNameInput by remember { mutableStateOf(user?.fullName ?: "") }
    var upiInput by remember { mutableStateOf("") }
    var selectedTierInr by remember { mutableLongStateOf(10L) }

    val tiers = listOf(
        10L to 1000L,
        30L to 3000L,
        50L to 5000L,
        100L to 10000L
    )

    val selectedCoinsRequired = selectedTierInr * 100L
    val hasSufficientBalance = currentBalance >= selectedCoinsRequired
    val hasPending = pendingWithdrawal != null

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            isOwner = isOwner,
            title = "💰 UPI Wallet & Payout",
            onBack = onBack,
            onOpenAdmin = onOpenAdmin,
            onOpenBotSpec = onOpenBotSpec
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Summary
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TgSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Available Coin Balance", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("%,d Coins".format(currentBalance), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Real INR Value", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("₹%.2f".format(currentBalance / 100.0), color = SuccessGreenLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Pending Withdrawal Alert Lock
            if (hasPending) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoinGold),
                        modifier = Modifier.fillMaxWidth().testTag("pending_withdrawal_alert")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LockClock, contentDescription = null, tint = CoinGold, modifier = Modifier.size(24.dp))
                            Column {
                                Text(
                                    text = "Pending Withdrawal in Progress",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "You already have a pending withdrawal of ₹${pendingWithdrawal.amountInr} to ${maskUpiId(pendingWithdrawal.upiId)}. Please wait for the admin to manually verify and pay before requesting another.",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Select Withdrawal Option Tier
            item {
                Text(
                    text = "SELECT WITHDRAWAL AMOUNT",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tiers.forEach { (inr, coins) ->
                        val isSelected = selectedTierInr == inr
                        val isAffordable = currentBalance >= coins

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedTierInr = inr }
                                .testTag("tier_btn_$inr"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) TgBlue.copy(alpha = 0.2f) else TgSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) TgBlueLight else TgBorderDark
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "₹$inr",
                                    color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$coins Coins",
                                    color = if (isAffordable) CoinGoldLight else Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Insufficient Balance Notice if user doesn't have enough coins
            if (!hasSufficientBalance) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ErrorRed.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().testTag("insufficient_balance_msg")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                            Text(
                                text = "You need more Coins to withdraw. (Required: $selectedCoinsRequired, Balance: $currentBalance)",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Input Form Card: Name & UPI ID
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recipient Details",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = userNameInput,
                            onValueChange = { userNameInput = it },
                            label = { Text("Account Holder Full Name") },
                            placeholder = { Text("e.g. Rahul Sharma") },
                            modifier = Modifier.fillMaxWidth().testTag("input_withdraw_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TgBlueLight,
                                unfocusedBorderColor = TgBorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = upiInput,
                            onValueChange = { upiInput = it },
                            label = { Text("UPI ID (VPA)") },
                            placeholder = { Text("e.g. yourname@oksbi or 9876543210@paytm") },
                            modifier = Modifier.fillMaxWidth().testTag("input_withdraw_upi"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TgBlueLight,
                                unfocusedBorderColor = TgBorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "• Manual Payout: The admin verifies your task completion and manually sends real INR via UPI.\n• No fake instant receipts: Status remains PENDING until the admin inputs the bank UTR reference.\n• Lock rule: Only 1 pending withdrawal request allowed at a time.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onRequestWithdrawal(userNameInput, upiInput, selectedTierInr) {
                                    upiInput = ""
                                }
                            },
                            enabled = hasSufficientBalance && !hasPending && upiInput.contains("@"),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_withdrawal"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                                disabledContainerColor = Color(0xFF334155),
                                disabledContentColor = Color(0xFF94A3B8)
                            )
                        ) {
                            Text(
                                text = if (hasPending) "WITHDRAWAL LOCKED (REQUEST PENDING)" else "REQUEST ₹$selectedTierInr WITHDRAWAL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Withdrawal History Section
            item {
                Text(
                    text = "WITHDRAWAL HISTORY",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (withdrawals.isEmpty()) {
                item {
                    Text(
                        text = "No withdrawal requests yet.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(withdrawals) { w ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().testTag("withdrawal_item_${w.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = TgSurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("₹${w.amountInr}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    Text("(${w.coinsUsed} Coins)", color = CoinGold, fontSize = 12.sp)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (w.status) {
                                        "PAID" -> SuccessGreen.copy(alpha = 0.2f)
                                        "APPROVED" -> TgBlue.copy(alpha = 0.2f)
                                        "REJECTED" -> ErrorRed.copy(alpha = 0.2f)
                                        else -> WarningOrange.copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = w.status,
                                        color = when (w.status) {
                                            "PAID" -> SuccessGreenLight
                                            "APPROVED" -> TgBlueLight
                                            "REJECTED" -> ErrorRed
                                            else -> WarningOrange
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("UPI ID: ${maskUpiId(w.upiId)} (${w.userName})", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("Requested: ${dateFormatter.format(Date(w.requestDate))}", color = Color(0xFF94A3B8), fontSize = 11.sp)

                            if (!w.paymentReference.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Payment UTR / Ref: ${w.paymentReference}", color = SuccessGreenLight, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (!w.adminNote.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Admin Note: ${w.adminNote}", color = Color(0xFFE2E8F0), fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
