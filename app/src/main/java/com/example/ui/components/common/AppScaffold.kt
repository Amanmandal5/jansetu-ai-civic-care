package com.example.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.navigation.AppNavigation
import com.example.navigation.Routes
import com.example.ui.theme.*
import com.example.ui.viewmodel.CivicViewModel
import androidx.compose.ui.res.painterResource
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    viewModel: CivicViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom navigation on core screens
    val bottomBarRoutes = listOf(
        Routes.HOME,
        Routes.MAP,
        Routes.MY_REPORTS,
        Routes.PROFILE
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = {
            if (showBottomBar) {
                // Customized elevated Bottom Navigation Bar containing standard tabs & central Report FAB
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .offset(y = 20.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    NavigationBar(
                        containerColor = SurfaceContainerLowest,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("app_bottom_navigation_bar")
                    ) {
                        // 1. Home Tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.HOME,
                            onClick = {
                                if (currentRoute != Routes.HOME) {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(painter = painterResource(id = R.drawable.home_page_svgrepo_com), contentDescription = "Home", modifier = Modifier.size(24.dp)) },
                            label = { Text("Home", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color.Black,
                                unselectedIconColor = Color(0xFF9CA3AF),
                                unselectedTextColor = Color(0xFF9CA3AF),
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("bottom_tab_home")
                        )

                        // 2. Map Tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.MAP,
                            onClick = {
                                if (currentRoute != Routes.MAP) {
                                    navController.navigate(Routes.MAP) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(painter = painterResource(id = R.drawable.location_svgrepo_com), contentDescription = "Map", modifier = Modifier.size(24.dp)) },
                            label = { Text("Map", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color.Black,
                                unselectedIconColor = Color(0xFF9CA3AF),
                                unselectedTextColor = Color(0xFF9CA3AF),
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("bottom_tab_map")
                        )

                        // Placeholder for Center FAB spacing
                        Spacer(modifier = Modifier.weight(1f))

                        // 3. My Reports Tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.MY_REPORTS,
                            onClick = {
                                if (currentRoute != Routes.MY_REPORTS) {
                                    navController.navigate(Routes.MY_REPORTS) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(painter = painterResource(id = R.drawable.ticket_information_svgrepo_com), contentDescription = "My Reports", modifier = Modifier.size(24.dp)) },
                            label = { Text("Reports", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color.Black,
                                unselectedIconColor = Color(0xFF9CA3AF),
                                unselectedTextColor = Color(0xFF9CA3AF),
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("bottom_tab_my_reports")
                        )

                        // 4. Profile Tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.PROFILE,
                            onClick = {
                                if (currentRoute != Routes.PROFILE) {
                                    navController.navigate(Routes.PROFILE) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(painter = painterResource(id = R.drawable.user_svgrepo_com), contentDescription = "Profile", modifier = Modifier.size(24.dp)) },
                            label = { Text("Profile", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color.Black,
                                unselectedIconColor = Color(0xFF9CA3AF),
                                unselectedTextColor = Color(0xFF9CA3AF),
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("bottom_tab_profile")
                        )
                    }

                    // Center Floating Action Button (FAB)
                    FloatingActionButton(
                        onClick = { navController.navigate(Routes.REPORT_ISSUE) },
                        containerColor = SecondaryFixed,
                        contentColor = OnSecondaryFixed,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .size(64.dp)
                            .testTag("center_report_fab")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_to_svgrepo_com),
                            contentDescription = "Report Issue",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize().padding(bottom = if (showBottomBar) 60.dp else 0.dp) // Offset content so it doesn't overlap the custom bottom navigation
        )
    }
}
