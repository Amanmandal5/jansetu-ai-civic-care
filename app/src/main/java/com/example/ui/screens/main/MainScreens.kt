package com.example.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.IssueEntity
import com.example.R
import androidx.compose.ui.res.painterResource
import com.example.ui.theme.*
import com.example.ui.viewmodel.CivicViewModel

// --- Reusable Component: Issue Card ---

@Composable
fun IssueCard(
    issue: IssueEntity,
    onClick: () -> Unit
) {
    val severityBgColor = when (issue.severity.lowercase()) {
        "critical" -> HighSeverityOrange // In tailwind config: severity-critical: DC2626
        "high" -> HighSeverityOrange // severity-high: F97316
        "medium" -> WarningYellow
        else -> SuccessGreen
    }

    val severityTextColor = Color.White

    val statusColor = when (issue.status.lowercase()) {
        "resolved" -> SuccessGreen
        "closed" -> OutlineColor
        "in progress" -> Primary
        "assigned" -> Primary.copy(alpha = 0.8f)
        "community verified" -> SuccessGreen.copy(alpha = 0.8f)
        else -> WarningYellow
    }

    val distance = "${(issue.id.hashCode() % 400 + 100).coerceAtLeast(80)}m away"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
            .testTag("issue_card_${issue.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Image Thumbnail or Placeholder
            if (issue.mediaUri != null) {
                AsyncImage(
                    model = issue.mediaUri,
                    contentDescription = issue.title,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = OutlineColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f).height(96.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Top Row: Title & Severity Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = issue.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        
                        // Severity Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(severityBgColor)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = issue.severity.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = severityTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Reported near $distance. ${issue.description}",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom Row: Status and Verification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "AI Verified",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Primary
                        )
                    }

                    // Status Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SurfaceContainerHighest)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = issue.status,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// --- Reusable Component: Local Impact Progress Gauge ---
@Composable
fun LocalImpactProgress(
    modifier: Modifier = Modifier,
    value: Int,
    max: Int = 1000,
    color: Color = SecondaryFixed
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(105.dp)) {
            val strokeWidth = 8.dp.toPx()
            // Background track
            drawArc(
                color = Color(0xFFE5E7EB),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Active track
            val sweepAngle = (value.toFloat() / max.toFloat()) * 270f
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                ),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "SOLVED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFF6B7280)
            )
        }
    }
}

// --- Reusable Component: Calendar Week Strip ---
@Composable
fun CalendarWeekStrip() {
    var selectedDay by remember { mutableStateOf(10) }
    val daysOfWeek = listOf(
        Pair("S", "07"),
        Pair("M", "08"),
        Pair("T", "09"),
        Pair("W", "10"),
        Pair("T", "11"),
        Pair("F", "12"),
        Pair("S", "13")
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("calendar_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header: Month, Year + Navigation arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "August 2025",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = Color.Black
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { /* Previous Month */ }
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { /* Next Month */ }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Week days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { dayPair ->
                    val dayNum = dayPair.second.toInt()
                    val isSelected = selectedDay == dayNum
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedDay = dayNum }
                    ) {
                        Text(
                            text = dayPair.first,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9CA3AF)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SecondaryFixed else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayPair.second,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Reusable Component: Pothole Icon (3 Orange Lanes) ---
@Composable
fun PotholeIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFF1EB)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val laneColor = Color(0xFFF97316)
            val strokeWidth = 3.dp.toPx()
            
            // Left dashed line
            drawLine(
                color = laneColor,
                start = androidx.compose.ui.geometry.Offset(x = size.width * 0.25f, y = size.height * 0.15f),
                end = androidx.compose.ui.geometry.Offset(x = size.width * 0.25f, y = size.height * 0.85f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            // Middle dashed lines
            drawLine(
                color = laneColor,
                start = androidx.compose.ui.geometry.Offset(x = size.width * 0.5f, y = size.height * 0.15f),
                end = androidx.compose.ui.geometry.Offset(x = size.width * 0.5f, y = size.height * 0.4f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = laneColor,
                start = androidx.compose.ui.geometry.Offset(x = size.width * 0.5f, y = size.height * 0.6f),
                end = androidx.compose.ui.geometry.Offset(x = size.width * 0.5f, y = size.height * 0.85f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            // Right dashed line
            drawLine(
                color = laneColor,
                start = androidx.compose.ui.geometry.Offset(x = size.width * 0.75f, y = size.height * 0.15f),
                end = androidx.compose.ui.geometry.Offset(x = size.width * 0.75f, y = size.height * 0.85f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

// --- Reusable Component: Garbage Trash Icon ---
@Composable
fun GarbageIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFE6FDF4)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Garbage",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(24.dp)
        )
    }
}

// --- Reusable Component: Quick Report Action Card ---
@Composable
fun QuickReportCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                icon()
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            // Circular thin outline button with + sign
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                    .clip(CircleShape)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// --- Redesigned HomeScreen ---
@Composable
fun HomeScreen(
    viewModel: CivicViewModel,
    onNavigateToReport: () -> Unit,
    onNavigateToIssueDetails: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToNearbyReports: () -> Unit,
    onNavigateToRewards: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allIssues by viewModel.allIssues.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp)
    ) {
        // Top Profile Greeting Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = (currentUser?.name ?: "C").take(1).uppercase()
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            ),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Good morning!",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = currentUser?.name ?: "Citizen",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = Color.Black
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification Icon with red dot
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onNavigateToNotifications() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.remind_svgrepo_com),
                            contentDescription = "Notifications",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Gamification / Local Impact Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left: Your Local Impact Card
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(240.dp)
                        .testTag("gamification_impact_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE7FAB6)), // Beautiful light neon yellow-green
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "Power",
                                    tint = OnSecondaryFixed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "COMMUNITY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = OnSecondaryFixed
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your Local\nImpact",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    lineHeight = 24.sp
                                ),
                                color = Color.Black
                            )
                        }
                        
                        // Circular Progress Tracker
                        LocalImpactProgress(
                            value = currentUser?.points ?: 0,
                            max = 1000,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                
                // Right: Column of Civic Hero and Reports Cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Right-Top: Civic Hero Card
                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxWidth()
                            .clickable { onNavigateToRewards() }
                            .testTag("gamification_card"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Civic Hero",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = Color.Gray
                                )
                                // Green Star Icon in transparent Circle
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(1.dp, SecondaryFixed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = SecondaryFixed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "${currentUser?.points ?: 0}",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp
                                    ),
                                    color = Color.Black
                                )
                                Text(
                                    text = "Points earned",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    // Right-Bottom: Reports Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable { /* Active reports */ },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reports",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = Color.Gray
                                )
                                // Blue Waterdrop Icon
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Reports",
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "${currentUser?.totalReports ?: 0}",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp
                                    ),
                                    color = Color.Black
                                )
                                Text(
                                    text = "Active issues",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Boost Civic Score Section
        item {
            BoostCivicScoreSection(onNavigateToRewards)
        }

        // Nearby Reports section title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Reports",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = Color.Black
                )
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    ),
                    modifier = Modifier.clickable {
                        onNavigateToNearbyReports()
                    }
                )
            }
        }

        if (allIssues.isEmpty()) {
            item {
                Text(
                    text = "No nearby reports there", 
                    modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(), 
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            items(allIssues.take(5)) { issue ->
                IssueCard(
                    issue = issue,
                    onClick = { onNavigateToIssueDetails(issue.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = subtext, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// --- MyReportsScreen Helpers ---

@Composable
fun SeverityBadge(severity: String) {
    val (bgColor, textColor) = when (severity.lowercase()) {
        "critical" -> Color(0xFFFEE2E2) to Color(0xFFDC2626) // light pink/red and red
        "high" -> Color(0xFFFFEDD5) to Color(0xFFEA580C) // light orange and orange
        "medium" -> Color(0xFFFEF9C3) to Color(0xFFCA8A04) // light yellow and dark yellow
        else -> Color(0xFFF3F4F6) to Color(0xFF6B7280) // light grey and grey
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = severity.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = textColor
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, icon) = when (status.lowercase()) {
        "resolved" -> Triple(Color(0xFFD1FAE5), Color(0xFF10B981), Icons.Default.Check)
        "assigned" -> Triple(Color(0xFFF3F4F6), Color(0xFF374151), Icons.Default.Engineering)
        "in progress" -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), Icons.Default.Autorenew)
        else -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), null)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(11.dp)
                )
            }
            Text(
                text = status,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun VerifiedBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFECFDF5))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF047857), // emerald-700
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = "Verified",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF047857)
            )
        }
    }
}

@Composable
fun MyReportItemCard(
    issue: IssueEntity,
    onClick: () -> Unit
) {
    val dateStr = remember(issue.createdAt) {
        val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.US)
        sdf.format(java.util.Date(issue.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .testTag("issue_card_${issue.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        
        // Left: circular crop of image
            if (issue.mediaUri != null) {
                AsyncImage(
                    model = issue.mediaUri,
                    contentDescription = issue.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.3f), CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(Color(0xFFE0F2FE), Color(0xFFD1FAE5), Color(0xFFFFEDD5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right: Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Category + Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = issue.category.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title (with line-through if resolved)
                val isResolved = issue.status.equals("resolved", ignoreCase = true)
                Text(
                    text = issue.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isResolved) Color(0xFF9CA3AF) else Color.Black,
                    textDecoration = if (isResolved) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Badge Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeverityBadge(severity = issue.severity)
                    
                    // If it's verified
                    if (issue.id == "issue_my_pothole" || issue.verificationCount >= 10) {
                        VerifiedBadge()
                    }

                    StatusBadge(status = issue.status)
                }
            }
        }
    }
}

@Composable
fun FilterStatusChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) Color(0xFFCCFF00) else Color.White)
            .border(
                1.dp,
                if (active) Color(0xFFCCFF00) else Color(0xFFE5E7EB),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (text == "Verified") {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (active) Color.Black else Color(0xFF6B7280),
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                color = if (active) Color.Black else Color(0xFF4B5563),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- MyReportsScreen ---

@Composable
fun MyReportsScreen(
    viewModel: CivicViewModel,
    onNavigateToIssueDetails: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToRewards: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allIssues by viewModel.allIssues.collectAsState()

    val userIssues = remember(allIssues, currentUser) {
        allIssues.filter { it.reporterId == currentUser?.id }
    }

    var selectedStatusTab by remember { mutableStateOf("All") }
    val statusTabs = listOf("All", "Reported", "Verified", "Assigned")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp)
    ) {
        // Top Custom Navigation Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onNavigateBack() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Jan Setu AI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Box {
                IconButton(
                    onClick = { /* No-op or notification click */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.remind_svgrepo_com),
                        contentDescription = "Notifications",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "My Reports",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle - dynamically calculated based on userIssues count
        val totalCount = userIssues.size
        val resolvedCount = userIssues.count { it.status.equals("resolved", ignoreCase = true) }
        val subtitleText = "You have submitted $totalCount reports. $resolvedCount have been resolved."

        Text(
            text = subtitleText,
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontally scrolling filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(statusTabs) { tab ->
                val active = selectedStatusTab == tab
                FilterStatusChip(
                    text = tab,
                    active = active,
                    onClick = { selectedStatusTab = tab }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredUserIssues = remember(selectedStatusTab, userIssues) {
            when (selectedStatusTab) {
                "All" -> userIssues
                "Verified" -> userIssues.filter { it.id == "issue_my_pothole" || it.verificationCount >= 10 }
                else -> userIssues.filter { it.status.equals(selectedStatusTab, ignoreCase = true) }
            }
        }

        if (filteredUserIssues.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No reports generated.",
                        color = Color(0xFF6B7280),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredUserIssues) { issue ->
                    MyReportItemCard(
                        issue = issue,
                        onClick = { onNavigateToIssueDetails(issue.id) }
                    )
                }
            }
        }
    }
}

// --- ProfileScreen ---

@Composable
fun ProfileScreen(
    viewModel: CivicViewModel,
    onNavigateToMyReports: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = (currentUser?.name ?: "C").take(1).uppercase()
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 48.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentUser?.name ?: "Ankit Paul",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = currentUser?.email ?: "citizen@jansetu.ai",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LightBlue)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentUser?.badgeLevel ?: "New Citizen",
                            color = PrimaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatBox(
                modifier = Modifier.weight(1f),
                label = "Reports",
                value = "${currentUser?.totalReports ?: 0}",
                color = PrimaryBlue
            )
            ProfileStatBox(
                modifier = Modifier.weight(1f),
                label = "Verifications",
                value = "${currentUser?.totalVerifications ?: 0}",
                color = SuccessGreen
            )
            ProfileStatBox(
                modifier = Modifier.weight(1f),
                label = "Resolved",
                value = "${currentUser?.resolvedReports ?: 0}",
                color = HighSeverityOrange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Menu Items
        Text("Account Menu", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        ProfileMenuItem(
            iconPainter = painterResource(id = R.drawable.remind_svgrepo_com),
            title = "My Reported Tickets",
            onClick = onNavigateToMyReports
        )
        ProfileMenuItem(
            iconPainter = painterResource(id = R.drawable.shop_svgrepo_com),
            title = "Rewards & Achievements",
            onClick = onNavigateToRewards
        )
        ProfileMenuItem(
            iconPainter = painterResource(id = R.drawable.set_up_svgrepo_com),
            title = "App Settings",
            onClick = onNavigateToSettings
        )
        ProfileMenuItem(
            iconVector = Icons.Default.ExitToApp,
            title = "Sign Out / Logout",
            onClick = {
                viewModel.logout()
                onLogoutSuccess()
            },
            textColor = CriticalRed
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ProfileStatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfileMenuItem(
    iconVector: ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    title: String,
    onClick: () -> Unit,
    textColor: Color = TextPrimary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconPainter != null) {
                    Icon(painter = iconPainter, contentDescription = null, tint = if (textColor == CriticalRed) CriticalRed else PrimaryBlue, modifier = Modifier.size(20.dp))
                } else if (iconVector != null) {
                    Icon(imageVector = iconVector, contentDescription = null, tint = if (textColor == CriticalRed) CriticalRed else PrimaryBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, fontSize = 15.sp, color = textColor, fontWeight = FontWeight.SemiBold)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

// --- Nearby Reports Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyReportsScreen(
    viewModel: CivicViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToIssueDetails: (String) -> Unit
) {
    val allIssues by viewModel.allIssues.collectAsState()

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { 
                    Text("Nearby Reports", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black)) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (allIssues.isEmpty()) {
                item {
                    Text(
                        text = "No nearby issues found.",
                        modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                items(allIssues) { issue ->
                    IssueCard(
                        issue = issue,
                        onClick = { onNavigateToIssueDetails(issue.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BoostCivicScoreSection(
    onNavigateToRewards: () -> Unit = {},
    showViewAll: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Boost Your Civic Score",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = Color(0xFF111827)
            )
            if (showViewAll) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569) // Slate gray
                    ),
                    modifier = Modifier.clickable { onNavigateToRewards() }
                )
            }
        }

        CivicScoreTaskCard(
            icon = { Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = Color(0xFF1E2D24)) },
            title = "Report Issues (+50 pts)",
            subtitle = "Capture new problems with clear photos",
            onClick = onNavigateToRewards
        )

        CivicScoreTaskCard(
            icon = { Icon(Icons.Outlined.FactCheck, contentDescription = null, tint = Color(0xFF1E2D24)) },
            title = "Verify Reports (+20 pts)",
            subtitle = "Confirm nearby active issues",
            onClick = onNavigateToRewards
        )

        CivicScoreTaskCard(
            icon = { Icon(Icons.Outlined.Update, contentDescription = null, tint = Color(0xFF1E2D24)) },
            title = "Follow Up (+10 pts)",
            subtitle = "Provide updates on existing reports",
            onClick = onNavigateToRewards
        )
    }
}

@Composable
fun CivicScoreTaskCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF8CEF34)) // Neon green base for thick left border effect
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp), // Exposes the thick green line on the left
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color(0xFF8CEF34))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F8E9)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F2937),
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Star icon
                Icon(
                    imageVector = Icons.Outlined.Stars,
                    contentDescription = "Reward",
                    tint = Color(0xFF8CEF34),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
