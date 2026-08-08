package com.example.ui.screens.social

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.RewardEntity
import com.example.data.repository.LeaderboardUser
import com.example.ui.screens.main.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CivicViewModel

// --- NotificationsScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: CivicViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    var filterUnread by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OutlineColor)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllNotificationsRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.9f))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { filterUnread = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!filterUnread) Primary else SurfaceContainerHighest, contentColor = if (!filterUnread) OnPrimary else OnSurface),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("All", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = { filterUnread = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (filterUnread) Primary else SurfaceContainerHighest, contentColor = if (filterUnread) OnPrimary else OnSurface),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Unread", style = MaterialTheme.typography.labelMedium)
                    }
                }
                Text(
                    text = "MARK ALL READ",
                    style = MaterialTheme.typography.labelSmall,
                    color = OutlineColor,
                    modifier = Modifier.clickable { viewModel.markAllNotificationsRead() }
                )
            }

            val displayedNotifs = if (filterUnread) notifications.filter { !it.read } else notifications

            if (displayedNotifs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = OutlineColor, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No notifications", color = OnSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    items(displayedNotifs) { notif ->
                        NotificationItem(
                            notification = notif,
                            onClick = { viewModel.markNotificationRead(notif.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .testTag("notification_item_${notification.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (!notification.read) BorderStroke(1.dp, SecondaryFixed) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (!notification.read) SecondaryFixed.copy(alpha = 0.2f) else SurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (!notification.read) Secondary else OutlineColor
                )
                if (!notification.read) {
                    Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp).size(12.dp).clip(CircleShape).background(SecondaryFixed).border(2.dp, SurfaceCard, CircleShape))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(notification.title, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold), color = OnSurface)
                    Text(
                        text = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall, color = OutlineColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.message, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }
        }
    }
}

// --- LeaderboardScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: CivicViewModel,
    onBack: () -> Unit
) {
    val leaderboardUsers by viewModel.leaderboard.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var isWeekly by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadLeaderboard()
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("JanSetu AI", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = OutlineColor)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = OutlineColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.9f))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Text("Top Contributors", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface, modifier = Modifier.padding(bottom = 24.dp))
            
            // Toggle
            Row(
                modifier = Modifier
                    .background(SurfaceContainerLow, RoundedCornerShape(50))
                    .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { isWeekly = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isWeekly) SurfaceCard else Color.Transparent, contentColor = if (isWeekly) OnSurface else OutlineColor),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isWeekly) 2.dp else 0.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("Weekly", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = { isWeekly = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isWeekly) SurfaceCard else Color.Transparent, contentColor = if (!isWeekly) OnSurface else OutlineColor),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (!isWeekly) 2.dp else 0.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("Monthly", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Top 3 Podium Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2
                Box(modifier = Modifier.weight(1f)) {
                    leaderboardUsers.getOrNull(1)?.let { u ->
                        TopUserCard(user = u, rank = 2, isFirst = false)
                    }
                }
                
                // Rank 1
                Box(modifier = Modifier.weight(1.2f).padding(bottom = 16.dp)) {
                    leaderboardUsers.getOrNull(0)?.let { u ->
                        TopUserCard(user = u, rank = 1, isFirst = true)
                    }
                }
                
                // Rank 3
                Box(modifier = Modifier.weight(1f)) {
                    leaderboardUsers.getOrNull(2)?.let { u ->
                        TopUserCard(user = u, rank = 3, isFirst = false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Standings list
            Column(modifier = Modifier.fillMaxWidth()) {
                val listUsers = leaderboardUsers.drop(3)
                listUsers.forEachIndexed { index, user ->
                    val actualRank = index + 4
                    val isMe = user.name == (currentUser?.name ?: "")
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(if (isMe) SurfaceContainerHighest else SurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, if (isMe) SecondaryFixed else Color.Transparent, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actualRank.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.labelMedium,
                            color = OutlineColor,
                            modifier = Modifier.width(32.dp)
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainer)
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isMe) "You" else user.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                            Text("${user.area}", style = MaterialTheme.typography.labelSmall, color = OutlineColor)
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(SurfaceContainerLow, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("${user.points}", style = MaterialTheme.typography.labelMedium, color = Primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopUserCard(user: LeaderboardUser, rank: Int, isFirst: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isFirst) 2.dp else 1.dp, if (isFirst) SecondaryFixed else SurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFirst) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(if (isFirst) 72.dp else 56.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant)
                    .border(if (isFirst) 4.dp else 2.dp, if (isFirst) SecondaryFixed else SurfaceVariant, CircleShape)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "RANK 0$rank",
                style = MaterialTheme.typography.labelSmall,
                color = if (isFirst) SecondaryFixed else OutlineColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                user.name,
                style = if (isFirst) MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                user.area,
                style = MaterialTheme.typography.labelSmall,
                color = OutlineColor,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Box(
                modifier = Modifier
                    .background(if (isFirst) SecondaryFixed else SurfaceContainerLow, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "${user.points} pts",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isFirst) OnSecondaryFixed else Primary
                )
            }
        }
    }
}

// --- RewardsScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    viewModel: CivicViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val rewardsHistory by viewModel.myRewards.collectAsState()

    val badges = listOf(
        Pair("First Reporter", Pair("Reported 1st issue", true) to Icons.Default.Flag),
        Pair("Civic Hero", Pair("Reached 250 points", true) to Icons.Default.EmojiEvents),
        Pair("Clean Champion", Pair("10 cleanup reports", false) to Icons.Default.Recycling),
        Pair("Hot Streak", Pair("Report 5 days in a row", false) to Icons.Default.LocalFireDepartment)
    )

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "JanSetu AI", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_location_svgrepo), contentDescription = "Location", tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_remind_svgrepo), contentDescription = "Notifications", tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.9f))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header Section
            Text("My Rewards", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = Primary)
            Text("Your contribution to a better city.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

            // Points card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, SecondaryFixed),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Box(modifier = Modifier.background(SurfaceContainerLow, RoundedCornerShape(50)).border(1.dp, OutlineVariant.copy(alpha=0.5f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Current Status", style = MaterialTheme.typography.labelMedium, color = OnSurface)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${currentUser?.points ?: 0}", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), color = Primary)
                        Text(" pts", style = MaterialTheme.typography.headlineSmall, color = OutlineColor, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currentUser?.badgeLevel?.uppercase() ?: "NEW CITIZEN", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SecondaryFixedDim, letterSpacing = 1.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Next Badge: City Legend", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        Text("500 pts", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Primary)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(SurfaceContainerHigh, RoundedCornerShape(50)).border(1.dp, OutlineVariant.copy(alpha=0.3f), RoundedCornerShape(50))) {
                        val currentPts = currentUser?.points ?: 0
                        val progress = (currentPts / 500f).coerceIn(0f, 1f)
                        Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(SecondaryFixed, RoundedCornerShape(50)))
                    }
                    
                    val remaining = (500 - (currentUser?.points ?: 0)).coerceAtLeast(0)
                    Text("$remaining points remaining", style = MaterialTheme.typography.labelSmall, color = OutlineColor, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.End)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tasks List
            BoostCivicScoreSection(showViewAll = false)
        }
    }
}

@Composable
fun BadgeCard(item: Pair<String, Pair<Pair<String, Boolean>, androidx.compose.ui.graphics.vector.ImageVector>>, modifier: Modifier = Modifier) {
    val name = item.first
    val desc = item.second.first.first
    val unlocked = item.second.first.second
    val icon = item.second.second

    Card(
        modifier = modifier.testTag("badge_card_$name"),
        colors = CardDefaults.cardColors(containerColor = if (unlocked) SurfaceCard else SurfaceCard.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (unlocked) Color.Transparent else OutlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unlocked) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) SurfaceContainer else SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (unlocked) SecondaryFixed else OutlineColor,
                    modifier = Modifier.size(40.dp)
                )
                if (unlocked) {
                    Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp).size(24.dp).clip(CircleShape).background(SurfaceCard).border(1.dp, OutlineVariant.copy(alpha=0.3f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp).size(24.dp).clip(CircleShape).background(SurfaceCard).border(1.dp, OutlineVariant.copy(alpha=0.3f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = OutlineColor, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (unlocked) Primary else OnSurface, textAlign = TextAlign.Center)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

// --- SettingsScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CivicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isPushEnabled by remember { mutableStateOf(true) }
    var isSmsEnabled by remember { mutableStateOf(false) }
    var isAnonymousDefault by remember { mutableStateOf(false) }
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.9f))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // User Profile Section
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                            .border(2.dp, SurfaceCard, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = (currentUser?.name ?: "C").take(1).uppercase()
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 36.sp
                            ),
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Column {
                        Text(currentUser?.name ?: "Citizen", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                        Text(currentUser?.email ?: "citizen@example.com", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(SecondaryFixed.copy(alpha = 0.2f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = OnSecondaryFixed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Verified Citizen", style = MaterialTheme.typography.labelSmall, color = OnSecondaryFixed)
                        }
                    }
                }
            }
            
            // General Settings Group
            Text("GENERAL", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant, letterSpacing = 1.sp, modifier = Modifier.padding(start = 8.dp, bottom = 16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    SettingsActionRow(icon = Icons.Default.ManageAccounts, title = "Account Settings")
                    Divider(color = SurfaceContainerHighest.copy(alpha = 0.3f))
                    SettingsToggleRow(icon = Icons.Default.Notifications, title = "Notifications", checked = isPushEnabled, onCheckedChange = { isPushEnabled = it })
                    Divider(color = SurfaceContainerHighest.copy(alpha = 0.3f))
                    SettingsActionRow(icon = Icons.Default.Language, title = "Language", endText = "English")
                }
            }

            // Security & Legal Group
            Text("SECURITY & LEGAL", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant, letterSpacing = 1.sp, modifier = Modifier.padding(start = 8.dp, bottom = 16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    SettingsToggleRow(icon = Icons.Default.Lock, title = "Anonymous Reporting", checked = isAnonymousDefault, onCheckedChange = { isAnonymousDefault = it })
                    Divider(color = SurfaceContainerHighest.copy(alpha = 0.3f))
                    SettingsActionRow(icon = Icons.Default.Help, title = "Help & Support")
                    Divider(color = SurfaceContainerHighest.copy(alpha = 0.3f))
                    SettingsActionRow(icon = Icons.Default.Policy, title = "Terms & Privacy Policy", endIcon = Icons.Default.OpenInNew)
                }
            }

            // Logout Button
            Button(
                onClick = { Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("logout_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = CriticalRed),
                border = BorderStroke(1.dp, OutlineVariant)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    endText: String? = null,
    endIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.ChevronRight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = OnSurface)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = OnSurface, modifier = Modifier.weight(1f))
        
        if (endText != null) {
            Text(endText, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
        }
        Icon(endIcon, contentDescription = null, tint = OutlineVariant)
    }
}

@Composable
fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(if (checked) SecondaryFixed.copy(alpha=0.2f) else SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (checked) Secondary else OnSurface)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = OnSurface, modifier = Modifier.weight(1f))
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnSecondaryFixed,
                checkedTrackColor = SecondaryFixed,
                uncheckedThumbColor = OutlineColor,
                uncheckedTrackColor = SurfaceContainerHighest
            )
        )
    }
}
