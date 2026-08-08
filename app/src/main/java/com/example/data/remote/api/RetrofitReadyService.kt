package com.example.data.remote.api

import retrofit2.http.*

// --- Data Transfer Objects (DTOs) ---

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password_hash: String,
    val city: String,
    val area: String,
    val role: String = "citizen"
)

data class RegisterResponse(
    val success: Boolean,
    val token: String,
    val userId: String
)

data class LoginRequest(
    val email_or_phone: String,
    val password_hash: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String,
    val userId: String
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val city: String,
    val area: String,
    val points: Int,
    val badge_level: String,
    val profile_image_url: String,
    val total_reports: Int,
    val total_verifications: Int,
    val resolved_reports: Int
)

data class ReportIssueRequest(
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val media_url: String?
)

data class ReportIssueResponse(
    val success: Boolean,
    val issue_id: String,
    val ai_category: String,
    val severity: String,
    val duplicate_detected: Boolean,
    val status: String
)

data class IssueDto(
    val id: String,
    val reporter_id: String,
    val title: String,
    val description: String,
    val category: String,
    val ai_category: String,
    val ai_confidence_score: Double,
    val severity: String,
    val severity_score: Int,
    val trust_score: Int,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val media_uri: String?,
    val created_at: Long,
    val updated_at: Long,
    val resolved_at: Long?,
    val verification_count: Int,
    val comment_count: Int,
    val duplicate_of: String?,
    val assigned_department: String?
)

data class IssueDetailDto(
    val issue: IssueDto,
    val verifications: List<VerificationDto>,
    val status_updates: List<StatusUpdateDto>,
    val comments: List<CommentDto>
)

data class VerificationDto(
    val id: String,
    val issue_id: String,
    val user_id: String,
    val verification_type: String,
    val comment: String,
    val media_url: String?,
    val created_at: Long
)

data class VerifyRequest(
    val verification_type: String, // Upvote, Resolved, Fake, More Evidence
    val comment: String,
    val media_url: String?
)

data class VerifyResponse(
    val success: Boolean,
    val updated_verification_count: Int,
    val updated_trust_score: Int
)

data class StatusUpdateDto(
    val id: String,
    val issue_id: String,
    val old_status: String,
    val new_status: String,
    val remarks: String,
    val proof_media_url: String?,
    val created_at: Long
)

data class CommentRequest(
    val comment_text: String
)

data class CommentDto(
    val id: String,
    val issue_id: String,
    val user_id: String,
    val user_name: String,
    val comment_text: String,
    val created_at: Long
)

data class RewardsDto(
    val total_points: Int,
    val badge_level: String,
    val history: List<RewardEntryDto>
)

data class RewardEntryDto(
    val id: String,
    val points_awarded: Int,
    val reason: String,
    val created_at: Long
)

data class LeaderboardUserDto(
    val rank: Int,
    val name: String,
    val area: String,
    val points: Int,
    val badge: String
)

data class LeaderboardDto(
    val top_users: List<LeaderboardUserDto>,
    val user_rank: Int
)

data class NotificationDto(
    val id: String,
    val user_id: String,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val created_at: Long,
    val issue_id: String?
)

data class FeedbackRequest(
    val issue_id: String,
    val rating: Int,
    val comment: String,
    val reopen_requested: Boolean
)

data class FeedbackResponse(
    val success: Boolean,
    val message: String
)

// --- Retrofit API Interfaces ---

interface RetrofitReadyService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/auth/me")
    suspend fun getMe(): UserDto

    @POST("api/issues/report")
    suspend fun reportIssue(@Body request: ReportIssueRequest): ReportIssueResponse

    @GET("api/issues")
    suspend fun getIssues(
        @Query("category") category: String? = null,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null,
        @Query("city") city: String? = null
    ): List<IssueDto>

    @GET("api/issues/{id}")
    suspend fun getIssueById(@Path("id") id: String): IssueDetailDto

    @POST("api/issues/{id}/verify")
    suspend fun verifyIssue(
        @Path("id") id: String,
        @Body request: VerifyRequest
    ): VerifyResponse

    @GET("api/issues/{id}/verifications")
    suspend fun getVerifications(@Path("id") id: String): List<VerificationDto>

    @POST("api/issues/{id}/comments")
    suspend fun addComment(
        @Path("id") id: String,
        @Body request: CommentRequest
    ): CommentDto

    @GET("api/issues/{id}/comments")
    suspend fun getComments(@Path("id") id: String): List<CommentDto>

    @GET("api/rewards/me")
    suspend fun getMyRewards(): RewardsDto

    @GET("api/rewards/leaderboard")
    suspend fun getLeaderboard(): LeaderboardDto

    @GET("api/notifications")
    suspend fun getNotifications(): List<NotificationDto>

    @PATCH("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): NotificationDto

    @POST("api/feedback")
    suspend fun submitFeedback(@Body request: FeedbackRequest): FeedbackResponse
}
