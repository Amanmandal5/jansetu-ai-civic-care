package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.ui.components.common.AppScaffold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CivicViewModel
import com.example.ui.viewmodel.CivicViewModelFactory

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.remove()
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
            )
        )

        // Obtain AppContainer from Application instance (Production Clean DI Architecture)
        val appContainer = (application as CivicApplication).appContainer

        // Instantiate central ViewModel using ViewModelFactory and AppContainer
        val civicViewModel: CivicViewModel = ViewModelProvider(
            this,
            CivicViewModelFactory(
                application = application,
                authRepository = appContainer.authRepository,
                issueRepository = appContainer.issueRepository,
                verificationRepository = appContainer.verificationRepository,
                notificationRepository = appContainer.notificationRepository,
                rewardRepository = appContainer.rewardRepository,
                userRepository = appContainer.userRepository,
                locationService = appContainer.locationService,
                aiService = appContainer.aiService
            )
        )[CivicViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppScaffold(viewModel = civicViewModel)
                }
            }
        }
    }
}
