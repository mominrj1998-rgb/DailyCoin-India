package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.INREarnBotTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.EarnBotViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: EarnBotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            INREarnBotTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                // Collect UI snackbars/toast messages
                LaunchedEffect(Unit) {
                    viewModel.uiMessage.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Collect states
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val isOwner by viewModel.isOwner.collectAsStateWithLifecycle()
                val user by viewModel.user.collectAsStateWithLifecycle()
                val todayEarned by viewModel.todayEarnedCoins.collectAsStateWithLifecycle()
                val dailySpinsUsed by viewModel.dailySpinsCount.collectAsStateWithLifecycle()
                val isSpinning by viewModel.isSpinning.collectAsStateWithLifecycle()
                val dailyGamesUsed by viewModel.dailyGamesCount.collectAsStateWithLifecycle()
                val dailyAdsUsed by viewModel.dailyAdsCount.collectAsStateWithLifecycle()
                val lastDailyReward by viewModel.lastDailyReward.collectAsStateWithLifecycle()
                val tasks by viewModel.enabledTasks.collectAsStateWithLifecycle()
                val userTasks by viewModel.userTasks.collectAsStateWithLifecycle()
                val referrals by viewModel.referrals.collectAsStateWithLifecycle()
                val qualifiedCount by viewModel.qualifiedReferralsCount.collectAsStateWithLifecycle()
                val userWithdrawals by viewModel.userWithdrawals.collectAsStateWithLifecycle()
                val pendingWithdrawal by viewModel.pendingWithdrawal.collectAsStateWithLifecycle()
                val userPaidInr by viewModel.userPaidInr.collectAsStateWithLifecycle()
                val userPendingInr by viewModel.userPendingInr.collectAsStateWithLifecycle()

                // Admin states
                val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
                val allWithdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()
                val pendingCount by viewModel.pendingWithdrawalsCount.collectAsStateWithLifecycle()
                val totalPaidInr by viewModel.totalPaidInr.collectAsStateWithLifecycle()
                val totalCoinsIssued by viewModel.totalCoinsIssued.collectAsStateWithLifecycle()
                val adminLogs by viewModel.adminLogs.collectAsStateWithLifecycle()
                val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

                // Hardware back press handler
                if (currentScreen != AppScreen.HOME) {
                    BackHandler {
                        viewModel.navigateBack()
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            AppScreen.HOME -> UserHomeScreen(
                                user = user,
                                isOwner = isOwner,
                                todayEarned = todayEarned,
                                pendingInr = userPendingInr,
                                paidInr = userPaidInr,
                                remainingSpins = (100 - dailySpinsUsed).coerceAtLeast(0),
                                onNavigate = { viewModel.navigateTo(it) }
                            )

                            AppScreen.FREE_SPIN -> FreeSpinScreen(
                                user = user,
                                isOwner = isOwner,
                                dailySpinsUsed = dailySpinsUsed,
                                isSpinning = isSpinning,
                                onSpinRequested = { callback -> viewModel.spinWheel(callback) },
                                onBack = { viewModel.navigateBack() },
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                                onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                            )

                            AppScreen.MATH_PIZZA -> MathPizzaScreen(
                                user = user,
                                isOwner = isOwner,
                                dailyGamesUsed = dailyGamesUsed,
                                onSubmitAnswer = { q, correct, callback ->
                                    viewModel.submitMathAnswer(q, correct, callback)
                                },
                                onBack = { viewModel.navigateBack() },
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                                onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                            )

                            AppScreen.WATCH_ADS -> WatchAdsScreen(
                                user = user,
                                isOwner = isOwner,
                                dailyAdsUsed = dailyAdsUsed,
                                onCompleteAd = { callback -> viewModel.completeRewardedAd(callback) },
                                onBack = { viewModel.navigateBack() },
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                                onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                            )

                            AppScreen.DAILY_BONUS -> DailyBonusScreen(
                                user = user,
                                isOwner = isOwner,
                                lastReward = lastDailyReward,
                                onClaimBonus = { callback -> viewModel.claimDailyBonus(callback) },
                                onBack = { viewModel.navigateBack() },
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                                onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                            )

                            AppScreen.DAILY_TASKS -> DailyTasksScreen(
                                user = user,
                                isOwner = isOwner,
                                tasks = tasks,
                                userTasks = userTasks,
                                onStartTask = { viewModel.startTaskVerification(it) },
                                onClaimTask = { viewModel.claimTaskReward(it) },
                                onBack = { viewModel.navigateBack() },
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                                onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                            )

                            AppScreen.REFERRAL -> ReferralScreen(
                                user = user,
                                isOwner = isOwner,
                                referrals = referrals,
                                qualifiedCount = qualifiedCount,
                                onSubmitReferralCode = { viewModel.submitReferralCode(it) },
                                onBack = { viewModel.navigateBack() },
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                                onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                            )

                            AppScreen.WITHDRAW -> WithdrawalScreen(
                                user = user,
                                isOwner = isOwner,
                                pendingWithdrawal = pendingWithdrawal,
                                withdrawals = userWithdrawals,
                                onRequestWithdrawal = { name, upi, amt, success ->
                                    viewModel.requestWithdrawal(name, upi, amt, success)
                                },
                                onBack = { viewModel.navigateBack() },
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                                onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                            )

                            AppScreen.TRANSACTIONS -> {
                                val txList by viewModel.transactions.collectAsStateWithLifecycle()
                                TransactionsScreen(
                                    user = user,
                                    isOwner = isOwner,
                                    transactions = txList,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                                    onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                                )
                            }

                            AppScreen.HELP -> HelpFaqScreen(
                                user = user,
                                isOwner = isOwner,
                                onBack = { viewModel.navigateBack() },
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                                onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                            )

                            AppScreen.ADMIN_PANEL -> AdminDashboardScreen(
                                user = user,
                                allUsers = allUsers,
                                allWithdrawals = allWithdrawals,
                                pendingCount = pendingCount,
                                totalPaidInr = totalPaidInr,
                                totalCoinsIssued = totalCoinsIssued,
                                adminLogs = adminLogs,
                                allTasks = allTasks,
                                onApproveWithdrawal = { viewModel.approveWithdrawal(it) },
                                onMarkPaid = { id, utr, note -> viewModel.markWithdrawalPaid(id, utr, note) },
                                onRejectWithdrawal = { id, reason -> viewModel.rejectWithdrawal(id, reason) },
                                onAdjustCoins = { target, delta, reason -> viewModel.adjustUserCoins(target, delta, reason) },
                                onToggleBan = { target, ban -> viewModel.toggleUserBan(target, ban) },
                                onUpdateConfig = { k, v -> viewModel.updateConfig(k, v) },
                                onSaveTask = { viewModel.saveTask(it) },
                                onDeleteTask = { viewModel.deleteTask(it) },
                                onBack = { viewModel.navigateBack() },
                                onOpenBotSpec = { viewModel.navigateTo(AppScreen.BOT_SPEC) }
                            )

                            AppScreen.BOT_SPEC -> BotSpecScreen(
                                user = user,
                                isOwner = isOwner,
                                onBack = { viewModel.navigateBack() },
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) }
                            )
                        }
                    }
                }
            }
        }
    }
}
