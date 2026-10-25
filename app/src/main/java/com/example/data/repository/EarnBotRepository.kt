package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EarnBotRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val transactionDao = database.transactionDao()
    private val withdrawalDao = database.withdrawalDao()
    private val earnDao = database.earnDao()
    private val adminDao = database.adminDao()

    companion object {
        const val COINS_PER_INR = 100L
        const val MIN_WITHDRAWAL_COINS = 1000L
        const val MIN_WITHDRAWAL_INR = 10L

        const val DEFAULT_ADMIN_TELEGRAM_ID = 999888777L

        // Default Configs
        const val DEFAULT_DAILY_SPIN_LIMIT = 100
        const val DEFAULT_DAILY_MATH_LIMIT = 30
        const val DEFAULT_DAILY_AD_LIMIT = 100
        const val DEFAULT_DAILY_EARNING_LIMIT = 500L
        const val DEFAULT_MATH_REWARD = 5L
        const val DEFAULT_AD_REWARD = 10L
        const val DEFAULT_REFERRAL_REWARD = 50L
    }

    suspend fun getAdminTelegramId(): Long = withContext(Dispatchers.IO) {
        adminDao.getConfig("admin_telegram_id")?.value?.toLongOrNull() ?: DEFAULT_ADMIN_TELEGRAM_ID
    }

    suspend fun setAdminTelegramId(newAdminTgId: Long): Unit = withContext(Dispatchers.IO) {
        adminDao.setConfig(AppConfigEntity("admin_telegram_id", newAdminTgId.toString()))
    }

    suspend fun isOwner(userId: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId) ?: return@withContext false
        val adminTgId = getAdminTelegramId()
        adminTgId > 0 && user.telegramId == adminTgId
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayString(): String = dateFormat.format(Date())

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // --- User & Seeding ---
    fun getUser(userId: String): Flow<UserEntity?> = userDao.getUserFlow(userId)
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    fun getTransactions(userId: String): Flow<List<TransactionEntity>> = transactionDao.getTransactionsForUser(userId)
    fun getTodayEarnedCoins(userId: String): Flow<Long> = transactionDao.getTodayEarnedCoinsFlow(userId, getStartOfDayTimestamp())

    suspend fun ensureInitialData(defaultUserId: String = "usr_rahul_101"): String = withContext(Dispatchers.IO) {
        val existing = userDao.getUser(defaultUserId)
        if (existing == null) {
            val user = UserEntity(
                id = defaultUserId,
                telegramId = 849204192L,
                username = "@rahul_earner",
                fullName = "Rahul Sharma",
                coinBalance = 350L,
                totalEarned = 350L,
                totalWithdrawnInr = 0L,
                isBanned = false,
                referralCode = "INR7892"
            )
            userDao.insertUser(user)
            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = defaultUserId,
                    amount = 350L,
                    type = "WELCOME_BONUS",
                    description = "Welcome sign-up reward",
                    balanceAfter = 350L
                )
            )

            // Seed sample tasks
            earnDao.insertTasks(
                listOf(
                    TaskEntity(
                        id = "task_tg_channel",
                        title = "Join Official Telegram Channel",
                        description = "Get instant payout alerts, bonus drop codes, and live proofs.",
                        rewardCoins = 30L,
                        actionUrl = "https://t.me/INR_Earn_Official",
                        iconName = "telegram"
                    ),
                    TaskEntity(
                        id = "task_tg_group",
                        title = "Join Community Chat",
                        description = "Chat with active earners and share payment receipts.",
                        rewardCoins = 25L,
                        actionUrl = "https://t.me/INR_Earn_Chat",
                        iconName = "chat"
                    ),
                    TaskEntity(
                        id = "task_rules",
                        title = "Read Earning & Anti-Fraud Guidelines",
                        description = "Understand 100 Coins = ₹1 rate and genuine UPI manual payouts.",
                        rewardCoins = 15L,
                        actionUrl = "https://t.me/INR_Earn_Rules",
                        iconName = "menu_book"
                    ),
                    TaskEntity(
                        id = "task_share",
                        title = "Share Bot in Any WhatsApp/TG Group",
                        description = "Invite genuine friends to start earning together.",
                        rewardCoins = 50L,
                        actionUrl = "https://t.me/share/url?url=https://t.me/INREarn_Bot",
                        iconName = "share"
                    )
                )
            )

            // Seed sample referrals
            earnDao.insertReferral(
                ReferralEntity(
                    referrerId = defaultUserId,
                    refereeId = "usr_friend_201",
                    refereeName = "Vikram Patel (@vikram_p)",
                    rewardCoins = 50L,
                    status = "QUALIFIED"
                )
            )
            earnDao.insertReferral(
                ReferralEntity(
                    referrerId = defaultUserId,
                    refereeId = "usr_friend_202",
                    refereeName = "Amit Verma (@amit_99)",
                    rewardCoins = 50L,
                    status = "PENDING"
                )
            )

            // Seed default configs
            adminDao.setConfig(AppConfigEntity("daily_spin_limit", DEFAULT_DAILY_SPIN_LIMIT.toString()))
            adminDao.setConfig(AppConfigEntity("daily_earning_limit", DEFAULT_DAILY_EARNING_LIMIT.toString()))
            adminDao.setConfig(AppConfigEntity("math_reward", DEFAULT_MATH_REWARD.toString()))
            adminDao.setConfig(AppConfigEntity("referral_reward", DEFAULT_REFERRAL_REWARD.toString()))
            adminDao.setConfig(AppConfigEntity("admin_telegram_id", DEFAULT_ADMIN_TELEGRAM_ID.toString()))
        }

        // Always ensure verified owner account exists
        val ownerId = "usr_owner_001"
        if (userDao.getUser(ownerId) == null) {
            userDao.insertUser(
                UserEntity(
                    id = ownerId,
                    telegramId = DEFAULT_ADMIN_TELEGRAM_ID,
                    username = "@bot_owner",
                    fullName = "Verified Owner",
                    coinBalance = 50000L,
                    totalEarned = 50000L,
                    totalWithdrawnInr = 0L,
                    isBanned = false,
                    referralCode = "OWNER777"
                )
            )
            adminDao.setConfig(AppConfigEntity("admin_telegram_id", DEFAULT_ADMIN_TELEGRAM_ID.toString()))
        }

        defaultUserId
    }

    // --- Earning: Spins ---
    fun observeDailySpins(userId: String): Flow<Int> = earnDao.observeDailySpinCount(userId, getTodayString())

    suspend fun getDailySpinLimit(): Int = withContext(Dispatchers.IO) {
        adminDao.getConfig("daily_spin_limit")?.value?.toIntOrNull() ?: DEFAULT_DAILY_SPIN_LIMIT
    }

    suspend fun determineSpinReward(userId: String): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.isBanned) return@withContext Result.failure(Exception("User account is suspended"))

        val today = getTodayString()
        val spinCount = earnDao.getDailySpinCount(userId, today)
        val limit = getDailySpinLimit()

        if (spinCount >= limit) {
            return@withContext Result.failure(Exception("Daily spin limit reached ($limit/$limit)"))
        }

        // Possible rewards: 5, 10, 15, 30, 40, 50, 75, 100
        val rewards = listOf(5L, 10L, 15L, 30L, 40L, 50L, 75L, 100L)
        val weights = listOf(35, 25, 15, 10, 8, 4, 2, 1)
        val totalWeight = weights.sum()
        val randomVal = (1..totalWeight).random()

        var accumulated = 0
        var selectedReward = rewards.first()
        for (i in rewards.indices) {
            accumulated += weights[i]
            if (randomVal <= accumulated) {
                selectedReward = rewards[i]
                break
            }
        }
        Result.success(selectedReward)
    }

    suspend fun creditSpinReward(userId: String, selectedReward: Long): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.isBanned) return@withContext Result.failure(Exception("User account is suspended"))

        val today = getTodayString()
        earnDao.insertSpin(SpinEntity(userId = userId, rewardCoins = selectedReward, dateString = today))
        userDao.updateCoinBalance(userId, selectedReward)
        val updatedUser = userDao.getUser(userId)!!
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                amount = selectedReward,
                type = "SPIN",
                description = "Free Spin Wheel Reward",
                balanceAfter = updatedUser.coinBalance
            )
        )

        checkAndQualifyReferee(userId)
        Result.success(selectedReward)
    }

    suspend fun executeSpin(userId: String): Result<Long> = withContext(Dispatchers.IO) {
        val determined = determineSpinReward(userId)
        if (determined.isFailure) return@withContext determined
        creditSpinReward(userId, determined.getOrThrow())
    }

    // --- Earning: Math / Pizza Game ---
    fun observeDailyGames(userId: String): Flow<Int> = earnDao.observeDailyGameCount(userId, getTodayString())

    suspend fun submitMathAnswer(userId: String, question: String, isCorrect: Boolean): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.isBanned) return@withContext Result.failure(Exception("User is banned"))

        val today = getTodayString()
        val count = earnDao.getDailyGameCount(userId, today)
        if (count >= DEFAULT_DAILY_MATH_LIMIT) {
            return@withContext Result.failure(Exception("Daily math challenge limit reached ($DEFAULT_DAILY_MATH_LIMIT)"))
        }

        val rewardCoins = if (isCorrect) {
            adminDao.getConfig("math_reward")?.value?.toLongOrNull() ?: DEFAULT_MATH_REWARD
        } else 0L

        earnDao.insertGameRecord(
            GameRecordEntity(
                userId = userId,
                gameType = "MATH_PIZZA",
                question = question,
                isCorrect = isCorrect,
                rewardCoins = rewardCoins,
                dateString = today
            )
        )

        if (rewardCoins > 0) {
            userDao.updateCoinBalance(userId, rewardCoins)
            val updatedUser = userDao.getUser(userId)!!
            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = userId,
                    amount = rewardCoins,
                    type = "MATH",
                    description = "Math Pizza Challenge Correct Answer",
                    balanceAfter = updatedUser.coinBalance
                )
            )
            checkAndQualifyReferee(userId)
        }

        Result.success(rewardCoins)
    }

    // --- Earning: Rewarded Ads (Demo Mode) ---
    fun observeDailyAds(userId: String): Flow<Int> = earnDao.observeDailyAdCount(userId, getTodayString())

    suspend fun completeRewardedAd(userId: String): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.isBanned) return@withContext Result.failure(Exception("User is banned"))

        val today = getTodayString()
        val count = earnDao.getDailyAdCount(userId, today)
        if (count >= DEFAULT_DAILY_AD_LIMIT) {
            return@withContext Result.failure(Exception("Daily ad limit reached ($DEFAULT_DAILY_AD_LIMIT)"))
        }

        val reward = DEFAULT_AD_REWARD
        earnDao.insertAdWatch(
            AdWatchEntity(
                userId = userId,
                rewardCoins = reward,
                isDemo = true,
                dateString = today
            )
        )
        userDao.updateCoinBalance(userId, reward)
        val updatedUser = userDao.getUser(userId)!!
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                amount = reward,
                type = "AD",
                description = "Rewarded Ad (Demo Mode completion)",
                balanceAfter = updatedUser.coinBalance
            )
        )
        checkAndQualifyReferee(userId)
        Result.success(reward)
    }

    // --- Earning: Daily Bonus Streak ---
    fun observeLastDailyReward(userId: String): Flow<DailyRewardEntity?> = earnDao.observeLastDailyReward(userId)

    suspend fun claimDailyBonus(userId: String): Result<Pair<Int, Long>> = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.isBanned) return@withContext Result.failure(Exception("User is banned"))

        val today = getTodayString()
        val lastReward = earnDao.getLastDailyReward(userId)

        if (lastReward?.claimDateString == today) {
            return@withContext Result.failure(Exception("Already claimed today's bonus! Check back tomorrow."))
        }

        // Streak check: Was the last claim yesterday?
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayString = dateFormat.format(calendar.time)

        val nextDay = when {
            lastReward == null -> 1
            lastReward.claimDateString == yesterdayString -> {
                if (lastReward.dayNumber >= 7) 1 else lastReward.dayNumber + 1
            }
            else -> 1 // Streak broken, reset to Day 1
        }

        val rewardSchedule = mapOf(
            1 to 10L,
            2 to 15L,
            3 to 20L,
            4 to 30L,
            5 to 40L,
            6 to 50L,
            7 to 100L
        )
        val coins = rewardSchedule[nextDay] ?: 10L

        earnDao.insertDailyReward(
            DailyRewardEntity(
                userId = userId,
                dayNumber = nextDay,
                coinsAwarded = coins,
                claimDateString = today
            )
        )
        userDao.updateCoinBalance(userId, coins)
        val updatedUser = userDao.getUser(userId)!!
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                amount = coins,
                type = "DAILY_BONUS",
                description = "Day $nextDay Login Bonus Streak",
                balanceAfter = updatedUser.coinBalance
            )
        )

        Result.success(Pair(nextDay, coins))
    }

    // --- Tasks ---
    fun getEnabledTasks(): Flow<List<TaskEntity>> = earnDao.getEnabledTasks()
    fun getAllTasks(): Flow<List<TaskEntity>> = earnDao.getAllTasks()
    fun getUserTasks(userId: String): Flow<List<UserTaskEntity>> = earnDao.getUserTasks(userId)

    suspend fun startTaskVerification(userId: String, taskId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId) ?: return@withContext Result.failure(Exception("User not found"))
        val existing = earnDao.getUserTask(userId, taskId)
        if (existing != null && (existing.status == "CLAIMED" || existing.status == "VERIFYING")) {
            return@withContext Result.success(Unit)
        }

        earnDao.insertOrUpdateUserTask(
            UserTaskEntity(
                id = existing?.id ?: 0,
                userId = userId,
                taskId = taskId,
                status = "VERIFYING",
                verificationStartTime = System.currentTimeMillis()
            )
        )
        Result.success(Unit)
    }

    suspend fun claimTaskReward(userId: String, taskId: String): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.isBanned) return@withContext Result.failure(Exception("User is banned"))

        val userTask = earnDao.getUserTask(userId, taskId) ?: return@withContext Result.failure(Exception("Task not started"))
        if (userTask.status == "CLAIMED") {
            return@withContext Result.failure(Exception("Task reward already claimed"))
        }

        val allTasks = earnDao.getAllTasks()
        // Find task definition
        val task = earnDao.getAllTasks() // or get specific
        // For simplicity:
        val taskDef = database.openHelper.readableDatabase.let {
            val cursor = it.query("SELECT rewardCoins, title FROM tasks WHERE id = '$taskId'")
            if (cursor.moveToFirst()) {
                val r = cursor.getLong(0)
                val t = cursor.getString(1)
                cursor.close()
                Pair(r, t)
            } else {
                cursor.close()
                Pair(20L, "Task")
            }
        }

        earnDao.insertOrUpdateUserTask(
            userTask.copy(status = "CLAIMED", claimedAt = System.currentTimeMillis())
        )
        userDao.updateCoinBalance(userId, taskDef.first)
        val updatedUser = userDao.getUser(userId)!!
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                amount = taskDef.first,
                type = "TASK",
                description = "Task completed: ${taskDef.second}",
                balanceAfter = updatedUser.coinBalance
            )
        )
        checkAndQualifyReferee(userId)

        Result.success(taskDef.first)
    }

    // --- Referrals ---
    fun getReferralsForUser(userId: String): Flow<List<ReferralEntity>> = earnDao.getReferralsForUser(userId)
    fun getQualifiedReferralsCount(userId: String): Flow<Int> = earnDao.getQualifiedReferralsCount(userId)

    suspend fun registerReferral(refereeId: String, refereeName: String, referralCode: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val referrer = userDao.getUserByReferralCode(referralCode) ?: return@withContext Result.failure(Exception("Invalid referral code"))
        if (referrer.id == refereeId) {
            return@withContext Result.failure(Exception("Anti-fraud: You cannot refer your own account!"))
        }
        val existing = earnDao.getReferralByReferee(refereeId)
        if (existing != null) {
            return@withContext Result.failure(Exception("User already has a referrer"))
        }

        earnDao.insertReferral(
            ReferralEntity(
                referrerId = referrer.id,
                refereeId = refereeId,
                refereeName = refereeName,
                rewardCoins = DEFAULT_REFERRAL_REWARD,
                status = "PENDING"
            )
        )
        Result.success(true)
    }

    private suspend fun checkAndQualifyReferee(refereeId: String) {
        val referral = earnDao.getReferralByReferee(refereeId) ?: return
        if (referral.status != "PENDING") return

        // Referee qualifies if they completed >= 3 spins or any task
        val spinCount = database.openHelper.readableDatabase.let {
            val cursor = it.query("SELECT COUNT(*) FROM spins WHERE userId = '$refereeId'")
            val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
            cursor.close()
            count
        }

        if (spinCount >= 3) {
            earnDao.updateReferral(referral.copy(status = "QUALIFIED"))
            // Credit referrer
            val reward = adminDao.getConfig("referral_reward")?.value?.toLongOrNull() ?: DEFAULT_REFERRAL_REWARD
            userDao.updateCoinBalance(referral.referrerId, reward)
            val referrer = userDao.getUser(referral.referrerId)
            if (referrer != null) {
                transactionDao.insertTransaction(
                    TransactionEntity(
                        userId = referral.referrerId,
                        amount = reward,
                        type = "REFERRAL",
                        description = "Referral qualification bonus for ${referral.refereeName}",
                        balanceAfter = referrer.coinBalance
                    )
                )
            }
        }
    }

    // --- Withdrawals ---
    fun getWithdrawalsForUser(userId: String): Flow<List<WithdrawalEntity>> = withdrawalDao.getWithdrawalsForUser(userId)
    fun observePendingWithdrawal(userId: String): Flow<WithdrawalEntity?> = withdrawalDao.observePendingWithdrawal(userId)
    fun getUserPaidInr(userId: String): Flow<Long> = withdrawalDao.getUserPaidInr(userId)
    fun getUserPendingInr(userId: String): Flow<Long> = withdrawalDao.getUserPendingInr(userId)

    suspend fun requestWithdrawal(
        userId: String,
        userName: String,
        upiId: String,
        amountInr: Long
    ): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.isBanned) return@withContext Result.failure(Exception("Your account has been suspended. Withdrawals disabled."))

        val validAmounts = listOf(10L, 30L, 50L, 100L)
        if (amountInr !in validAmounts) {
            return@withContext Result.failure(Exception("Invalid withdrawal tier. Choose ₹10, ₹30, ₹50, or ₹100."))
        }

        val coinsRequired = amountInr * COINS_PER_INR

        if (user.coinBalance < coinsRequired) {
            return@withContext Result.failure(Exception("You need more Coins to withdraw. Required: $coinsRequired, Current: ${user.coinBalance}."))
        }

        // Anti-fraud: Pending withdrawal lock
        val pending = withdrawalDao.getPendingWithdrawalForUser(userId)
        if (pending != null) {
            return@withContext Result.failure(Exception("You already have a pending withdrawal request of ₹${pending.amountInr}. Admin must verify and process it before you can submit another."))
        }

        // Clean & validate UPI ID
        val cleanUpi = upiId.trim()
        if (!cleanUpi.contains("@") || cleanUpi.length < 5) {
            return@withContext Result.failure(Exception("Please enter a valid UPI ID (e.g., yourname@upi or 9876543210@paytm)."))
        }

        // Deduct coins & create PENDING record atomically
        userDao.updateCoinBalance(userId, -coinsRequired)
        val updatedUser = userDao.getUser(userId)!!

        val withdrawalId = withdrawalDao.insertWithdrawal(
            WithdrawalEntity(
                userId = userId,
                userName = userName.trim().ifEmpty { user.fullName },
                amountInr = amountInr,
                coinsUsed = coinsRequired,
                upiId = cleanUpi,
                status = "PENDING"
            )
        )

        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                amount = -coinsRequired,
                type = "WITHDRAWAL",
                description = "Withdrawal Request #$withdrawalId (₹$amountInr to $cleanUpi)",
                balanceAfter = updatedUser.coinBalance
            )
        )

        Result.success(withdrawalId)
    }

    // --- Admin Operations (Strictly Restricted to Bot Owner) ---
    suspend fun getAllUsersAdmin(requesterId: String): Result<List<UserEntity>> = withContext(Dispatchers.IO) {
        if (!isOwner(requesterId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can access user records."))
        }
        Result.success(userDao.getAllUsers())
    }

    suspend fun getAllWithdrawalsAdmin(requesterId: String): Result<List<WithdrawalEntity>> = withContext(Dispatchers.IO) {
        if (!isOwner(requesterId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can access withdrawal requests."))
        }
        Result.success(withdrawalDao.getAllWithdrawals())
    }

    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>> = withdrawalDao.getAllWithdrawalsFlow()
    fun getPendingWithdrawalsCount(): Flow<Int> = withdrawalDao.getPendingWithdrawalsCount()
    fun getTotalPaidInr(): Flow<Long> = withdrawalDao.getTotalPaidInr()
    fun getTotalCoinsIssued(): Flow<Long> = transactionDao.getTotalCoinsIssuedFlow()
    fun getAdminLogs(): Flow<List<AdminLogEntity>> = adminDao.getRecentLogs()

    suspend fun approveWithdrawal(adminId: String, withdrawalId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isOwner(adminId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can approve withdrawals."))
        }
        val w = withdrawalDao.getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Not found"))
        if (w.status != "PENDING") return@withContext Result.failure(Exception("Cannot approve non-pending request"))

        withdrawalDao.updateWithdrawal(w.copy(status = "APPROVED", processedDate = System.currentTimeMillis()))
        adminDao.insertLog(
            AdminLogEntity(
                adminId = adminId,
                actionType = "WITHDRAW_APPROVE",
                targetUserId = w.userId,
                details = "Approved withdrawal #$withdrawalId for ₹${w.amountInr}"
            )
        )
        Result.success(Unit)
    }

    suspend fun markWithdrawalPaid(adminId: String, withdrawalId: Long, utrRef: String, adminNote: String?): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isOwner(adminId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can mark payouts."))
        }
        val w = withdrawalDao.getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Not found"))
        if (w.status == "PAID") return@withContext Result.failure(Exception("Already marked paid"))

        if (utrRef.isBlank()) {
            return@withContext Result.failure(Exception("Please enter the real UPI Transaction Reference / UTR number from your payment app."))
        }

        withdrawalDao.updateWithdrawal(
            w.copy(
                status = "PAID",
                paymentReference = utrRef.trim(),
                adminNote = adminNote?.trim(),
                processedDate = System.currentTimeMillis()
            )
        )
        userDao.incrementTotalWithdrawn(w.userId, w.amountInr)

        adminDao.insertLog(
            AdminLogEntity(
                adminId = adminId,
                actionType = "WITHDRAW_PAID",
                targetUserId = w.userId,
                details = "Marked withdrawal #$withdrawalId as PAID. UTR: ${utrRef.trim()}, Note: ${adminNote ?: "None"}"
            )
        )
        Result.success(Unit)
    }

    suspend fun rejectWithdrawal(adminId: String, withdrawalId: Long, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isOwner(adminId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can reject withdrawals."))
        }
        val w = withdrawalDao.getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Not found"))
        if (w.status == "PAID") return@withContext Result.failure(Exception("Cannot reject already paid withdrawal"))
        if (w.status == "REJECTED") return@withContext Result.failure(Exception("Already rejected"))

        // Refund coins to user ledger
        userDao.updateCoinBalance(w.userId, w.coinsUsed)
        val updatedUser = userDao.getUser(w.userId)!!

        withdrawalDao.updateWithdrawal(
            w.copy(
                status = "REJECTED",
                adminNote = "Rejected: $reason",
                processedDate = System.currentTimeMillis()
            )
        )

        transactionDao.insertTransaction(
            TransactionEntity(
                userId = w.userId,
                amount = w.coinsUsed,
                type = "REFUND",
                description = "Refund for rejected withdrawal #$withdrawalId: $reason",
                balanceAfter = updatedUser.coinBalance
            )
        )

        adminDao.insertLog(
            AdminLogEntity(
                adminId = adminId,
                actionType = "WITHDRAW_REJECT",
                targetUserId = w.userId,
                details = "Rejected withdrawal #$withdrawalId. Refunded ${w.coinsUsed} coins. Reason: $reason"
            )
        )
        Result.success(Unit)
    }

    suspend fun adjustUserCoins(adminId: String, targetUserId: String, deltaCoins: Long, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isOwner(adminId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can adjust user coins."))
        }
        val user = userDao.getUser(targetUserId) ?: return@withContext Result.failure(Exception("User not found"))
        if (reason.isBlank()) return@withContext Result.failure(Exception("Mandatory audit reason required for coin adjustment"))

        userDao.updateCoinBalance(targetUserId, deltaCoins)
        val updated = userDao.getUser(targetUserId)!!

        transactionDao.insertTransaction(
            TransactionEntity(
                userId = targetUserId,
                amount = deltaCoins,
                type = "ADMIN_ADJUST",
                description = "Admin manual balance adjust: $reason",
                balanceAfter = updated.coinBalance
            )
        )

        adminDao.insertLog(
            AdminLogEntity(
                adminId = adminId,
                actionType = "COIN_ADJUST",
                targetUserId = targetUserId,
                details = "Adjusted coins by $deltaCoins (New Balance: ${updated.coinBalance}). Reason: $reason"
            )
        )
        Result.success(Unit)
    }

    suspend fun toggleUserBan(adminId: String, targetUserId: String, ban: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isOwner(adminId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can ban or unban users."))
        }
        val user = userDao.getUser(targetUserId) ?: return@withContext Result.failure(Exception("User not found"))
        userDao.setUserBanned(targetUserId, ban)
        adminDao.insertLog(
            AdminLogEntity(
                adminId = adminId,
                actionType = if (ban) "USER_BAN" else "USER_UNBAN",
                targetUserId = targetUserId,
                details = "${if (ban) "Banned" else "Unbanned"} user ${user.username} (${user.id})"
            )
        )
        Result.success(Unit)
    }

    suspend fun updateConfig(adminId: String, key: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isOwner(adminId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can modify game settings."))
        }
        adminDao.setConfig(AppConfigEntity(key, value))
        adminDao.insertLog(
            AdminLogEntity(
                adminId = adminId,
                actionType = "SETTING_UPDATE",
                details = "Updated config '$key' to '$value'"
            )
        )
        Result.success(Unit)
    }

    suspend fun saveTask(adminId: String, task: TaskEntity): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isOwner(adminId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can modify tasks."))
        }
        earnDao.insertTask(task)
        adminDao.insertLog(
            AdminLogEntity(
                adminId = adminId,
                actionType = "TASK_SAVE",
                details = "Saved task: ${task.title}"
            )
        )
        Result.success(Unit)
    }

    suspend fun deleteTask(adminId: String, task: TaskEntity): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isOwner(adminId)) {
            return@withContext Result.failure(SecurityException("Unauthorized: Access Denied. Only the verified bot owner (ADMIN_TELEGRAM_ID) can delete tasks."))
        }
        earnDao.deleteTask(task)
        adminDao.insertLog(
            AdminLogEntity(
                adminId = adminId,
                actionType = "TASK_DELETE",
                details = "Deleted task: ${task.title}"
            )
        )
        Result.success(Unit)
    }
}
