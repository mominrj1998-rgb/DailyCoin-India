package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    transactions: List<TransactionEntity>,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenBotSpec: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, EARNED, SPENT

    val filtered = when (selectedFilter) {
        "EARNED" -> transactions.filter { it.amount > 0 }
        "SPENT" -> transactions.filter { it.amount < 0 }
        else -> transactions
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            isOwner = isOwner,
            title = "📜 Coin Ledger History",
            onBack = onBack,
            onOpenAdmin = onOpenAdmin,
            onOpenBotSpec = onOpenBotSpec
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "EARNED", "SPENT").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TgBlue,
                            selectedLabelColor = Color.White,
                            containerColor = TgSurfaceDark,
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) TgBlueLight else TgBorderDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions found for this filter.", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { tx ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tx_item_${tx.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = TgSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (tx.type) {
                                                "SPIN" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                "MATH" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                "AD" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                                "DAILY_BONUS" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                "TASK" -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                                "REFERRAL" -> Color(0xFF06B6D4).copy(alpha = 0.2f)
                                                "WITHDRAWAL" -> Color(0xFF6B7280).copy(alpha = 0.2f)
                                                else -> TgSurfaceElevated
                                            }
                                        ) {
                                            Text(
                                                text = tx.type,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "#${tx.id}",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = tx.description,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = dateFormatter.format(Date(tx.createdAt)),
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (tx.amount > 0) "+${tx.amount}" else "${tx.amount}",
                                        color = if (tx.amount > 0) SuccessGreenLight else ErrorRed,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Bal: ${tx.balanceAfter}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
