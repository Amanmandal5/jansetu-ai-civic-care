package com.example.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val SIGN_UP = "signup"
    const val LOGIN = "login"
    
    // Bottom Nav Destinations
    const val HOME = "home"
    const val MAP = "map"
    const val MY_REPORTS = "my_reports"
    const val PROFILE = "profile"
    
    // Secondary Screens
    const val REPORT_ISSUE = "report_issue"
    const val NEARBY = "nearby"
    const val NOTIFICATIONS = "notifications"
    const val LEADERBOARD = "leaderboard"
    const val REWARDS = "rewards"
    const val SETTINGS = "settings"
    
    // Parameterized Screens
    const val AI_ANALYSIS = "ai_analysis/{title}/{description}/{lat}/{lon}/{address}"
    fun buildAiAnalysis(title: String, desc: String, lat: Double, lon: Double, address: String): String {
        val safeTitle = java.net.URLEncoder.encode(title.ifBlank { "Civic Issue" }, "UTF-8")
        val safeDesc = java.net.URLEncoder.encode(desc.ifBlank { "No description provided" }, "UTF-8")
        val safeAddress = java.net.URLEncoder.encode(address.ifBlank { "Current Location" }, "UTF-8")
        return "ai_analysis/$safeTitle/$safeDesc/$lat/$lon/$safeAddress"
    }

    const val DUPLICATE_SUGGESTION = "duplicate_suggestion/{issueId}/{title}/{description}/{lat}/{lon}/{address}"
    fun buildDuplicateSuggestion(issueId: String, title: String, desc: String, lat: Double, lon: Double, address: String): String {
        val safeIssueId = issueId.ifBlank { "none" }
        val safeTitle = java.net.URLEncoder.encode(title.ifBlank { "Civic Issue" }, "UTF-8")
        val safeDesc = java.net.URLEncoder.encode(desc.ifBlank { "No description provided" }, "UTF-8")
        val safeAddress = java.net.URLEncoder.encode(address.ifBlank { "Current Location" }, "UTF-8")
        return "duplicate_suggestion/$safeIssueId/$safeTitle/$safeDesc/$lat/$lon/$safeAddress"
    }

    const val SUCCESS = "success/{issueId}/{category}/{severity}"
    fun buildSuccess(issueId: String, category: String, severity: String): String {
        val safeIssueId = issueId.ifBlank { "none" }
        val safeCat = java.net.URLEncoder.encode(category.ifBlank { "General" }, "UTF-8")
        val safeSev = java.net.URLEncoder.encode(severity.ifBlank { "Medium" }, "UTF-8")
        return "success/$safeIssueId/$safeCat/$safeSev"
    }

    const val ISSUE_DETAILS = "issue_details/{issueId}"
    fun buildIssueDetails(issueId: String): String {
        val safeIssueId = issueId.ifBlank { "none" }
        return "issue_details/$safeIssueId"
    }

    const val VERIFICATION = "verification/{issueId}"
    fun buildVerification(issueId: String): String {
        val safeIssueId = issueId.ifBlank { "none" }
        return "verification/$safeIssueId"
    }

    const val FEEDBACK = "feedback/{issueId}"
    fun buildFeedback(issueId: String): String {
        val safeIssueId = issueId.ifBlank { "none" }
        return "feedback/$safeIssueId"
    }
}
