package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.navigation.Routes
import com.example.ui.screens.auth.*
import com.example.ui.screens.detail.*
import com.example.ui.screens.main.*
import com.example.ui.screens.map.*
import com.example.ui.screens.report.*
import com.example.ui.screens.social.*
import com.example.ui.screens.splash.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CivicViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: CivicViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING,
        modifier = modifier
    ) {
        // 1. Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding Screen
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // 3. Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 4. Sign Up Screen
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                onSignUpSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                }
            )
        }

        // 5. Home Screen
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToReport = { navController.navigate(Routes.REPORT_ISSUE) },
                onNavigateToIssueDetails = { id -> navController.navigate(Routes.buildIssueDetails(id)) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onNavigateToLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onNavigateToNearbyReports = { navController.navigate(Routes.NEARBY) },
                onNavigateToRewards = { navController.navigate(Routes.REWARDS) }
            )
        }

        // 6. Map Screen
        composable(Routes.MAP) {
            CivicMapScreen(
                viewModel = viewModel,
                onNavigateToIssueDetails = { id -> navController.navigate(Routes.buildIssueDetails(id)) }
            )
        }

        // 7. My Reports Screen
        composable(Routes.MY_REPORTS) {
            MyReportsScreen(
                viewModel = viewModel,
                onNavigateToIssueDetails = { id -> navController.navigate(Routes.buildIssueDetails(id)) },
                onNavigateBack = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
            )
        }

        // Nearby Reports Screen
        composable(Routes.NEARBY) {
            NearbyReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToIssueDetails = { id -> navController.navigate(Routes.buildIssueDetails(id)) }
            )
        }

        // 8. Profile Screen
        composable(Routes.PROFILE) {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToMyReports = { navController.navigate(Routes.MY_REPORTS) },
                onNavigateToRewards = { navController.navigate(Routes.REWARDS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onLogoutSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 9. Report Issue Screen
        composable(Routes.REPORT_ISSUE) {
            ReportIssueScreen(
                viewModel = viewModel,
                onNavigateToAiAnalysis = { title, desc, lat, lon, addr ->
                    navController.navigate(Routes.buildAiAnalysis(title, desc, lat, lon, addr))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 10. AI Analysis Screen
        composable(
            route = Routes.AI_ANALYSIS,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
            val desc = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("description") ?: "", "UTF-8")
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
            val addr = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("address") ?: "", "UTF-8")

            AIAnalysisScreen(
                viewModel = viewModel,
                title = title,
                description = desc,
                lat = lat,
                lon = lon,
                address = addr,
                onNavigateToDuplicate = { dupId ->
                    navController.navigate(Routes.buildDuplicateSuggestion(dupId, title, desc, lat, lon, addr))
                },
                onNavigateToSuccess = { issueId, cat, sev ->
                    navController.navigate(Routes.buildSuccess(issueId, cat, sev)) {
                        popUpTo(Routes.REPORT_ISSUE) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 11. Duplicate Suggestion Screen
        composable(
            route = Routes.DUPLICATE_SUGGESTION,
            arguments = listOf(
                navArgument("issueId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
            val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
            val desc = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("description") ?: "", "UTF-8")
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
            val addr = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("address") ?: "", "UTF-8")

            DuplicateSuggestionScreen(
                viewModel = viewModel,
                duplicateId = issueId,
                title = title,
                description = desc,
                lat = lat,
                lon = lon,
                address = addr,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSuccess = { newId, cat, sev ->
                    navController.navigate(Routes.buildSuccess(newId, cat, sev)) {
                        popUpTo(Routes.REPORT_ISSUE) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 12. Success Confirmation Screen
        composable(
            route = Routes.SUCCESS,
            arguments = listOf(
                navArgument("issueId") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType },
                navArgument("severity") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val severity = backStackEntry.arguments?.getString("severity") ?: ""

            SuccessScreen(
                issueId = issueId,
                category = category,
                severity = severity,
                onNavigateToDetails = { id ->
                    navController.navigate(Routes.buildIssueDetails(id)) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 13. Issue Details Tracking Screen
        composable(
            route = Routes.ISSUE_DETAILS,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType })
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
            IssueDetailsScreen(
                viewModel = viewModel,
                issueId = issueId,
                onNavigateToVerify = { id -> navController.navigate(Routes.buildVerification(id)) },
                onNavigateToFeedback = { id -> navController.navigate(Routes.buildFeedback(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        // 14. Verification Voting Screen
        composable(
            route = Routes.VERIFICATION,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType })
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
            CommunityVerificationScreen(
                viewModel = viewModel,
                issueId = issueId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 15. Resolution Feedback Star Rating Screen
        composable(
            route = Routes.FEEDBACK,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType })
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
            FeedbackScreen(
                viewModel = viewModel,
                issueId = issueId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 16. Inbox Notifications Screen
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 17. Leaderboard Screen
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 18. Rewards Screen
        composable(Routes.REWARDS) {
            RewardsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 19. Settings Screen
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
