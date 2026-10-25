package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.*
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
    user: UserEntity?,
    allUsers: List<UserEntity>,
    allWithdrawals: List<WithdrawalEntity>,
    pendingCount: Int,
    totalPaidInr: Long,
    totalCoinsIssued: Long,
    adminLogs: List<AdminLogEntity>,
    allTasks: List<TaskEntity>,
    onApproveWithdrawal: (Long) -> Unit,
    onMarkPaid: (Long, String, String?) -> Unit,
    onRejectWithdrawal: (Long, String) -> Unit,
    onAdjustCoins: (targetUserId: String, delta: Long, reason: String) -> Unit,
    onToggleBan: (targetUserId: String, ban: Boolean) -> Unit,
    onUpdateConfig: (key: String, value: String) -> Unit,
    onSaveTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onBack: () -> Unit,
    onOpenBotSpec: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Withdrawals, 2: Users, 3: Settings, 4: Tasks, 5: Logs

    // Dialog States
    var markPaidWithdrawalId by remember { mutableStateOf<Long?>(null) }
    var utrInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    var rejectWithdrawalId by remember { mutableStateOf<Long?>(null) }
    var rejectReasonInput by remember { mutableStateOf("") }

    var adjustUserTarget by remember { mutableStateOf<UserEntity?>(null) }
    var adjustAmountInput by remember { mutableStateOf("") }
    var adjustReasonInput by remember { mutableStateOf("") }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskReward by remember { mutableStateOf("30") }
    var newTaskUrl by remember { mutableStateOf("https://t.me/INR_Earn") }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            title = "🛡️ Admin Control Panel",
            onBack = onBack,
            onOpenAdmin = { /* Already in admin */ },
            onOpenBotSpec = onOpenBotSpec
        )

        // Admin Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = TgSurfaceDark,
            contentColor = Color.White,
            edgePadding = 12.dp
        ) {
            listOf("Overview", "Withdrawals ($pendingCount)", "Users (${allUsers.size})", "Game Settings", "Tasks", "Audit Logs").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> { // Overview KPIs
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("ADMIN METRICS OVERVIEW", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AdminKpiCard("Total Users", "${allUsers.size}", Icons.Default.People, Color(0xFF3B82F6), modifier = Modifier.weight(1f))
                                AdminKpiCard("Active Users", "${allUsers.count { !it.isBanned }}", Icons.Default.CheckCircle, SuccessGreen, modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AdminKpiCard("Coins Issued", "%,d".format(totalCoinsIssued), Icons.Default.MonetizationOn, CoinGold, modifier = Modifier.weight(1f))
                                AdminKpiCard("Total Paid INR", "₹%,d".format(totalPaidInr), Icons.Default.CurrencyRupee, SuccessGreenLight, modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AdminKpiCard("Withdrawal Requests", "${allWithdrawals.size}", Icons.Default.Receipt, Color(0xFF8B5CF6), modifier = Modifier.weight(1f))
                                AdminKpiCard("Pending Payouts", "$pendingCount", Icons.Default.HourglassTop, if (pendingCount > 0) WarningOrange else Color(0xFF64748B), modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Manual UPI Payout SOP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "1. Navigate to 'Withdrawals' tab and review PENDING requests.\n2. Open your real UPI banking app (Google Pay, PhonePe, Paytm, BHIM).\n3. Send the exact INR amount to the user's UPI ID.\n4. Click 'Mark as PAID' and enter the genuine 12-digit UPI Reference Number / UTR.\n5. Status switches to PAID and user's ledger is finalized. Never mark PAID without real transfer.",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> { // Withdrawal Management
                    var filterState by remember { mutableStateOf("ALL") }
                    val filteredW = when (filterState) {
                        "PENDING" -> allWithdrawals.filter { it.status == "PENDING" }
                        "PAID" -> allWithdrawals.filter { it.status == "PAID" }
                        "REJECTED" -> allWithdrawals.filter { it.status == "REJECTED" }
                        else -> allWithdrawals
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("ALL", "PENDING", "PAID", "REJECTED").forEach { f ->
                                    FilterChip(
                                        selected = filterState == f,
                                        onClick = { filterState = f },
                                        label = { Text(f, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        if (filteredW.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No withdrawals match this filter.", color = Color(0xFF64748B))
                                }
                            }
                        } else {
                            items(filteredW) { w ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("admin_withdrawal_${w.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("₹${w.amountInr}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                                Text("(${w.coinsUsed} Coins)", color = CoinGold, fontSize = 12.sp)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (w.status) {
                                                    "PAID" -> SuccessGreen.copy(alpha = 0.2f)
                                                    "PENDING" -> WarningOrange.copy(alpha = 0.2f)
                                                    "REJECTED" -> ErrorRed.copy(alpha = 0.2f)
                                                    else -> TgBlue.copy(alpha = 0.2f)
                                                }
                                            ) {
                                                Text(
                                                    text = w.status,
                                                    color = when (w.status) {
                                                        "PAID" -> SuccessGreenLight
                                                        "PENDING" -> WarningOrange
                                                        "REJECTED" -> ErrorRed
                                                        else -> TgBlueLight
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("User: ${w.userName} (${w.userId})", color = Color(0xFFCBD5E1), fontSize = 12.5.sp)
                                        Text("UPI ID: ${w.upiId}", color = TgBlueLight, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Date: ${dateFormatter.format(Date(w.requestDate))}", color = Color(0xFF94A3B8), fontSize = 11.sp)

                                        if (!w.paymentReference.isNullOrBlank()) {
                                            Text("UTR: ${w.paymentReference}", color = SuccessGreenLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (!w.adminNote.isNullOrBlank()) {
                                            Text("Note: ${w.adminNote}", color = Color(0xFFE2E8F0), fontSize = 11.5.sp)
                                        }

                                        if (w.status == "PENDING" || w.status == "APPROVED") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (w.status == "PENDING") {
                                                    Button(
                                                        onClick = { onApproveWithdrawal(w.id) },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = TgBlue),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Text("Approve", fontSize = 12.sp)
                                                    }
                                                }

                                                Button(
                                                    onClick = {
                                                        markPaidWithdrawalId = w.id
                                                        utrInput = ""
                                                        noteInput = ""
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Mark as PAID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        rejectWithdrawalId = w.id
                                                        rejectReasonInput = ""
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Reject & Refund", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> { // User Management
                    var searchQuery by remember { mutableStateOf("") }
                    val searchedUsers = allUsers.filter {
                        it.username.contains(searchQuery, ignoreCase = true) ||
                                it.fullName.contains(searchQuery, ignoreCase = true) ||
                                it.id.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search by name, @username, or user ID") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        items(searchedUsers) { u ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("user_row_${u.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(u.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${u.username} • ID: ${u.id}", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (u.isBanned) ErrorRed.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (u.isBanned) "BANNED" else "ACTIVE",
                                                color = if (u.isBanned) ErrorRed else SuccessGreenLight,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Balance: %,d Coins".format(u.coinBalance), color = CoinGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Total Earned: %,d".format(u.totalEarned), color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                        Text("Withdrawn: ₹${u.totalWithdrawnInr}", color = SuccessGreenLight, fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                adjustUserTarget = u
                                                adjustAmountInput = ""
                                                adjustReasonInput = ""
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Adjust Coins", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { onToggleBan(u.id, !u.isBanned) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (u.isBanned) SuccessGreen else ErrorRed
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(if (u.isBanned) "Unban User" else "Ban User", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> { // Game Settings
                    var spinLimit by remember { mutableStateOf("100") }
                    var mathReward by remember { mutableStateOf("5") }
                    var dailyCap by remember { mutableStateOf("500") }
                    var refReward by remember { mutableStateOf("50") }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("GAME & REWARD CONFIGURATION", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = spinLimit,
                                        onValueChange = { spinLimit = it },
                                        label = { Text("Daily Spin Limit (Spins/Day)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = mathReward,
                                        onValueChange = { mathReward = it },
                                        label = { Text("Math Pizza Challenge Reward (Coins)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = dailyCap,
                                        onValueChange = { dailyCap = it },
                                        label = { Text("Daily Earning Cap (Coins)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = refReward,
                                        onValueChange = { refReward = it },
                                        label = { Text("Referral Bonus on Qualification (Coins)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            onUpdateConfig("daily_spin_limit", spinLimit)
                                            onUpdateConfig("math_reward", mathReward)
                                            onUpdateConfig("daily_earning_limit", dailyCap)
                                            onUpdateConfig("referral_reward", refReward)
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TgBlue)
                                    ) {
                                        Text("Save All Settings", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> { // Tasks Manager
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TASK MANAGEMENT", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { showAddTaskDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Task", fontSize = 12.sp)
                                }
                            }
                        }

                        items(allTasks) { t ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(t.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(t.description, color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                                        Text("Reward: +${t.rewardCoins} Coins", color = CoinGoldLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Switch(
                                            checked = t.isEnabled,
                                            onCheckedChange = { checked ->
                                                onSaveTask(t.copy(isEnabled = checked))
                                            }
                                        )
                                        IconButton(onClick = { onDeleteTask(t) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> { // Audit Logs
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("ADMIN ACTION AUDIT LOGS", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (adminLogs.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No admin actions logged yet.", color = Color(0xFF64748B))
                                }
                            }
                        } else {
                            items(adminLogs) { log ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = TgSurfaceDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(log.actionType, color = TgBlueLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(dateFormatter.format(Date(log.createdAt)), color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(log.details, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Mark Paid Dialog
    if (markPaidWithdrawalId != null) {
        AlertDialog(
            onDismissRequest = { markPaidWithdrawalId = null },
            title = { Text("Mark Withdrawal #$markPaidWithdrawalId as PAID") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter real UPI UTR / Transaction Reference number after sending payment:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = utrInput,
                        onValueChange = { utrInput = it },
                        label = { Text("Bank UPI Ref / UTR (12 digits)") },
                        placeholder = { Text("e.g. 428492019482") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Optional Admin Note") },
                        placeholder = { Text("e.g. Sent via GPay") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = markPaidWithdrawalId!!
                        markPaidWithdrawalId = null
                        onMarkPaid(id, utrInput, noteInput.ifBlank { null })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Confirm Real Payout")
                }
            },
            dismissButton = {
                TextButton(onClick = { markPaidWithdrawalId = null }) { Text("Cancel") }
            }
        )
    }

    // Reject Dialog
    if (rejectWithdrawalId != null) {
        AlertDialog(
            onDismissRequest = { rejectWithdrawalId = null },
            title = { Text("Reject Withdrawal #$rejectWithdrawalId") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter rejection reason (Coins will be automatically refunded to user's ledger):", fontSize = 12.sp)
                    OutlinedTextField(
                        value = rejectReasonInput,
                        onValueChange = { rejectReasonInput = it },
                        label = { Text("Rejection Reason") },
                        placeholder = { Text("e.g. Invalid UPI ID, Name mismatch") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = rejectWithdrawalId!!
                        rejectWithdrawalId = null
                        onRejectWithdrawal(id, rejectReasonInput.ifBlank { "Policy violation or invalid UPI" })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Reject & Refund")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectWithdrawalId = null }) { Text("Cancel") }
            }
        )
    }

    // Adjust Coins Dialog
    if (adjustUserTarget != null) {
        val target = adjustUserTarget!!
        AlertDialog(
            onDismissRequest = { adjustUserTarget = null },
            title = { Text("Adjust Coins for ${target.username}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Balance: ${target.coinBalance} Coins", fontSize = 12.sp)
                    OutlinedTextField(
                        value = adjustAmountInput,
                        onValueChange = { adjustAmountInput = it },
                        label = { Text("Coin Delta (+/- number)") },
                        placeholder = { Text("e.g. +100 or -50") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = adjustReasonInput,
                        onValueChange = { adjustReasonInput = it },
                        label = { Text("Mandatory Audit Reason") },
                        placeholder = { Text("e.g. Contest winner or penalty") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val delta = adjustAmountInput.toLongOrNull() ?: 0L
                        if (delta != 0L && adjustReasonInput.isNotBlank()) {
                            onAdjustCoins(target.id, delta, adjustReasonInput)
                            adjustUserTarget = null
                        }
                    }
                ) {
                    Text("Apply Adjustment")
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustUserTarget = null }) { Text("Cancel") }
            }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Create New Daily Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = { newTaskDesc = it },
                        label = { Text("Description") }
                    )
                    OutlinedTextField(
                        value = newTaskReward,
                        onValueChange = { newTaskReward = it },
                        label = { Text("Reward (Coins)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newTaskUrl,
                        onValueChange = { newTaskUrl = it },
                        label = { Text("Telegram / Web URL") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            val t = TaskEntity(
                                id = "task_${System.currentTimeMillis()}",
                                title = newTaskTitle.trim(),
                                description = newTaskDesc.trim(),
                                rewardCoins = newTaskReward.toLongOrNull() ?: 30L,
                                actionUrl = newTaskUrl.trim(),
                                iconName = "check_circle",
                                isEnabled = true
                            )
                            onSaveTask(t)
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("Save Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminKpiCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
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
                Text(title, color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
