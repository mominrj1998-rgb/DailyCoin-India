package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.UserEntity
import com.example.data.repository.EarnBotRepository
import com.example.ui.screens.maskUpiId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private fun getRepo(): Pair<EarnBotRepository, AppDatabase> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = AppDatabase.getInstance(context)
        val repository = EarnBotRepository(database)
        return Pair(repository, database)
    }

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("INR Earn Bot", appName)
    }

    @Test
    fun `verify coin value and repository conversion constants`() {
        assertEquals(100L, EarnBotRepository.COINS_PER_INR)
        assertEquals(1000L, EarnBotRepository.MIN_WITHDRAWAL_COINS)
        assertEquals(10L, EarnBotRepository.MIN_WITHDRAWAL_INR)
        assertEquals(999888777L, EarnBotRepository.DEFAULT_ADMIN_TELEGRAM_ID)
    }

    @Test
    fun `1_verify owner can access admin panel and perform admin actions`() = runBlocking {
        val (repository, database) = getRepo()
        repository.ensureInitialData("usr_rahul_101")

        // usr_owner_001 is seeded with telegramId = 999888777L (the verified owner)
        val isOwner = repository.isOwner("usr_owner_001")
        assertTrue("Owner usr_owner_001 must be recognized as verified owner", isOwner)

        // Owner can adjust coins for a user with audit log
        val adjustResult = repository.adjustUserCoins(
            adminId = "usr_owner_001",
            targetUserId = "usr_rahul_101",
            deltaCoins = 200L,
            reason = "Approved promotional bonus"
        )
        assertTrue("Owner coin adjustment should succeed", adjustResult.isSuccess)

        val updatedRahul = database.userDao().getUser("usr_rahul_101")
        assertNotNull(updatedRahul)
        assertEquals(550L, updatedRahul?.coinBalance) // 350 + 200
    }

    @Test
    fun `2_verify normal user cannot access admin panel or admin APIs`() = runBlocking {
        val (repository, _) = getRepo()
        repository.ensureInitialData("usr_rahul_101")

        // usr_rahul_101 is a normal user (telegramId = 849204192L)
        val isOwner = repository.isOwner("usr_rahul_101")
        assertFalse("Normal user must NOT be recognized as owner", isOwner)

        // Normal user attempting to approve withdrawal must fail with SecurityException
        val approveResult = repository.approveWithdrawal("usr_rahul_101", 1L)
        assertTrue("Approve withdrawal must fail for normal user", approveResult.isFailure)
        assertTrue(
            "Exception must be SecurityException",
            approveResult.exceptionOrNull() is SecurityException
        )

        // Normal user attempting to adjust coins must fail with SecurityException
        val adjustResult = repository.adjustUserCoins("usr_rahul_101", "usr_rahul_101", 1000L, "Hacked")
        assertTrue("Adjust coins must fail for normal user", adjustResult.isFailure)
        assertTrue(
            "Exception must be SecurityException",
            adjustResult.exceptionOrNull() is SecurityException
        )

        // Normal user attempting to ban a user must fail with SecurityException
        val banResult = repository.toggleUserBan("usr_rahul_101", "usr_owner_001", true)
        assertTrue("Toggle ban must fail for normal user", banResult.isFailure)
        assertTrue(
            "Exception must be SecurityException",
            banResult.exceptionOrNull() is SecurityException
        )
    }

    @Test
    fun `3_verify normal user cannot read another users profile or withdrawal info and UPI is masked`() = runBlocking {
        val (repository, database) = getRepo()
        repository.ensureInitialData("usr_rahul_101")

        // Seed another user
        val secondUser = UserEntity(
            id = "usr_sneha_202",
            telegramId = 912345678L,
            username = "@sneha_p",
            fullName = "Sneha Patel",
            coinBalance = 2000L,
            totalEarned = 2000L,
            totalWithdrawnInr = 0L,
            isBanned = false,
            referralCode = "INR3321"
        )
        database.userDao().insertUser(secondUser)

        // Ensure Rahul has sufficient coins for 10 INR tier (requires 1000 coins)
        database.userDao().updateCoinBalance("usr_rahul_101", 1000L)

        // Rahul submits withdrawal for his account
        val rahulWId = repository.requestWithdrawal("usr_rahul_101", "Rahul Sharma", "rahul@okaxis", 10L)
        assertTrue("Rahul withdrawal should succeed", rahulWId.isSuccess)

        // Sneha submits withdrawal for her account
        val snehaWId = repository.requestWithdrawal("usr_sneha_202", "Sneha Patel", "sneha.patel@okhdfcbank", 10L)
        assertTrue("Sneha withdrawal should succeed", snehaWId.isSuccess)

        // When Rahul views his withdrawal history, only Rahul's record is returned
        val rahulWithdrawals = repository.getWithdrawalsForUser("usr_rahul_101").first()
        assertEquals(1, rahulWithdrawals.size)
        assertEquals("usr_rahul_101", rahulWithdrawals[0].userId)
        assertFalse(rahulWithdrawals.any { it.userId == "usr_sneha_202" })

        // When Sneha views her withdrawal history, only Sneha's record is returned
        val snehaWithdrawals = repository.getWithdrawalsForUser("usr_sneha_202").first()
        assertEquals(1, snehaWithdrawals.size)
        assertEquals("usr_sneha_202", snehaWithdrawals[0].userId)
        assertFalse(snehaWithdrawals.any { it.userId == "usr_rahul_101" })

        // Verify UPI masking
        val maskedRahulUpi = maskUpiId("rahul@okaxis")
        assertEquals("ra****@okaxis", maskedRahulUpi)

        val maskedSnehaUpi = maskUpiId("sneha.patel@okhdfcbank")
        assertEquals("sn****@okhdfcbank", maskedSnehaUpi)
    }

    @Test
    fun `4_verify normal user cannot change role to admin or modify owner config`() = runBlocking {
        val (repository, _) = getRepo()
        repository.ensureInitialData("usr_rahul_101")

        // Normal user attempting to update admin_telegram_id config to their own ID
        val updateResult = repository.updateConfig(
            adminId = "usr_rahul_101",
            key = "admin_telegram_id",
            value = "849204192"
        )
        assertTrue("Modifying config must fail for non-owner", updateResult.isFailure)
        assertTrue(
            "Exception must be SecurityException",
            updateResult.exceptionOrNull() is SecurityException
        )

        // Verify owner ID in config remains unchanged
        val adminId = repository.getAdminTelegramId()
        assertEquals(999888777L, adminId)
        assertFalse("Rahul still cannot be owner", repository.isOwner("usr_rahul_101"))
    }

    @Test
    fun `5_verify owner can search and view all users and withdrawal requests`() = runBlocking {
        val (repository, _) = getRepo()
        repository.ensureInitialData("usr_rahul_101")

        // Owner queries all users via admin API
        val usersResult = repository.getAllUsersAdmin("usr_owner_001")
        assertTrue("Owner should be able to view all users", usersResult.isSuccess)
        val users = usersResult.getOrNull() ?: emptyList()
        assertTrue("Should contain multiple users", users.size >= 2)
        assertTrue("Should contain Rahul", users.any { it.id == "usr_rahul_101" })
        assertTrue("Should contain Owner", users.any { it.id == "usr_owner_001" })

        // Owner queries all withdrawals via admin API
        val withResult = repository.getAllWithdrawalsAdmin("usr_owner_001")
        assertTrue("Owner should be able to view all withdrawals", withResult.isSuccess)
    }

    @Test
    fun `6_verify unauthorized API requests are rejected`() = runBlocking {
        val (repository, _) = getRepo()
        repository.ensureInitialData("usr_rahul_101")

        // Non-existent user trying to access admin user list
        val anonUsersResult = repository.getAllUsersAdmin("anonymous_intruder")
        assertTrue("Anon user must be rejected", anonUsersResult.isFailure)
        assertTrue(anonUsersResult.exceptionOrNull() is SecurityException)

        // Non-existent user trying to access withdrawals
        val anonWithResult = repository.getAllWithdrawalsAdmin("anonymous_intruder")
        assertTrue("Anon user must be rejected", anonWithResult.isFailure)
        assertTrue(anonWithResult.exceptionOrNull() is SecurityException)

        // Normal user trying to mark a withdrawal as PAID
        val markPaidResult = repository.markWithdrawalPaid(
            adminId = "usr_rahul_101",
            withdrawalId = 1L,
            utrRef = "FAKE_UTR_999",
            adminNote = "Fraudulent attempt"
        )
        assertTrue("Normal user mark PAID must fail", markPaidResult.isFailure)
        assertTrue(markPaidResult.exceptionOrNull() is SecurityException)
    }
}
