package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.EarnBotRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    FREE_SPIN,
    MATH_PIZZA,
    WATCH_ADS,
    DAILY_BONUS,
    DAILY_TASKS,
    REFERRAL,
    WITHDRAW,
    TRANSACTIONS,
    HELP,
    ADMIN_PANEL,
    BOT_SPEC
}

class EarnBotViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = EarnBotRepository(database)

    private val _currentUserId = MutableStateFlow("usr_rahul_101")
    val currentUserId = _currentUserId.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<AppScreen>()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning = _isSpinning.asStateFlow()

    private val _lastSpinResult = MutableStateFlow<Long?>(null)
    val lastSpinResult = _lastSpinResult.asStateFlow()

    // Active User Data
    val user: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        repository.getUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<TransactionEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayEarnedCoins: StateFlow<Long> = _currentUserId.flatMapLatest { id ->
        repository.getTodayEarnedCoins(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val dailySpinsCount: StateFlow<Int> = _currentUserId.flatMapLatest { id ->
        repository.observeDailySpins(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailyGamesCount: StateFlow<Int> = _currentUserId.flatMapLatest { id ->
        repository.observeDailyGames(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailyAdsCount: StateFlow<Int> = _currentUserId.flatMapLatest { id ->
        repository.observeDailyAds(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lastDailyReward: StateFlow<DailyRewardEntity?> = _currentUserId.flatMapLatest { id ->
        repository.observeLastDailyReward(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val enabledTasks: StateFlow<List<TaskEntity>> = repository.getEnabledTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTasks: StateFlow<List<UserTaskEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getUserTasks(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referrals: StateFlow<List<ReferralEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getReferralsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val qualifiedReferralsCount: StateFlow<Int> = _currentUserId.flatMapLatest { id ->
        repository.getQualifiedReferralsCount(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userWithdrawals: StateFlow<List<WithdrawalEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getWithdrawalsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingWithdrawal: StateFlow<WithdrawalEntity?> = _currentUserId.flatMapLatest { id ->
        repository.observePendingWithdrawal(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userPaidInr: StateFlow<Long> = _currentUserId.flatMapLatest { id ->
        repository.getUserPaidInr(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val userPendingInr: StateFlow<Long> = _currentUserId.flatMapLatest { id ->
        repository.getUserPendingInr(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val isOwner: StateFlow<Boolean> = _currentUserId.flatMapLatest { id ->
        flow { emit(repository.isOwner(id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Admin Panel Data
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.getAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingWithdrawalsCount: StateFlow<Int> = repository.getPendingWithdrawalsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPaidInr: StateFlow<Long> = repository.getTotalPaidInr()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalCoinsIssued: StateFlow<Long> = repository.getTotalCoinsIssued()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val adminLogs: StateFlow<List<AdminLogEntity>> = repository.getAdminLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureInitialData(_currentUserId.value)
        }
    }

    // --- Navigation (Strictly guarded) ---
    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.ADMIN_PANEL) {
            viewModelScope.launch {
                val authorized = repository.isOwner(_currentUserId.value)
                if (!authorized) {
                    _uiMessage.emit("⛔ Access Denied: Admin Panel is restricted to the verified bot owner.")
                    return@launch
                }
                if (_currentScreen.value != screen) {
                    _screenHistory.add(_currentScreen.value)
                    _currentScreen.value = screen
                }
            }
            return
        }

        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (_screenHistory.isNotEmpty()) {
            _currentScreen.value = _screenHistory.removeAt(_screenHistory.lastIndex)
            true
        } else if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            true
        } else {
            false
        }
    }

    // --- Actions ---
    fun spinWheel(onSpinReady: (rewardWon: Long, onAnimationComplete: () -> Unit) -> Unit) {
        if (_isSpinning.value) return
        _isSpinning.value = true
        _lastSpinResult.value = null

        viewModelScope.launch {
            val result = repository.determineSpinReward(_currentUserId.value)
            result.onSuccess { reward ->
                onSpinReady(reward) {
                    // Executed strictly AFTER the wheel animation has completely stopped
                    viewModelScope.launch {
                        val creditResult = repository.creditSpinReward(_currentUserId.value, reward)
                        _isSpinning.value = false
                        creditResult.onSuccess {
                            _lastSpinResult.value = reward
                            _uiMessage.emit("🎰 You won $reward Coins!")
                        }.onFailure { err ->
                            _uiMessage.emit(err.message ?: "Failed to credit coins")
                        }
                    }
                }
            }.onFailure { err ->
                _isSpinning.value = false
                _uiMessage.emit(err.message ?: "Spin failed")
            }
        }
    }

    fun submitMathAnswer(question: String, isCorrect: Boolean, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            val result = repository.submitMathAnswer(_currentUserId.value, question, isCorrect)
            result.onSuccess { reward ->
                onResult(reward)
                if (isCorrect) {
                    _uiMessage.emit("🍕 Correct! +$reward Coins credited.")
                } else {
                    _uiMessage.emit("❌ Wrong answer. Try the next pizza problem!")
                }
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Challenge error")
            }
        }
    }

    fun completeRewardedAd(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val result = repository.completeRewardedAd(_currentUserId.value)
            result.onSuccess { reward ->
                onSuccess(reward)
                _uiMessage.emit("📺 Demo Ad completed! +$reward Coins credited to your ledger.")
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Failed to claim ad reward")
            }
        }
    }

    fun claimDailyBonus(onSuccess: (Int, Long) -> Unit) {
        viewModelScope.launch {
            val result = repository.claimDailyBonus(_currentUserId.value)
            result.onSuccess { (day, coins) ->
                onSuccess(day, coins)
                _uiMessage.emit("🎁 Day $day Bonus Claimed! +$coins Coins added to balance.")
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Bonus claim error")
            }
        }
    }

    fun startTaskVerification(taskId: String) {
        viewModelScope.launch {
            val res = repository.startTaskVerification(_currentUserId.value, taskId)
            res.onSuccess {
                _uiMessage.emit("Task started. Verification in progress (approx 5s)...")
            }
        }
    }

    fun claimTaskReward(taskId: String) {
        viewModelScope.launch {
            val result = repository.claimTaskReward(_currentUserId.value, taskId)
            result.onSuccess { reward ->
                _uiMessage.emit("✅ Task Reward Claimed! +$reward Coins credited.")
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Could not claim task")
            }
        }
    }

    fun submitReferralCode(code: String) {
        viewModelScope.launch {
            val result = repository.registerReferral(_currentUserId.value, user.value?.fullName ?: "User", code.trim())
            result.onSuccess {
                _uiMessage.emit("🎉 Referral code applied! When you complete 3 spins, your inviter will receive 50 Coins.")
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Referral code invalid")
            }
        }
    }

    fun requestWithdrawal(userName: String, upiId: String, amountInr: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.requestWithdrawal(_currentUserId.value, userName, upiId, amountInr)
            result.onSuccess { id ->
                _uiMessage.emit("💰 Withdrawal request #$id submitted! Status is PENDING. Admin will manually pay to $upiId.")
                onSuccess()
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Withdrawal request failed")
            }
        }
    }

    // --- Admin Operations (Owner Authenticated) ---
    fun approveWithdrawal(withdrawalId: Long) {
        viewModelScope.launch {
            val res = repository.approveWithdrawal(_currentUserId.value, withdrawalId)
            res.onSuccess {
                _uiMessage.emit("Withdrawal #$withdrawalId APPROVED.")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Approval failed")
            }
        }
    }

    fun markWithdrawalPaid(withdrawalId: Long, utr: String, note: String?) {
        viewModelScope.launch {
            val res = repository.markWithdrawalPaid(_currentUserId.value, withdrawalId, utr, note)
            res.onSuccess {
                _uiMessage.emit("Withdrawal #$withdrawalId marked as PAID. Reference logged.")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Payment logging failed")
            }
        }
    }

    fun rejectWithdrawal(withdrawalId: Long, reason: String) {
        viewModelScope.launch {
            val res = repository.rejectWithdrawal(_currentUserId.value, withdrawalId, reason)
            res.onSuccess {
                _uiMessage.emit("Withdrawal #$withdrawalId REJECTED. Coins refunded to user.")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Rejection failed")
            }
        }
    }

    fun adjustUserCoins(targetUserId: String, delta: Long, reason: String) {
        viewModelScope.launch {
            val res = repository.adjustUserCoins(_currentUserId.value, targetUserId, delta, reason)
            res.onSuccess {
                _uiMessage.emit("Coin balance adjusted by $delta with audit note.")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Adjustment failed")
            }
        }
    }

    fun toggleUserBan(targetUserId: String, ban: Boolean) {
        viewModelScope.launch {
            val res = repository.toggleUserBan(_currentUserId.value, targetUserId, ban)
            res.onSuccess {
                _uiMessage.emit("User ${if (ban) "BANNED" else "UNBANNED"}.")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Ban toggle failed")
            }
        }
    }

    fun updateConfig(key: String, value: String) {
        viewModelScope.launch {
            val res = repository.updateConfig(_currentUserId.value, key, value)
            res.onSuccess {
                _uiMessage.emit("Setting '$key' saved.")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Config update failed")
            }
        }
    }

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            val result = repository.saveTask(_currentUserId.value, task)
            if (result.isSuccess) {
                _uiMessage.emit("Task '${task.title}' saved.")
            } else {
                _uiMessage.emit(result.exceptionOrNull()?.message ?: "Failed to save task")
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            val result = repository.deleteTask(_currentUserId.value, task)
            if (result.isSuccess) {
                _uiMessage.emit("Task deleted.")
            } else {
                _uiMessage.emit(result.exceptionOrNull()?.message ?: "Failed to delete task")
            }
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            _currentUserId.value = userId
            _uiMessage.emit("Switched active session to $userId")
        }
    }
}
