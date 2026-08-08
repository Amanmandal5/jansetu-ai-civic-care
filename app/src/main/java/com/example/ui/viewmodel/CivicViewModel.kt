package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.ai.AiAnalysisResult
import com.example.core.ai.AiService
import com.example.core.ai.DuplicateCheckResult
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.*
import com.example.core.location.LocationService
import com.example.core.location.UserLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AiAnalysisState {
    object Idle : AiAnalysisState()
    object AnalyzingImage : AiAnalysisState()
    object ReadingDescription : AiAnalysisState()
    object Categorizing : AiAnalysisState()
    object CheckingDuplicates : AiAnalysisState()
    object CalculatingSeverity : AiAnalysisState()
    object GeneratingTrustScore : AiAnalysisState()
    data class Success(val result: AiAnalysisResult) : AiAnalysisState()
    data class Error(val error: String) : AiAnalysisState()
}

class CivicViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val issueRepository: IssueRepository,
    private val verificationRepository: VerificationRepository,
    private val notificationRepository: NotificationRepository,
    private val rewardRepository: RewardRepository,
    private val userRepository: UserRepository,
    private val locationService: LocationService,
    private val aiService: AiService
) : AndroidViewModel(application) {

    // --- State Observables ---

    val currentUser: StateFlow<UserEntity?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allIssues: StateFlow<List<IssueEntity>> = issueRepository.allIssues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = notificationRepository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myRewards: StateFlow<List<RewardEntity>> = rewardRepository.myRewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _leaderboard = MutableStateFlow<List<LeaderboardUser>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardUser>> = _leaderboard.asStateFlow()

    // Location States for Reporting
    private val _reportLocation = MutableStateFlow<UserLocation?>(null)
    val reportLocation: StateFlow<UserLocation?> = _reportLocation.asStateFlow()

    private val _locationLoading = MutableStateFlow(false)
    val locationLoading: StateFlow<Boolean> = _locationLoading.asStateFlow()

    // AI Analysis States
    private val _aiState = MutableStateFlow<AiAnalysisState>(AiAnalysisState.Idle)
    val aiState: StateFlow<AiAnalysisState> = _aiState.asStateFlow()

    private val _duplicateResult = MutableStateFlow<DuplicateCheckResult?>(null)
    val duplicateResult: StateFlow<DuplicateCheckResult?> = _duplicateResult.asStateFlow()

    // Detail Screen States
    private val _activeIssueId = MutableStateFlow<String?>(null)
    
    val activeIssue: StateFlow<IssueEntity?> = _activeIssueId
        .flatMapLatest { id ->
            if (id != null) issueRepository.getIssueById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeIssueComments: StateFlow<List<CommentEntity>> = _activeIssueId
        .flatMapLatest { id ->
            if (id != null) issueRepository.getCommentsForIssue(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeIssueTimeline: StateFlow<List<StatusUpdateEntity>> = _activeIssueId
        .flatMapLatest { id ->
            if (id != null) issueRepository.getStatusUpdatesForIssue(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeIssueVerifications: StateFlow<List<VerificationEntity>> = _activeIssueId
        .flatMapLatest { id ->
            if (id != null) verificationRepository.getVerificationsForIssue(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Operation States
    private val _loginState = MutableStateFlow<Result<Unit>?>(null)
    val loginState = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow<Result<Unit>?>(null)
    val signUpState = _signUpState.asStateFlow()

    // Prefill helper states for Report Screen
    private val _prefilledTitle = MutableStateFlow("")
    val prefilledTitle: StateFlow<String> = _prefilledTitle.asStateFlow()

    private val _prefilledDescription = MutableStateFlow("")
    val prefilledDescription: StateFlow<String> = _prefilledDescription.asStateFlow()

    fun setPrefilledReport(title: String, description: String) {
        _prefilledTitle.value = title
        _prefilledDescription.value = description
    }

    fun clearPrefilledReport() {
        _prefilledTitle.value = ""
        _prefilledDescription.value = ""
    }

    init {
        // Automatically check/seed database on ViewModel creation
        viewModelScope.launch {
            loadLeaderboard()
        }
    }

    // --- Actions ---

    fun loginUser(email: String, pwhash: String) {
        viewModelScope.launch {
            _loginState.value = null
            _loginState.value = authRepository.login(email, pwhash)
        }
    }

    fun signUpUser(name: String, email: String, phone: String, pwhash: String, city: String, area: String) {
        viewModelScope.launch {
            _signUpState.value = null
            _signUpState.value = authRepository.signUp(name, email, phone, pwhash, city, area)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = null
            _signUpState.value = null
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _leaderboard.value = rewardRepository.getLeaderboard()
        }
    }

    fun detectLocation() {
        viewModelScope.launch {
            _locationLoading.value = true
            try {
                val loc = locationService.getCurrentLocation()
                _reportLocation.value = loc
            } catch (e: Exception) {
                _reportLocation.value = locationService.getDefaultLocation()
            } finally {
                _locationLoading.value = false
            }
        }
    }

    fun setManualLocation(lat: Double, lon: Double, address: String) {
        viewModelScope.launch {
            val userLoc = locationService.getAddressFromLocation(lat, lon)
            _reportLocation.value = userLoc.copy(address = address)
        }
    }

    fun resetAiState() {
        _aiState.value = AiAnalysisState.Idle
        _duplicateResult.value = null
    }

    fun analyzeIssueWithAi(title: String, description: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _aiState.value = AiAnalysisState.AnalyzingImage
            delay(1000)
            
            _aiState.value = AiAnalysisState.ReadingDescription
            delay(1000)

            _aiState.value = AiAnalysisState.Categorizing
            delay(1000)
            val result = aiService.analyzeIssue(title, description, null)

            _aiState.value = AiAnalysisState.CheckingDuplicates
            delay(1000)
            val duplicates = aiService.checkDuplicate(title, description, lat, lon, allIssues.value)
            _duplicateResult.value = duplicates

            _aiState.value = AiAnalysisState.CalculatingSeverity
            delay(1000)

            _aiState.value = AiAnalysisState.GeneratingTrustScore
            delay(1000)

            _aiState.value = AiAnalysisState.Success(result)
        }
    }

    fun submitReportedIssue(
        title: String,
        description: String,
        category: String,
        lat: Double,
        lon: Double,
        address: String,
        mediaUri: String?,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val issue = issueRepository.reportIssue(title, description, category, lat, lon, address, mediaUri)
                onComplete(issue.id)
            } catch (e: Exception) {
                Log.e("CivicViewModel", "Error reporting issue: ${e.message}")
            }
        }
    }

    fun getIssueById(id: String, onComplete: (IssueEntity?) -> Unit) {
        viewModelScope.launch {
            issueRepository.getIssueById(id).take(1).collect {
                onComplete(it)
            }
        }
    }

    fun setActiveIssue(issueId: String) {
        _activeIssueId.value = issueId
    }

    fun addCommentToActiveIssue(text: String) {
        val issueId = _activeIssueId.value ?: return
        viewModelScope.launch {
            issueRepository.addComment(issueId, text)
        }
    }

    fun verifyActiveIssue(verificationType: String, comment: String, mediaUri: String?, onComplete: (Boolean) -> Unit) {
        val issueId = _activeIssueId.value ?: return
        viewModelScope.launch {
            val success = verificationRepository.verifyIssue(issueId, verificationType, comment, mediaUri)
            onComplete(success)
        }
    }

    fun verifyIssue(issueId: String, verificationType: String, comment: String, mediaUri: String?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = verificationRepository.verifyIssue(issueId, verificationType, comment, mediaUri)
            onComplete(success)
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun submitActiveIssueFeedback(rating: Int, comment: String, reopen: Boolean) {
        val issueId = _activeIssueId.value ?: return
        viewModelScope.launch {
            userRepository.submitFeedback(issueId, rating, comment, reopen)
        }
    }
}

// --- ViewModel Factory ---

class CivicViewModelFactory(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val issueRepository: IssueRepository,
    private val verificationRepository: VerificationRepository,
    private val notificationRepository: NotificationRepository,
    private val rewardRepository: RewardRepository,
    private val userRepository: UserRepository,
    private val locationService: LocationService,
    private val aiService: AiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CivicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CivicViewModel(
                application,
                authRepository,
                issueRepository,
                verificationRepository,
                notificationRepository,
                rewardRepository,
                userRepository,
                locationService,
                aiService
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
