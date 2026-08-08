package com.example.data.repository

import android.content.Context
import com.example.core.ai.AiService
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID
import com.example.data.remote.SupabaseSetup
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonPrimitive

// --- Auth Repository ---

class AuthRepository(
    private val userDao: UserDao,
    private val issueDao: IssueDao,
    private val notificationDao: NotificationDao,
    private val databaseSeeder: DatabaseSeeder
) {
    val currentUser: Flow<UserEntity?> = userDao.getCurrentUser()

    suspend fun login(emailOrPhone: String, passwordHash: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            try {
                SupabaseSetup.client.auth.signInWith(Email) {
                    email = emailOrPhone
                    password = passwordHash
                }
            } catch (e: Exception) {
                // Graceful fallback for offline / demo mode
                e.printStackTrace()
            }
            
            val localUser = userDao.getCurrentUserSync()
            val localName = if (localUser != null && localUser.email == emailOrPhone) localUser.name else null
            
            userDao.clearUser()
            issueDao.clearIssues()
            notificationDao.clearNotifications()
            
            val session = SupabaseSetup.client.auth.currentSessionOrNull()
            val user = session?.user
            val metaName = user?.userMetadata?.get("name")?.jsonPrimitive?.content
            val userEmail = user?.email
            val emailValue: String = when {
                !userEmail.isNullOrBlank() -> userEmail
                emailOrPhone.isNotBlank() -> emailOrPhone
                else -> "demo@jansetu.ai"
            }
            
            val rawName: String = localName ?: metaName ?: if (emailValue.contains("@")) emailValue.substringBefore("@") else "Citizen"
            val derivedName: String = rawName.split(".", "_", "-").joinToString(" ") { 
                it.lowercase().replaceFirstChar { char -> char.uppercase() } 
            }.ifBlank { "Citizen" }
            
            val userId = user?.id ?: "user_${emailValue.hashCode().let { if (it < 0) -it else it }}"

            val activeUser = UserEntity(
                id = userId,
                name = derivedName,
                email = emailValue,
                phone = "9876543210",
                city = "Dhanbad",
                area = "Hirapur",
                points = 460,
                badgeLevel = "Civic Hero",
                profileImageUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                totalReports = 3,
                totalVerifications = 8,
                resolvedReports = 1
            )
            userDao.insertUser(activeUser)
            databaseSeeder.seedDefaultIssuesAndContent(userId)
            
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = e.message ?: "Invalid email or password."
            return@withContext Result.failure(Exception(msg))
        }
    }

    suspend fun signUp(
        name: String,
        email: String,
        phone: String,
        passwordHash: String,
        city: String,
        area: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseSetup.client.auth.signUpWith(Email) {
                this.email = email
                this.password = passwordHash
                this.data = buildJsonObject {
                    put("name", name)
                }
            }
            userDao.clearUser()
            issueDao.clearIssues()
            notificationDao.clearNotifications()
            
            val newUser = UserEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                email = email,
                phone = phone,
                city = city,
                area = area,
                points = 0,
                badgeLevel = "New Citizen",
                profileImageUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                totalReports = 0,
                totalVerifications = 0,
                resolvedReports = 0
            )
            userDao.insertUser(newUser)
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = e.message ?: "Could not create user account"
            val niceMsg = if (msg.contains("already registered", ignoreCase = true)) "User already registered. Please log in." else msg
            return@withContext Result.failure(Exception(niceMsg))
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.clearUser()
    }
}

// --- Issue Repository ---

class IssueRepository(
    private val issueDao: IssueDao,
    private val userDao: UserDao,
    private val notificationDao: NotificationDao,
    private val rewardDao: RewardDao,
    private val aiService: AiService
) {
    val allIssues: Flow<List<IssueEntity>> = issueDao.getAllIssues()

    fun getIssueById(id: String): Flow<IssueEntity?> = issueDao.getIssueById(id)

    suspend fun reportIssue(
        title: String,
        description: String,
        category: String,
        lat: Double,
        lon: Double,
        address: String,
        mediaUri: String?
    ): IssueEntity = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUserSync() ?: throw IllegalStateException("No user logged in")
        
        // Let's get AI analysis results
        val aiResult = aiService.analyzeIssue(title, description, null)

        // Check duplicate
        val existing = issueDao.getAllIssuesSync()
        val duplicateCheck = aiService.checkDuplicate(title, description, lat, lon, existing)

        val newIssue = IssueEntity(
            id = UUID.randomUUID().toString(),
            reporterId = user.id,
            title = title,
            description = description,
            category = if (category == "Auto-Detect") aiResult.category else category,
            aiCategory = aiResult.category,
            aiConfidenceScore = aiResult.confidenceScore,
            severity = aiResult.severity,
            severityScore = aiResult.severityScore,
            trustScore = aiResult.trustScore,
            status = "Reported",
            latitude = lat,
            longitude = lon,
            address = address,
            mediaUri = mediaUri,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            resolvedAt = null,
            verificationCount = 0,
            commentCount = 0,
            duplicateOf = if (duplicateCheck.duplicateFound) duplicateCheck.duplicateIssueId else null,
            assignedDepartment = aiResult.suggestedDepartment
        )

        issueDao.insertIssue(newIssue)

        // Insert Default Timeline Event
        issueDao.insertStatusUpdate(
            StatusUpdateEntity(
                id = UUID.randomUUID().toString(),
                issueId = newIssue.id,
                oldStatus = "",
                newStatus = "Reported",
                remarks = "Issue reported successfully by citizen.",
                proofMediaUri = null,
                createdAt = System.currentTimeMillis()
            )
        )

        // Add Gamification points (+10 for reporting)
        val updatedUser = user.copy(
            points = user.points + 10,
            totalReports = user.totalReports + 1,
            badgeLevel = calculateBadgeLevel(user.points + 10)
        )
        userDao.insertUser(updatedUser)

        // Log Reward
        rewardDao.insertReward(
            RewardEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                issueId = newIssue.id,
                pointsAwarded = 10,
                reason = "Reported new civic issue: ${newIssue.title}",
                createdAt = System.currentTimeMillis()
            )
        )

        // Create notification
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = "Issue Submitted Successfully",
                message = "Your issue '${newIssue.title}' has been recorded. AI has categorized it under ${newIssue.category}.",
                type = "Issue Submitted",
                read = false,
                createdAt = System.currentTimeMillis(),
                issueId = newIssue.id
            )
        )

        return@withContext newIssue
    }

    fun getCommentsForIssue(issueId: String): Flow<List<CommentEntity>> = issueDao.getCommentsForIssue(issueId)

    suspend fun addComment(issueId: String, commentText: String) = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUserSync() ?: return@withContext
        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            issueId = issueId,
            userId = user.id,
            userName = user.name,
            commentText = commentText,
            createdAt = System.currentTimeMillis()
        )
        issueDao.insertComment(comment)

        // Update comment count on issue
        val issue = issueDao.getIssueByIdSync(issueId)
        if (issue != null) {
            issueDao.insertIssue(issue.copy(commentCount = issue.commentCount + 1))
        }
    }

    fun getStatusUpdatesForIssue(issueId: String): Flow<List<StatusUpdateEntity>> = issueDao.getStatusUpdatesForIssue(issueId)

    private fun calculateBadgeLevel(points: Int): String {
        return when {
            points >= 1000 -> "Monthly Leader"
            points >= 750 -> "Top Verifier"
            points >= 500 -> "Road Safety Contributor"
            points >= 300 -> "Clean City Champion"
            points >= 150 -> "Civic Hero"
            else -> "First Reporter"
        }
    }
}

// --- Verification Repository ---

class VerificationRepository(
    private val verificationDao: VerificationDao,
    private val issueDao: IssueDao,
    private val userDao: UserDao,
    private val rewardDao: RewardDao,
    private val notificationDao: NotificationDao
) {
    fun getVerificationsForIssue(issueId: String): Flow<List<VerificationEntity>> =
        verificationDao.getVerificationsForIssue(issueId)

    suspend fun verifyIssue(
        issueId: String,
        verificationType: String, // Upvote, Resolved, Fake, More Evidence
        comment: String,
        mediaUri: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUserSync() ?: return@withContext false
        val issue = issueDao.getIssueByIdSync(issueId) ?: return@withContext false

        // Reporter cannot verify their own issue
        if (issue.reporterId == user.id) return@withContext false

        // Check if already verified
        val existingVerification = verificationDao.getVerificationByUser(issueId, user.id)
        if (existingVerification != null) return@withContext false

        val verification = VerificationEntity(
            id = UUID.randomUUID().toString(),
            issueId = issueId,
            userId = user.id,
            verificationType = verificationType,
            comment = comment,
            mediaUri = mediaUri,
            createdAt = System.currentTimeMillis()
        )
        verificationDao.insertVerification(verification)

        // Update issue counts and trust scores
        val pointsAwarded = 3
        var newTrustScore = issue.trustScore
        var newStatus = issue.status

        when (verificationType) {
            "Yes, this issue exists" -> {
                newTrustScore = (issue.trustScore + 10).coerceAtMost(100)
                if (issue.status == "Reported") {
                    newStatus = "Community Verified"
                }
            }
            "This issue is already resolved" -> {
                newTrustScore = (issue.trustScore + 5).coerceAtMost(100)
            }
            "This issue is fake or incorrect" -> {
                newTrustScore = (issue.trustScore - 20).coerceAtLeast(0)
            }
        }

        val updatedIssue = issue.copy(
            verificationCount = issue.verificationCount + 1,
            trustScore = newTrustScore,
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        issueDao.insertIssue(updatedIssue)

        // Insert Status Update on transition to Community Verified
        if (newStatus != issue.status) {
            issueDao.insertStatusUpdate(
                StatusUpdateEntity(
                    id = UUID.randomUUID().toString(),
                    issueId = issueId,
                    oldStatus = issue.status,
                    newStatus = newStatus,
                    remarks = "Community verified by citizens. Verification notes: $comment",
                    proofMediaUri = mediaUri,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        // Gamification: +3 points for verification
        val updatedUser = user.copy(
            points = user.points + pointsAwarded,
            totalVerifications = user.totalVerifications + 1,
            badgeLevel = calculateBadgeLevel(user.points + pointsAwarded)
        )
        userDao.insertUser(updatedUser)

        // Log Reward
        rewardDao.insertReward(
            RewardEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                issueId = issueId,
                pointsAwarded = pointsAwarded,
                reason = "Verified civic issue: ${issue.title}",
                createdAt = System.currentTimeMillis()
            )
        )

        // Create notification
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = "Points Earned!",
                message = "You earned +$pointsAwarded points for verifying the issue '${issue.title}'.",
                type = "Verification Reward",
                read = false,
                createdAt = System.currentTimeMillis(),
                issueId = issueId
            )
        )

        return@withContext true
    }

    private fun calculateBadgeLevel(points: Int): String {
        return when {
            points >= 1000 -> "Monthly Leader"
            points >= 750 -> "Top Verifier"
            points >= 500 -> "Road Safety Contributor"
            points >= 300 -> "Clean City Champion"
            points >= 150 -> "Civic Hero"
            else -> "First Reporter"
        }
    }
}

// --- Notification Repository ---

class NotificationRepository(
    private val notificationDao: NotificationDao
) {
    val notifications: Flow<List<NotificationEntity>> = notificationDao.getNotifications()

    suspend fun markAsRead(id: String) = withContext(Dispatchers.IO) {
        notificationDao.markNotificationRead(id)
    }

    suspend fun markAllAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }
}

// --- Reward Repository ---

class RewardRepository(
    private val rewardDao: RewardDao,
    private val userDao: UserDao
) {
    val myRewards: Flow<List<RewardEntity>> = rewardDao.getRewards()

    suspend fun getLeaderboard(): List<LeaderboardUser> = withContext(Dispatchers.IO) {
        // Simple mock leaderboard data
        listOf(
            LeaderboardUser(1, "Ankit Paul", "Hirapur", 460, "Civic Hero"),
            LeaderboardUser(2, "Rohan Sharma", "Saraidhela", 415, "Civic Hero"),
            LeaderboardUser(3, "Sneha Kumari", "Steel Gate", 380, "Clean City Champion"),
            LeaderboardUser(4, "Priya Mahto", "Jharia", 320, "Clean City Champion"),
            LeaderboardUser(5, "Vikram Singh", "Katras", 240, "Civic Hero"),
            LeaderboardUser(6, "Abhishek Raj", "Govindpur", 180, "Civic Hero"),
            LeaderboardUser(7, "Nisha Das", "Bank More", 125, "First Reporter")
        )
    }
}

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val area: String,
    val points: Int,
    val badge: String
)

// --- User Repository ---

class UserRepository(
    private val userDao: UserDao,
    private val feedbackDao: FeedbackDao,
    private val issueDao: IssueDao
) {
    val currentUser: Flow<UserEntity?> = userDao.getCurrentUser()

    suspend fun submitFeedback(
        issueId: String,
        rating: Int,
        comment: String,
        reopenRequested: Boolean
    ) = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUserSync() ?: return@withContext
        val feedback = FeedbackEntity(
            id = UUID.randomUUID().toString(),
            issueId = issueId,
            userId = user.id,
            rating = rating,
            comment = comment,
            reopenRequested = reopenRequested,
            createdAt = System.currentTimeMillis()
        )
        feedbackDao.insertFeedback(feedback)

        // Handle Reopen transition if requested
        if (reopenRequested) {
            val issue = issueDao.getIssueByIdSync(issueId)
            if (issue != null) {
                val updatedIssue = issue.copy(
                    status = "Reopened",
                    updatedAt = System.currentTimeMillis()
                )
                issueDao.insertIssue(updatedIssue)

                // Add status timeline update
                issueDao.insertStatusUpdate(
                    StatusUpdateEntity(
                        id = UUID.randomUUID().toString(),
                        issueId = issueId,
                        oldStatus = issue.status,
                        newStatus = "Reopened",
                        remarks = "Citizen requested reopen. Reason: $comment",
                        proofMediaUri = null,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}

// --- Database Seeder ---

class DatabaseSeeder(
    private val userDao: UserDao,
    private val issueDao: IssueDao,
    private val notificationDao: NotificationDao,
    private val rewardDao: RewardDao
) {
    suspend fun seedIfNeeded(customEmailOrPhone: String? = null) {
        val current = userDao.getCurrentUserSync()
        if (current == null) {
            val emailValue = if (customEmailOrPhone != null && customEmailOrPhone.contains("@")) customEmailOrPhone else "citizen@jansetu.ai"
            val phoneValue = if (customEmailOrPhone != null && !customEmailOrPhone.contains("@")) customEmailOrPhone else "9876543210"
            val derivedName = if (customEmailOrPhone != null && customEmailOrPhone.contains("@")) {
                val part = customEmailOrPhone.substringBefore("@")
                part.split(".", "_", "-").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            } else if (customEmailOrPhone != null && !customEmailOrPhone.contains("@")) {
                "Citizen Reporter"
            } else {
                "Ankit Paul"
            }

            val defaultUser = UserEntity(
                id = "ankit_paul_123",
                name = derivedName,
                email = emailValue,
                phone = phoneValue,
                city = "Dhanbad",
                area = "Hirapur",
                points = 460,
                badgeLevel = "Civic Hero",
                profileImageUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                totalReports = 0,
                totalVerifications = 0,
                resolvedReports = 0
            )
            userDao.insertUser(defaultUser)
        }
    }

    suspend fun seedDefaultIssuesAndContent(userId: String) {
        val count = issueDao.getAllIssuesSync().size

        // Always ensure the 3 custom screenshot issues exist for the current user
        val myPothole = issueDao.getIssueByIdSync("issue_my_pothole")
        if (myPothole == null) {
            val now = System.currentTimeMillis()
            val issue1 = IssueEntity(
                id = "issue_my_pothole",
                reporterId = userId,
                title = "Massive Pothole on 5th Ave",
                description = "There is a massive, deep pothole right on 5th Ave causing severe traffic delays and danger to motorcyclists.",
                category = "Infrastructure",
                aiCategory = "Pothole",
                aiConfidenceScore = 0.98,
                severity = "Critical",
                severityScore = 95,
                trustScore = 92,
                status = "Reported",
                latitude = 23.8122,
                longitude = 86.4425,
                address = "5th Ave, near central square",
                mediaUri = "https://images.unsplash.com/photo-1515162305285-0293e4767cc2?auto=format&fit=crop&w=500&q=80",
                createdAt = now - 3 * 24 * 3600 * 1000, // 3 days ago (e.g. Oct 24 style)
                updatedAt = now - 3 * 24 * 3600 * 1000,
                resolvedAt = null,
                verificationCount = 12,
                commentCount = 3,
                duplicateOf = null,
                assignedDepartment = "Roads & Highways Department"
            )
            issueDao.insertIssue(issue1)
            issueDao.insertStatusUpdate(
                StatusUpdateEntity(
                    id = UUID.randomUUID().toString(),
                    issueId = "issue_my_pothole",
                    oldStatus = "",
                    newStatus = "Reported",
                    remarks = "Issue reported successfully by citizen.",
                    proofMediaUri = null,
                    createdAt = now - 3 * 24 * 3600 * 1000
                )
            )
        }

        val myStreetlight = issueDao.getIssueByIdSync("issue_my_streetlight")
        if (myStreetlight == null) {
            val now = System.currentTimeMillis()
            val issue2 = IssueEntity(
                id = "issue_my_streetlight",
                reporterId = userId,
                title = "Hanging Streetlight Fixture",
                description = "A streetlight fixture is hanging loose by its wire over the pedestrian walkway, posing an immediate falling hazard.",
                category = "Utilities",
                aiCategory = "Broken Streetlight",
                aiConfidenceScore = 0.95,
                severity = "High",
                severityScore = 78,
                trustScore = 88,
                status = "Assigned",
                latitude = 23.8138,
                longitude = 86.4441,
                address = "3rd Cross Lane, near Metro Station",
                mediaUri = "https://images.unsplash.com/photo-1508144753681-9986d4df99b3?auto=format&fit=crop&w=500&q=80",
                createdAt = now - 5 * 24 * 3600 * 1000, // Oct 22 style
                updatedAt = now - 4 * 24 * 3600 * 1000,
                resolvedAt = null,
                verificationCount = 8,
                commentCount = 1,
                duplicateOf = null,
                assignedDepartment = "Electrical Infrastructure Dept"
            )
            issueDao.insertIssue(issue2)
            issueDao.insertStatusUpdate(
                StatusUpdateEntity(
                    id = UUID.randomUUID().toString(),
                    issueId = "issue_my_streetlight",
                    oldStatus = "",
                    newStatus = "Reported",
                    remarks = "Issue reported successfully by citizen.",
                    proofMediaUri = null,
                    createdAt = now - 5 * 24 * 3600 * 1000
                )
            )
            issueDao.insertStatusUpdate(
                StatusUpdateEntity(
                    id = UUID.randomUUID().toString(),
                    issueId = "issue_my_streetlight",
                    oldStatus = "Reported",
                    newStatus = "Assigned",
                    remarks = "Routed to Electrical Infrastructure Dept.",
                    proofMediaUri = null,
                    createdAt = now - 4 * 24 * 3600 * 1000
                )
            )
        }

        val myGraffiti = issueDao.getIssueByIdSync("issue_my_graffiti")
        if (myGraffiti == null) {
            val now = System.currentTimeMillis()
            val issue3 = IssueEntity(
                id = "issue_my_graffiti",
                reporterId = userId,
                title = "Graffiti on Transit Station",
                description = "Vandalism and spray paint graffiti on the walls of the local transit subway station.",
                category = "Vandalism",
                aiCategory = "Vandalism",
                aiConfidenceScore = 0.91,
                severity = "Low",
                severityScore = 24,
                trustScore = 70,
                status = "Resolved",
                latitude = 23.8149,
                longitude = 86.4419,
                address = "City Transit Center Metro Station",
                mediaUri = "https://images.unsplash.com/photo-1541535650810-10d26f5c2ab3?auto=format&fit=crop&w=500&q=80",
                createdAt = now - 12 * 24 * 3600 * 1000, // Oct 15 style
                updatedAt = now - 6 * 24 * 3600 * 1000,
                resolvedAt = now - 6 * 24 * 3600 * 1000,
                verificationCount = 15,
                commentCount = 6,
                duplicateOf = null,
                assignedDepartment = "Municipal Services & Transit Board"
            )
            issueDao.insertIssue(issue3)
            issueDao.insertStatusUpdate(
                StatusUpdateEntity(
                    id = UUID.randomUUID().toString(),
                    issueId = "issue_my_graffiti",
                    oldStatus = "",
                    newStatus = "Reported",
                    remarks = "Issue reported successfully by citizen.",
                    proofMediaUri = null,
                    createdAt = now - 12 * 24 * 3600 * 1000
                )
            )
            issueDao.insertStatusUpdate(
                StatusUpdateEntity(
                    id = UUID.randomUUID().toString(),
                    issueId = "issue_my_graffiti",
                    oldStatus = "Reported",
                    newStatus = "Resolved",
                    remarks = "Transit cleanup crew completed graffiti removal.",
                    proofMediaUri = null,
                    createdAt = now - 6 * 24 * 3600 * 1000
                )
            )
        }

        if (count > 0) return

        val now = System.currentTimeMillis()

        val demoIssues = listOf(
            IssueEntity(
                id = "issue_1",
                reporterId = userId,
                title = "Large pothole near college gate",
                description = "There is a massive, deep pothole right near the main entrance gate of SSLNT College. It causes bikes to skid, especially in the evening. Extremely dangerous for students.",
                category = "Pothole",
                aiCategory = "Pothole",
                aiConfidenceScore = 0.96,
                severity = "High",
                severityScore = 78,
                trustScore = 85,
                status = "Reported",
                latitude = 23.8122,
                longitude = 86.4425,
                address = "SSLNT College Gate, Luby Circular Road, Dhanbad, Jharkhand",
                mediaUri = "https://images.unsplash.com/photo-1515162305285-0293e4767cc2?auto=format&fit=crop&w=500&q=80",
                createdAt = now - (3 * 3600 * 1000), // 3 hours ago
                updatedAt = now - (3 * 3600 * 1000),
                resolvedAt = null,
                verificationCount = 4,
                commentCount = 2,
                duplicateOf = null,
                assignedDepartment = "Roads & Highways Department"
            ),
            IssueEntity(
                id = "issue_2",
                reporterId = "another_citizen_1",
                title = "Garbage overflow near market area",
                description = "The garbage dump container near the Hirapur local market is overflowing with waste. It has not been cleared for three days. Stray dogs are scattering trash everywhere. Foul smell.",
                category = "Garbage Overflow",
                aiCategory = "Garbage Overflow",
                aiConfidenceScore = 0.94,
                severity = "High",
                severityScore = 72,
                trustScore = 90,
                status = "Assigned",
                latitude = 23.8155,
                longitude = 86.4398,
                address = "Hirapur Market Chowk, Dhanbad, Jharkhand",
                mediaUri = "https://images.unsplash.com/photo-1611284446314-60a58ac0deb9?auto=format&fit=crop&w=500&q=80",
                createdAt = now - (24 * 3600 * 1000), // 1 day ago
                updatedAt = now - (12 * 3600 * 1000),
                resolvedAt = null,
                verificationCount = 8,
                commentCount = 3,
                duplicateOf = null,
                assignedDepartment = "Waste & Sanitation Department"
            ),
            IssueEntity(
                id = "issue_3",
                reporterId = "another_citizen_2",
                title = "Broken streetlight in residential lane",
                description = "The streetlight opposite House #42 in Lane 2 is completely broken and flickering. The entire lane becomes pitch dark after 6 PM, raising security concerns for women and elderly residents.",
                category = "Broken Streetlight",
                aiCategory = "Broken Streetlight",
                aiConfidenceScore = 0.91,
                severity = "Medium",
                severityScore = 52,
                trustScore = 75,
                status = "In Progress",
                latitude = 23.8138,
                longitude = 86.4441,
                address = "Lane 2, Near Carmel School, Hirapur, Dhanbad, Jharkhand",
                mediaUri = "https://images.unsplash.com/photo-1508144753681-9986d4df99b3?auto=format&fit=crop&w=500&q=80",
                createdAt = now - (2 * 24 * 3600 * 1000), // 2 days ago
                updatedAt = now - (24 * 3600 * 1000),
                resolvedAt = null,
                verificationCount = 5,
                commentCount = 1,
                duplicateOf = null,
                assignedDepartment = "Electrical Infrastructure Dept"
            ),
            IssueEntity(
                id = "issue_4",
                reporterId = userId,
                title = "Water pipeline leakage near main road",
                description = "A clean drinking water supply pipeline is burst and leaking thousands of liters of water right near Court Road. It has created a small waterlogged stream on the street, causing minor traffic jams.",
                category = "Water Leakage",
                aiCategory = "Water Leakage",
                aiConfidenceScore = 0.98,
                severity = "Critical",
                severityScore = 88,
                trustScore = 95,
                status = "Resolved",
                latitude = 23.8149,
                longitude = 86.4419,
                address = "Opposite District Court, Court Road, Dhanbad, Jharkhand",
                mediaUri = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?auto=format&fit=crop&w=500&q=80",
                createdAt = now - (5 * 24 * 3600 * 1000), // 5 days ago
                updatedAt = now - (2 * 24 * 3600 * 1000),
                resolvedAt = now - (2 * 24 * 3600 * 1000),
                verificationCount = 12,
                commentCount = 5,
                duplicateOf = null,
                assignedDepartment = "Municipal Water & Sewerage Board"
            ),
            IssueEntity(
                id = "issue_5",
                reporterId = "another_citizen_3",
                title = "Blocked drainage causing street logging",
                description = "Heavy plastic waste and silt have completely blocked the open drain corner. The gutter water is now flowing onto the pedestrian walk and road, causing unhygienic conditions.",
                category = "Drainage Blockage",
                aiCategory = "Drainage Blockage",
                aiConfidenceScore = 0.93,
                severity = "High",
                severityScore = 70,
                trustScore = 80,
                status = "Reported",
                latitude = 23.8115,
                longitude = 86.4385,
                address = "Near Bus Stand Road, Dhanbad, Jharkhand",
                mediaUri = null,
                createdAt = now - (1 * 3600 * 1000), // 1 hour ago
                updatedAt = now - (1 * 3600 * 1000),
                resolvedAt = null,
                verificationCount = 1,
                commentCount = 0,
                duplicateOf = null,
                assignedDepartment = "Municipal Water & Sewerage Board"
            )
        )

        for (issue in demoIssues) {
            issueDao.insertIssue(issue)

            // Seed status timeline
            issueDao.insertStatusUpdate(
                StatusUpdateEntity(
                    id = UUID.randomUUID().toString(),
                    issueId = issue.id,
                    oldStatus = "",
                    newStatus = "Reported",
                    remarks = "Report successfully registered in the platform.",
                    proofMediaUri = null,
                    createdAt = issue.createdAt
                )
            )

            if (issue.status != "Reported") {
                issueDao.insertStatusUpdate(
                    StatusUpdateEntity(
                        id = UUID.randomUUID().toString(),
                        issueId = issue.id,
                        oldStatus = "Reported",
                        newStatus = "AI Categorized",
                        remarks = "AI processed title and description. Issue routed to '${issue.assignedDepartment}'.",
                        proofMediaUri = null,
                        createdAt = issue.createdAt + 2000
                    )
                )
            }

            if (issue.status == "Assigned" || issue.status == "In Progress" || issue.status == "Resolved") {
                issueDao.insertStatusUpdate(
                    StatusUpdateEntity(
                        id = UUID.randomUUID().toString(),
                        issueId = issue.id,
                        oldStatus = "AI Categorized",
                        newStatus = "Community Verified",
                        remarks = "Verified by over 5 local residents who confirmed its existence.",
                        proofMediaUri = null,
                        createdAt = issue.createdAt + 12 * 3600 * 1000
                    )
                )
                issueDao.insertStatusUpdate(
                    StatusUpdateEntity(
                        id = UUID.randomUUID().toString(),
                        issueId = issue.id,
                        oldStatus = "Community Verified",
                        newStatus = "Assigned",
                        remarks = "Assigned to field team under '${issue.assignedDepartment}'. Officer: R.K. Pandey.",
                        proofMediaUri = null,
                        createdAt = issue.createdAt + 18 * 3600 * 1000
                    )
                )
            }

            if (issue.status == "In Progress" || issue.status == "Resolved") {
                issueDao.insertStatusUpdate(
                    StatusUpdateEntity(
                        id = UUID.randomUUID().toString(),
                        issueId = issue.id,
                        oldStatus = "Assigned",
                        newStatus = "In Progress",
                        remarks = "Repair team has arrived on site and started resolution work.",
                        proofMediaUri = null,
                        createdAt = issue.createdAt + 22 * 3600 * 1000
                    )
                )
            }

            if (issue.status == "Resolved") {
                issueDao.insertStatusUpdate(
                    StatusUpdateEntity(
                        id = UUID.randomUUID().toString(),
                        issueId = issue.id,
                        oldStatus = "In Progress",
                        newStatus = "Resolved",
                        remarks = "Resolution complete. Leakage sealed, road patched. Officer uploaded completion proof.",
                        proofMediaUri = "https://images.unsplash.com/photo-1541888946425-d81bb19240f5?auto=format&fit=crop&w=500&q=80",
                        createdAt = issue.resolvedAt ?: (issue.createdAt + 36 * 3600 * 1000)
                    )
                )
            }

            // Seed Comments
            if (issue.id == "issue_1") {
                issueDao.insertComment(
                    CommentEntity(
                        id = "comment_1",
                        issueId = issue.id,
                        userId = "another_citizen_1",
                        userName = "Rohan Sharma",
                        commentText = "Agreed, I almost fell off my scooty here last night. It's really deep!",
                        createdAt = issue.createdAt + 15 * 60 * 1000
                    )
                )
                issueDao.insertComment(
                    CommentEntity(
                        id = "comment_2",
                        issueId = issue.id,
                        userId = "another_citizen_3",
                        userName = "Sneha Kumari",
                        commentText = "We should get more people to upvote this so the department takes it up on priority.",
                        createdAt = issue.createdAt + 45 * 60 * 1000
                    )
                )
            }
        }

        // Seed notifications
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_1",
                userId = userId,
                title = "Water pipeline leakage resolved",
                message = "Great news! The pipeline leakage issue you reported near Court Road has been marked as RESOLVED. Please provide feedback.",
                type = "Issue Resolved",
                read = false,
                createdAt = now - (2 * 3600 * 1000),
                issueId = "issue_4"
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_2",
                userId = userId,
                title = "New points earned!",
                message = "Your verification of 'Broken Streetlight near Carmel School' was marked genuine. You earned +3 points.",
                type = "Verification Reward",
                read = true,
                createdAt = now - (1 * 24 * 3600 * 1000),
                issueId = "issue_3"
            )
        )

        // Seed Rewards History
        rewardDao.insertReward(
            RewardEntity(
                id = "reward_1",
                userId = userId,
                issueId = "issue_4",
                pointsAwarded = 10,
                reason = "Reported Water pipeline leakage",
                createdAt = now - (5 * 24 * 3600 * 1000)
            )
        )
        rewardDao.insertReward(
            RewardEntity(
                id = "reward_2",
                userId = userId,
                issueId = "issue_4",
                pointsAwarded = 15,
                reason = "Report confirmed genuine by community",
                createdAt = now - (4 * 24 * 3600 * 1000)
            )
        )
        rewardDao.insertReward(
            RewardEntity(
                id = "reward_3",
                userId = userId,
                issueId = "issue_3",
                pointsAwarded = 3,
                reason = "Verified Broken streetlight report",
                createdAt = now - (1 * 24 * 3600 * 1000)
            )
        )
    }
}
