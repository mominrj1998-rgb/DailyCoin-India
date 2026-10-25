package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        WithdrawalEntity::class,
        SpinEntity::class,
        GameRecordEntity::class,
        AdWatchEntity::class,
        DailyRewardEntity::class,
        TaskEntity::class,
        UserTaskEntity::class,
        ReferralEntity::class,
        AdminLogEntity::class,
        AppConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun earnDao(): EarnDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inr_earn_bot.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
