package com.example.ui.screens.detail

import com.example.R
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.IssueEntity
import com.example.data.local.entity.StatusUpdateEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.CivicViewModel

// --- IssueDetailsScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailsScreen(
    viewModel: CivicViewModel,
    issueId: String,
    onNavigateToVerify: (String) -> Unit,
    onNavigateToFeedback: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val issue by viewModel.activeIssue.collectAsState()
    val timeline by viewModel.activeIssueTimeline.collectAsState()
    val comments by viewModel.activeIssueComments.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(issueId) {
        viewModel.setActiveIssue(issueId)
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Issue Details", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(painter = androidx.compose.ui.res.painterResource(id = R.drawable.share_svgrepo_com), contentDescription = "Share", tint = OnSurface, modifier = Modifier.size(24.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.9f))
            )
        }
    ) { innerPadding ->
        if (issue == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SecondaryFixed)
            }
        } else {
            val item = issue!!
            val severityColor = when (item.severity.lowercase()) {
                "critical" -> CriticalRed
                "high" -> HighSeverityOrange
                "medium" -> WarningYellow
                else -> SuccessGreen
            }

            val statusColor = when (item.status.lowercase()) {
                "resolved" -> SuccessGreen
                "closed" -> OutlineColor
                "in progress" -> Primary
                else -> WarningYellow
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Header Image or Placeholder
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                        if (item.mediaUri != null) {
                            AsyncImage(
                                model = item.mediaUri,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = OutlineColor, modifier = Modifier.size(56.dp))
                            }
                        }
                        
                        // Floating Severity Badge
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(severityColor, RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = OnError, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.severity.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = OnError)
                        }
                        
                        // Glassmorphic Status Overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .background(Surface.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(48.dp).background(statusColor, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("CURRENT STATUS", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text(item.status.uppercase(), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Title, Category, and Details
                item {
                    Column(modifier = Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(24.dp)).padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.background(SurfaceContainer, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text(item.category.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = OutlineColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("2 hrs ago", style = MaterialTheme.typography.labelSmall, color = OutlineColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(item.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)

                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = OutlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(item.address, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = OnSurface)
                                Text("Location based on GPS", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(item.description, style = MaterialTheme.typography.bodyLarge, color = OnSurface, lineHeight = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Community Action Card
                item {
                    Column(modifier = Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(24.dp)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("12", style = MaterialTheme.typography.displayMedium, color = Primary)
                        Text("CITIZENS VERIFIED", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Adding your verification increases priority for city repair crews.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { onNavigateToVerify(item.id) },
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("verify_complaint_details_button"),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryFixed, contentColor = OnSecondaryFixed)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("VERIFY ISSUE", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        if (item.status.lowercase() == "resolved") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onNavigateToFeedback(item.id) },
                                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("rate_resolution_feedback_button"),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("RATE SOLUTION", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Official Status Timeline
                item {
                    Text("Action Timeline", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface, modifier = Modifier.padding(bottom = 16.dp))
                }

                if (timeline.isEmpty()) {
                    item {
                        Text("Timeline is currently empty.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    }
                } else {
                    items(timeline) { step ->
                        StatusTimelineRow(step)
                    }
                }

                // Comments Header
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Discussion Board", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    Text("Post updates or tag municipal workers below", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp, top = 4.dp))

                    // Input Field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Write a comment...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input_field"),
                            shape = RoundedCornerShape(50),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecondaryFixed,
                                unfocusedBorderColor = SurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                if (commentText.isNotEmpty()) {
                                    viewModel.addCommentToActiveIssue(commentText)
                                    commentText = ""
                                }
                            },
                            modifier = Modifier
                                .background(SecondaryFixed, CircleShape)
                                .size(56.dp)
                                .testTag("post_comment_button")
                        ) {
                            Icon(painter = androidx.compose.ui.res.painterResource(id = R.drawable.send_2_svgrepo_com), contentDescription = "Send", tint = OnSecondaryFixed, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // Comments List
                if (comments.isEmpty()) {
                    item {
                        Text("No discussions yet. Be the first to comment!", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
                    }
                } else {
                    items(comments) { comment ->
                        CommentRow(comment)
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }
}

@Composable
fun StatusTimelineRow(update: StatusUpdateEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(SecondaryFixed)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(56.dp)
                    .background(SurfaceVariant)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(update.newStatus, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
            Text(update.remarks, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            Text(
                text = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(update.createdAt),
                style = MaterialTheme.typography.labelSmall, color = OutlineColor, modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun CommentRow(comment: CommentEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(comment.userName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    text = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(comment.createdAt),
                    fontSize = 11.sp, color = TextSecondary
                )
            }
            Text(comment.commentText, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

// --- CommunityVerificationScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityVerificationScreen(
    viewModel: CivicViewModel,
    issueId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isPotholeActuallyThere by remember { mutableStateOf(true) }
    var locationIsAccurate by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Verify this Issue", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = OnSurface)
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                
                // Context Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(16.dp))
                        .border(1.dp, SecondaryFixed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = OutlineColor)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                            Box(modifier = Modifier.background(HighSeverityOrange, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("High Priority", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                            Box(modifier = Modifier.background(SurfaceContainer, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("#INF-492", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            }
                        }
                        Text("Issue ID: $issueId", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Is this issue still valid?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface, modifier = Modifier.padding(bottom = 16.dp))

                // Verification Question 1
                VerificationToggleRow(
                    question = "I can confirm the hazard is present as described.",
                    checked = isPotholeActuallyThere,
                    onCheckedChange = { isPotholeActuallyThere = it }
                )

                // Verification Question 2
                VerificationToggleRow(
                    question = "Are the attached photo & GPS correct?",
                    checked = locationIsAccurate,
                    onCheckedChange = { locationIsAccurate = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Additional Evidence (Optional)", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add any details that might help...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("verification_notes_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryFixed,
                        unfocusedBorderColor = OutlineVariant.copy(alpha = 0.5f),
                        focusedContainerColor = SurfaceContainerLowest,
                        unfocusedContainerColor = SurfaceContainerLowest
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Primary),
                    border = BorderStroke(2.dp, OutlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(24.dp), tint = OutlineColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Upload Photo Evidence", style = MaterialTheme.typography.labelMedium, color = OnSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val statusStr = if (isPotholeActuallyThere && locationIsAccurate) "Yes, this issue exists" else "This issue is fake or incorrect"
                    viewModel.verifyIssue(issueId, statusStr, notes, null) { success ->
                        if (success) {
                            Toast.makeText(context, "Verification logged! +3 Civic points earned.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "You have already verified this issue.", Toast.LENGTH_SHORT).show()
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("submit_verification_vote_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryFixed, contentColor = OnSecondaryFixed)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Verification", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun VerificationToggleRow(
    question: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, if (checked) SecondaryFixed else OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) SecondaryFixed else Color.Transparent)
                .border(1.dp, if (checked) SecondaryFixed else OutlineColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(OnSecondaryFixed))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (checked) Secondary else OnSurfaceVariant, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(question, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface, modifier = Modifier.weight(1f))
    }
}

// --- FeedbackScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    viewModel: CivicViewModel,
    issueId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedStars by remember { mutableIntStateOf(5) }
    var comments by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Share Feedback", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = OnSurface)
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Issue Resolved!", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
            Text("The city reported this issue as complete. How did they do?", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.padding(top = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)

            Spacer(modifier = Modifier.height(32.dp))

            // Rating Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("RATE RESOLUTION QUALITY", style = MaterialTheme.typography.labelMedium, color = OutlineColor)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Star Rating Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val active = (index + 1) <= selectedStars
                        Icon(
                            imageVector = if (active) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "${index + 1} Stars",
                            tint = if (active) SuccessGreen else OutlineVariant,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { selectedStars = index + 1 }
                                .padding(4.dp)
                                .testTag("star_${index + 1}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Tell us about the resolution...", style = MaterialTheme.typography.labelMedium, color = OnSurface, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        placeholder = { Text("Provide any details about the fix...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("feedback_comments_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryFixed,
                            unfocusedBorderColor = OutlineVariant.copy(alpha = 0.5f),
                            focusedContainerColor = SurfaceContainerLowest,
                            unfocusedContainerColor = SurfaceContainerLowest
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = OutlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).border(2.dp, SuccessGreen, CircleShape), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(SuccessGreen))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Mark as properly resolved", style = MaterialTheme.typography.bodyLarge, color = OnSurface)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).border(2.dp, OutlineVariant, CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Request reopen (still an issue)", style = MaterialTheme.typography.bodyLarge, color = OnSurface)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.submitActiveIssueFeedback(selectedStars, comments, selectedStars <= 2)
                    Toast.makeText(context, "Resolution Rating Logged! Thank you.", Toast.LENGTH_LONG).show()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("submit_feedback_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryFixed, contentColor = OnSecondaryFixed)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("Submit Feedback", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(painter = androidx.compose.ui.res.painterResource(id = R.drawable.send_2_svgrepo_com), contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
