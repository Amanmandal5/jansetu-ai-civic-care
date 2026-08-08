package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val city: String,
    val area: String,
    val points: Int,
    val badgeLevel: String,
    val profileImageUrl: String,
    val totalReports: Int,
    val totalVerifications: Int,
    val resolvedReports: Int
)

@Entity(tableName = "issues")
data class IssueEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val title: String,
    val description: String,
    val category: String,
    val aiCategory: String,
    val aiConfidenceScore: Double,
    val severity: String, // Low, Medium, High, Critical
    val severityScore: Int,
    val trustScore: Int,
    val status: String, // Reported, AI Categorized, Community Verified, Assigned, In Progress, Resolved, Citizen Confirmed, Closed, Reopened, Rejected
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val mediaUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val resolvedAt: Long?,
    val verificationCount: Int,
    val commentCount: Int,
    val duplicateOf: String?,
    val assignedDepartment: String?
)

@Entity(tableName = "verifications")
data class VerificationEntity(
    @PrimaryKey val id: String,
    val issueId: String,
    val userId: String,
    val verificationType: String, // Upvote/Exists, Resolved, Fake, More Evidence
    val comment: String,
    val mediaUri: String?,
    val createdAt: Long
)

@Entity(tableName = "status_updates")
data class StatusUpdateEntity(
    @PrimaryKey val id: String,
    val issueId: String,
    val oldStatus: String,
    val newStatus: String,
    val remarks: String,
    val proofMediaUri: String?,
    val createdAt: Long
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val issueId: String,
    val userId: String,
    val userName: String,
    val commentText: String,
    val createdAt: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: Long,
    val issueId: String?
)

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val issueId: String?,
    val pointsAwarded: Int,
    val reason: String,
    val createdAt: Long
)

@Entity(tableName = "feedbacks")
data class FeedbackEntity(
    @PrimaryKey val id: String,
    val issueId: String,
    val userId: String,
    val rating: Int,
    val comment: String,
    val reopenRequested: Boolean,
    val createdAt: Long
)
