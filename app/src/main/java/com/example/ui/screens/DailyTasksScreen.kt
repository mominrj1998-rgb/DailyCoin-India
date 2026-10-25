package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserTaskEntity
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*

@Composable
fun DailyTasksScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    tasks: List<TaskEntity>,
    userTasks: List<UserTaskEntity>,
    onStartTask: (taskId: String) -> Unit,
    onClaimTask: (taskId: String) -> Unit,
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
            title = "✅ Daily Task Center",
            onBack = onBack,
            onOpenAdmin = onOpenAdmin,
            onOpenBotSpec = onOpenBotSpec
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = TgBlueLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Verified Completion Required",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Click 'Start', perform the task in Telegram/Browser, wait for automatic verification, and click 'Claim'.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            items(tasks) { task ->
                val userTask = userTasks.firstOrNull { it.taskId == task.id }
                val isClaimed = userTask?.status == "CLAIMED"
                val isVerifying = userTask?.status == "VERIFYING"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_item_${task.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isClaimed) SuccessGreen.copy(alpha = 0.4f) else TgBorderDark
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CoinGold.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CoinGold.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "+${task.rewardCoins} Coins",
                                    color = CoinGoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Status & Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when {
                                    isClaimed -> "Status: Claimed ✅"
                                    isVerifying -> "Status: Verifying... ⏳"
                                    else -> "Status: Not Started"
                                },
                                color = when {
                                    isClaimed -> SuccessGreenLight
                                    isVerifying -> CoinGold
                                    else -> Color(0xFF94A3B8)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            when {
                                isClaimed -> {
                                    FilledTonalButton(
                                        onClick = {},
                                        enabled = false,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            disabledContainerColor = SuccessGreen.copy(alpha = 0.15f),
                                            disabledContentColor = SuccessGreenLight
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Done", fontSize = 12.sp)
                                    }
                                }
                                isVerifying -> {
                                    Button(
                                        onClick = { onClaimTask(task.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Claim Reward", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { onStartTask(task.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TgBlue),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Start Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
