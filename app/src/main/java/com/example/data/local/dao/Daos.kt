package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET coinBalance = coinBalance + :deltaCoins, totalEarned = totalEarned + CASE WHEN :deltaCoins > 0 THEN :deltaCoins ELSE 0 END WHERE id = :userId")
    suspend fun updateCoinBalance(userId: String, deltaCoins: Long)

    @Query("UPDATE users SET isBanned = :banned WHERE id = :userId")
    suspend fun setUserBanned(userId: String, banned: Boolean)

    @Query("UPDATE users SET totalWithdrawnInr = totalWithdrawnInr + :amountInr WHERE id = :userId")
    suspend fun incrementTotalWithdrawn(userId: String, amountInr: Long)
}

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT 100")
    fun getRecentTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :userId AND type = :type AND createdAt >= :sinceTimestamp")
    suspend fun getEarningsByTypeSince(userId: String, type: String, sinceTimestamp: Long): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :userId AND amount > 0 AND createdAt >= :sinceTimestamp")
    fun getTodayEarnedCoinsFlow(userId: String, sinceTimestamp: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE amount > 0")
    fun getTotalCoinsIssuedFlow(): Flow<Long>
}

@Dao
interface WithdrawalDao {
    @Insert
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Query("SELECT * FROM withdrawals WHERE id = :id")
    suspend fun getWithdrawalById(id: Long): WithdrawalEntity?

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY requestDate DESC")
    fun getWithdrawalsForUser(userId: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userId = :userId AND status = 'PENDING' LIMIT 1")
    suspend fun getPendingWithdrawalForUser(userId: String): WithdrawalEntity?

    @Query("SELECT * FROM withdrawals WHERE userId = :userId AND status = 'PENDING' LIMIT 1")
    fun observePendingWithdrawal(userId: String): Flow<WithdrawalEntity?>

    @Query("SELECT * FROM withdrawals ORDER BY requestDate DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY requestDate DESC")
    suspend fun getAllWithdrawals(): List<WithdrawalEntity>

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'PENDING'")
    fun getPendingWithdrawalsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amountInr), 0) FROM withdrawals WHERE status = 'PAID'")
    fun getTotalPaidInr(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountInr), 0) FROM withdrawals WHERE userId = :userId AND status = 'PAID'")
    fun getUserPaidInr(userId: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountInr), 0) FROM withdrawals WHERE userId = :userId AND status = 'PENDING'")
    fun getUserPendingInr(userId: String): Flow<Long>
}

@Dao
interface EarnDao {
    // Spins
    @Insert
    suspend fun insertSpin(spin: SpinEntity): Long

    @Query("SELECT COUNT(*) FROM spins WHERE userId = :userId AND dateString = :dateString")
    suspend fun getDailySpinCount(userId: String, dateString: String): Int

    @Query("SELECT COUNT(*) FROM spins WHERE userId = :userId AND dateString = :dateString")
    fun observeDailySpinCount(userId: String, dateString: String): Flow<Int>

    // Math Games
    @Insert
    suspend fun insertGameRecord(record: GameRecordEntity): Long

    @Query("SELECT COUNT(*) FROM game_records WHERE userId = :userId AND dateString = :dateString")
    suspend fun getDailyGameCount(userId: String, dateString: String): Int

    @Query("SELECT COUNT(*) FROM game_records WHERE userId = :userId AND dateString = :dateString")
    fun observeDailyGameCount(userId: String, dateString: String): Flow<Int>

    // Ad Watches
    @Insert
    suspend fun insertAdWatch(adWatch: AdWatchEntity): Long

    @Query("SELECT COUNT(*) FROM ad_watches WHERE userId = :userId AND dateString = :dateString")
    suspend fun getDailyAdCount(userId: String, dateString: String): Int

    @Query("SELECT COUNT(*) FROM ad_watches WHERE userId = :userId AND dateString = :dateString")
    fun observeDailyAdCount(userId: String, dateString: String): Flow<Int>

    // Daily Login Rewards
    @Insert
    suspend fun insertDailyReward(reward: DailyRewardEntity): Long

    @Query("SELECT * FROM daily_rewards WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastDailyReward(userId: String): DailyRewardEntity?

    @Query("SELECT * FROM daily_rewards WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    fun observeLastDailyReward(userId: String): Flow<DailyRewardEntity?>

    // Tasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE isEnabled = 1")
    fun getEnabledTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM user_tasks WHERE userId = :userId")
    fun getUserTasks(userId: String): Flow<List<UserTaskEntity>>

    @Query("SELECT * FROM user_tasks WHERE userId = :userId AND taskId = :taskId LIMIT 1")
    suspend fun getUserTask(userId: String, taskId: String): UserTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserTask(userTask: UserTaskEntity)

    // Referrals
    @Insert
    suspend fun insertReferral(referral: ReferralEntity): Long

    @Query("SELECT * FROM referrals WHERE referrerId = :referrerId ORDER BY createdAt DESC")
    fun getReferralsForUser(referrerId: String): Flow<List<ReferralEntity>>

    @Query("SELECT COUNT(*) FROM referrals WHERE referrerId = :referrerId AND status = 'QUALIFIED'")
    fun getQualifiedReferralsCount(referrerId: String): Flow<Int>

    @Query("SELECT * FROM referrals WHERE refereeId = :refereeId LIMIT 1")
    suspend fun getReferralByReferee(refereeId: String): ReferralEntity?

    @Update
    suspend fun updateReferral(referral: ReferralEntity)
}

@Dao
interface AdminDao {
    @Insert
    suspend fun insertLog(log: AdminLogEntity): Long

    @Query("SELECT * FROM admin_logs ORDER BY createdAt DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<AdminLogEntity>>

    @Query("SELECT * FROM app_configs WHERE `key` = :key")
    suspend fun getConfig(key: String): AppConfigEntity?

    @Query("SELECT * FROM app_configs WHERE `key` = :key")
    fun observeConfig(key: String): Flow<AppConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: AppConfigEntity)
}
