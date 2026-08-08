package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}

@Dao
interface IssueDao {
    @Query("SELECT * FROM issues ORDER BY createdAt DESC")
    fun getAllIssues(): Flow<List<IssueEntity>>

    @Query("DELETE FROM issues")
    suspend fun clearIssues()

    @Query("SELECT * FROM issues ORDER BY createdAt DESC")
    suspend fun getAllIssuesSync(): List<IssueEntity>

    @Query("SELECT * FROM issues WHERE id = :id")
    fun getIssueById(id: String): Flow<IssueEntity?>

    @Query("SELECT * FROM issues WHERE id = :id")
    suspend fun getIssueByIdSync(id: String): IssueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: IssueEntity)

    @Update
    suspend fun updateIssue(issue: IssueEntity)

    @Query("SELECT * FROM comments WHERE issueId = :issueId ORDER BY createdAt DESC")
    fun getCommentsForIssue(issueId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("SELECT * FROM status_updates WHERE issueId = :issueId ORDER BY createdAt ASC")
    fun getStatusUpdatesForIssue(issueId: String): Flow<List<StatusUpdateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusUpdate(statusUpdate: StatusUpdateEntity)
}

@Dao
interface VerificationDao {
    @Query("SELECT * FROM verifications WHERE issueId = :issueId ORDER BY createdAt DESC")
    fun getVerificationsForIssue(issueId: String): Flow<List<VerificationEntity>>

    @Query("SELECT * FROM verifications WHERE issueId = :issueId AND userId = :userId LIMIT 1")
    suspend fun getVerificationByUser(issueId: String, userId: String): VerificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerification(verification: VerificationEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET read = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)

    @Query("UPDATE notifications SET read = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM rewards ORDER BY createdAt DESC")
    fun getRewards(): Flow<List<RewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardEntity)
}

@Dao
interface FeedbackDao {
    @Query("SELECT * FROM feedbacks WHERE issueId = :issueId ORDER BY createdAt DESC")
    fun getFeedbacksForIssue(issueId: String): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity)
}
