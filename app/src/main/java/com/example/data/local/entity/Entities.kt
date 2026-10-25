package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val telegramId: Long,
    val username: String,
    val fullName: String,
    val coinBalance: Long = 0,
    val totalEarned: Long = 0,
    val totalWithdrawnInr: Long = 0,
    val isBanned: Boolean = false,
    val referralCode: String,
    val referredBy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val amount: Long, // Positive for credit, negative for debit
    val type: String, // SPIN, MATH, AD, DAILY_BONUS, TASK, REFERRAL, WITHDRAWAL, REFUND, ADMIN_ADJUST
    val description: String,
    val balanceAfter: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val amountInr: Long, // e.g. 10, 30, 50, 100
    val coinsUsed: Long, // e.g. 1000, 3000, 5000, 10000
    val upiId: String,
    val status: String, // PENDING, APPROVED, PAID, REJECTED
    val requestDate: Long = System.currentTimeMillis(),
    val processedDate: Long? = null,
    val adminNote: String? = null,
    val paymentReference: String? = null // Real UTR / UPI reference from manual payment
)

@Entity(tableName = "spins")
data class SpinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val rewardCoins: Long,
    val dateString: String, // YYYY-MM-DD for daily limit
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val gameType: String, // MATH_PIZZA
    val question: String,
    val isCorrect: Boolean,
    val rewardCoins: Long,
    val dateString: String, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ad_watches")
data class AdWatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val rewardCoins: Long,
    val isDemo: Boolean = true,
    val dateString: String, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_rewards")
data class DailyRewardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val dayNumber: Int, // 1 to 7
    val coinsAwarded: Long,
    val claimDateString: String, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Long,
    val actionUrl: String,
    val iconName: String,
    val isEnabled: Boolean = true
)

@Entity(tableName = "user_tasks")
data class UserTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val taskId: String,
    val status: String, // NOT_STARTED, VERIFYING, COMPLETED, CLAIMED
    val verificationStartTime: Long? = null,
    val claimedAt: Long? = null
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referrerId: String,
    val refereeId: String,
    val refereeName: String,
    val rewardCoins: Long = 50,
    val status: String, // PENDING, QUALIFIED, PAID
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adminId: String,
    val actionType: String, // USER_BAN, USER_UNBAN, COIN_ADJUST, WITHDRAW_APPROVE, WITHDRAW_PAID, WITHDRAW_REJECT, SETTING_UPDATE
    val targetUserId: String? = null,
    val details: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_configs")
data class AppConfigEntity(
    @PrimaryKey val key: String,
    val value: String
)
