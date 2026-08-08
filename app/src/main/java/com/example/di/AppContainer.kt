package com.example.di

import android.content.Context
import com.example.core.ai.AiService
import com.example.core.ai.GeminiAiService
import com.example.core.location.LocationService
import com.example.data.local.database.AppDatabase
import com.example.data.repository.*

/**
 * Dependency Injection Container for Production Architecture.
 * Centralizes creation and management of Database, DAOs, Core Services, and Repositories.
 */
class AppContainer(private val context: Context) {

    // Database & DAOs
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val userDao by lazy { database.userDao() }
    val issueDao by lazy { database.issueDao() }
    val verificationDao by lazy { database.verificationDao() }
    val notificationDao by lazy { database.notificationDao() }
    val rewardDao by lazy { database.rewardDao() }
    val feedbackDao by lazy { database.feedbackDao() }

    // Core Services
    val aiService: AiService by lazy {
        GeminiAiService()
    }

    val locationService: LocationService by lazy {
        LocationService(context)
    }

    // Repositories & Seeders
    val seeder: DatabaseSeeder by lazy {
        DatabaseSeeder(userDao, issueDao, notificationDao, rewardDao)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(userDao, issueDao, notificationDao, seeder)
    }

    val issueRepository: IssueRepository by lazy {
        IssueRepository(issueDao, userDao, notificationDao, rewardDao, aiService)
    }

    val verificationRepository: VerificationRepository by lazy {
        VerificationRepository(verificationDao, issueDao, userDao, rewardDao, notificationDao)
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(notificationDao)
    }

    val rewardRepository: RewardRepository by lazy {
        RewardRepository(rewardDao, userDao)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(userDao, feedbackDao, issueDao)
    }
}
