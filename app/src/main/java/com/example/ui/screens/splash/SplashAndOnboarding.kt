package com.example.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onNavigateToOnboarding: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 3000, easing = LinearOutSlowInEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        progress = 1f
        delay(1000) // Beautiful splash hold with active loader
        onNavigateToOnboarding()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Pure black background matching the logo
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.group_93), // Logo from drawable
                contentDescription = "Jan Setu AI Logo",
                modifier = Modifier
                    .size(240.dp)
                    .padding(bottom = 32.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "JAN SETU AI",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .width(180.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp)),
                color = SecondaryFixed,
                trackColor = Color(0xFF1E201E)
            )
        }
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val illustration: @Composable () -> Unit
)

@Composable
fun OnboardingIllustration1() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAEFEB)) // soft pale green-gray
    ) {
        // Draw a stylized cracked road/pothole at the bottom
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
        ) {
            val width = size.width
            val height = size.height
            
            // Draw a soft grey shadow for the pothole
            drawOval(
                color = Color(0x1F000000),
                topLeft = androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.4f),
                size = androidx.compose.ui.geometry.Size(width * 0.6f, height * 0.4f)
            )
            
            // Draw the cracked pothole path
            val path = Path().apply {
                moveTo(width * 0.35f, height * 0.5f)
                lineTo(width * 0.45f, height * 0.42f)
                lineTo(width * 0.5f, height * 0.48f)
                lineTo(width * 0.6f, height * 0.4f)
                lineTo(width * 0.65f, height * 0.52f)
                lineTo(width * 0.58f, height * 0.65f)
                lineTo(width * 0.48f, height * 0.6f)
                lineTo(width * 0.38f, height * 0.63f)
                close()
            }
            drawPath(path = path, color = Color(0xFFC5C9C6))
            
            // Draw internal crack details
            val crackPath = Path().apply {
                moveTo(width * 0.4f, height * 0.5f)
                lineTo(width * 0.48f, height * 0.53f)
                lineTo(width * 0.52f, height * 0.48f)
                lineTo(width * 0.58f, height * 0.55f)
            }
            drawPath(path = crackPath, color = Color(0xFF8E9290), style = Stroke(width = 3f))
        }

        // Draw a stylized 3D character (back/side view) on the left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Head
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF42594E)) // dark teal hair/head
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Shoulders / Torso
                Box(
                    modifier = Modifier
                        .size(width = 72.dp, height = 120.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color(0xFF8FA89A)) // soft sage jacket
                )
            }
        }

        // Floating smartphone in the center-right
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 20.dp, y = (-10).dp)
                .size(width = 90.dp, height = 160.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFF1E201E)) // dark smartphone body
                .border(2.dp, Color(0xFF8E9290), RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            // Screen area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF323B36)) // camera viewfinder bg
            ) {
                // Camera grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Vertical grid lines
                    drawLine(Color(0x33FFFFFF), androidx.compose.ui.geometry.Offset(w / 3f, 0f), androidx.compose.ui.geometry.Offset(w / 3f, h), strokeWidth = 1f)
                    drawLine(Color(0x33FFFFFF), androidx.compose.ui.geometry.Offset(w * 2f / 3f, 0f), androidx.compose.ui.geometry.Offset(w * 2f / 3f, h), strokeWidth = 1f)
                    // Horizontal grid lines
                    drawLine(Color(0x33FFFFFF), androidx.compose.ui.geometry.Offset(0f, h / 3f), androidx.compose.ui.geometry.Offset(w, h / 3f), strokeWidth = 1f)
                    drawLine(Color(0x33FFFFFF), androidx.compose.ui.geometry.Offset(0f, h * 2f / 3f), androidx.compose.ui.geometry.Offset(w, h * 2f / 3f), strokeWidth = 1f)
                }

                // Minimized cracked road in screen
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color(0x55000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = SecondaryFixed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Glowing overlays around the phone (Pins & Labels)
        // Label 1: Coordinate pin 1 (top right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCCFFFFFF))
                .border(1.dp, SecondaryFixed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryFixed, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("40.7128° N", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = OnSurface)
                }
                Text("74.0060° W", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = OnSurfaceVariant)
            }
        }

        // Label 2: Coordinate pin 2 (mid left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 10.dp, y = (-40).dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCCFFFFFF))
                .border(1.dp, SecondaryFixed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryFixed, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("GPS Verified", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = OnSurface)
            }
        }

        // Label 3: Road issue detected overlay (floating bottom right of phone)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
                .clip(RoundedCornerShape(50))
                .background(SecondaryFixed) // neon green background
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                "ROAD ISSUE DETECTED",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                color = OnSecondaryFixed
            )
        }
    }
}

@Composable
fun OnboardingIllustration2() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDF1F5)) // light cool grey-blue
    ) {
        // High-tech holographic background circles using Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.45f)
            
            // Tech Ring 1 (Dashed Outer)
            drawCircle(
                color = SecondaryFixed.copy(alpha = 0.4f),
                radius = 110.dp.toPx(),
                center = center,
                style = Stroke(
                    width = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            )
            
            // Tech Ring 2 (Dashed Inner)
            drawCircle(
                color = Color(0xFF2563EB).copy(alpha = 0.3f), // PrimaryBlue accent
                radius = 80.dp.toPx(),
                center = center,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f)
                )
            )

            // Tech Ring 3 (Solid thin)
            drawCircle(
                color = SecondaryFixed.copy(alpha = 0.2f),
                radius = 50.dp.toPx(),
                center = center,
                style = Stroke(width = 1f)
            )
        }

        // Brain Display Pod in Center
        Column(
            modifier = Modifier.align(Alignment.Center).offset(y = (-10).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rotating Holographic Pod
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                SecondaryFixed.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // High-fidelity Brain icon
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Brain",
                    tint = OnSecondaryFixedVariant, // rich dark green-black
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        // Floating Badges directly replicating HUD metadata cards
        // HUD Card 1 (Left): Category: Infrastructure
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 12.dp, y = (-50).dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(8.dp)
        ) {
            Column {
                Text("Category:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = OnSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SecondaryFixed.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Infrastructure", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = OnSecondaryFixedVariant)
                }
            }
        }

        // HUD Card 2 (Right): Severity: High
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-12).dp, y = (-20).dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(8.dp)
        ) {
            Column {
                Text("Severity:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = OnSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFF7ED)) // warm cream
                        .border(1.dp, SeverityHigh.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("High", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = SeverityHigh)
                }
            }
        }

        // HUD Card 3 (Bottom): 98% Confidence Dial
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .shadow(6.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circle Progress Indicator
                Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color(0xFFE2E8F0), style = Stroke(width = 2.dp.toPx()))
                        drawArc(
                            color = SecondaryFixed,
                            startAngle = -90f,
                            sweepAngle = 310f,
                            useCenter = false,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "98% Confidence Match",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF131F00)
                )
            }
        }
    }
}

@Composable
fun OnboardingIllustration3() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F6F1)) // light organic pale green
    ) {
        // Draw a simulated 3D landscape skyline at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(60.dp, 80.dp, 50.dp, 75.dp, 40.dp, 65.dp).forEach { height ->
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(height)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(Color(0xFFD4DDD7))
                )
            }
        }

        // Draw a perspective tech grid
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height
            // Grid horizon line
            drawLine(Color(0x22476800), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(w, 0f), strokeWidth = 2f)
            // Vanishing perspective lines
            for (i in 0..10) {
                val fraction = i / 10f
                drawLine(
                    Color(0x22476800),
                    androidx.compose.ui.geometry.Offset(w / 2f, 0f),
                    androidx.compose.ui.geometry.Offset(w * fraction, h),
                    strokeWidth = 1f
                )
            }
        }

        // Central floating transparent map panel
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-20).dp)
                .size(width = 180.dp, height = 180.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.85f))
                .border(1.dp, Color.White, RoundedCornerShape(24.dp))
                .padding(12.dp)
        ) {
            // Draw a map trace line in the background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val mapPath = Path().apply {
                    moveTo(w * 0.1f, h * 0.8f)
                    quadraticTo(w * 0.3f, h * 0.7f, w * 0.4f, h * 0.4f)
                    quadraticTo(w * 0.5f, h * 0.1f, w * 0.8f, h * 0.2f)
                }
                drawPath(path = mapPath, color = Color(0x33476800), style = Stroke(width = 4.dp.toPx()))
                
                // Map Dots
                drawCircle(color = Color(0xFF476800), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.8f))
                drawCircle(color = SecondaryFixed, radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.4f))
                drawCircle(color = Color(0xFF2563EB), radius = 5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.2f))
            }

            // High-fidelity Floating AR Capsules
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Capsule 1: VERIFIED
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SecondaryFixed)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = OnSecondaryFixed, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("VERIFIED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = OnSecondaryFixed)
                }

                // Capsule 2: ASSIGNED
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ASSIGNED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Color(0xFF2563EB))
                }

                // Capsule 3: RESOLVED
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFAF5FF))
                        .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RESOLVED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Color(0xFF7E22CE))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        OnboardingPageData(
            title = "Report local issues\ninstantly",
            description = "Capture potholes, garbage, leakages, and damaged public infrastructure with photos and location.",
            illustration = { 
                Image(
                    painter = painterResource(id = R.drawable.onboarding_1),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        ),
        OnboardingPageData(
            title = "AI understands the\nproblem",
            description = "JanSetu AI automatically categorizes issues, checks duplicates, and estimates severity in seconds.",
            illustration = { 
                Image(
                    painter = painterResource(id = R.drawable.onboarding_2),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        ),
        OnboardingPageData(
            title = "Track progress and\nimprove your area",
            description = "Verify issues, follow updates, and see how your community works together to solve problems.",
            illustration = { 
                Image(
                    painter = painterResource(id = R.drawable.onboarding_3),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color(0xFFF5F9F3) // Soft pale green-cream background matching the image exactly
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Absolute Header "Skip" Button (matches screenshots)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 24.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                TextButton(
                    onClick = onFinished,
                    colors = ButtonDefaults.textButtonColors(contentColor = OnSurfaceVariant)
                ) {
                    Text("Skip", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Pager Content for Illustration + Text
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp, bottom = 120.dp, start = 24.dp, end = 24.dp)
            ) { index ->
                val page = pages[index]
                Column(
                    horizontalAlignment = Alignment.Start, // Left-aligned exactly like screenshots!
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Modern Illustration Card with rounded corners
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f)
                            .clip(RoundedCornerShape(32.dp))
                    ) {
                        page.illustration()
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 38.sp
                        ),
                        color = OnSurface,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    )
                }
            }

            // Page Indicator Dots (bottom left, horizontally aligned with Next button)
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 66.dp, start = 24.dp)
            ) {
                repeat(pages.size) { i ->
                    val active = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (active) 28.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (active) SecondaryFixed else Color(0xFFC2CBD1))
                    )
                }
            }

            // Next/CTA Circular Button (bottom right, floating neon-green with black arrow)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 40.dp, end = 24.dp)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(SecondaryFixed) // neon green `#aff800`
                    .clickable {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinished()
                        }
                    }
                    .testTag("onboarding_cta_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Continue",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
