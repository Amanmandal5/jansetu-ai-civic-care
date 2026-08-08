package com.example.ui.screens.map

import android.graphics.Paint
import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivicMapScreen(
    viewModel: CivicViewModel,
    onNavigateToIssueDetails: (String) -> Unit
) {
    val context = LocalContext.current
    val allIssues by viewModel.allIssues.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val categories = listOf("All", "Pothole", "Garbage Overflow", "Water Leakage", "Broken Streetlight", "Drainage Blockage")

    // Live tracking of tapped pin
    var selectedIssueOnMap by remember { mutableStateOf<IssueEntity?>(null) }

    // Schematic fallback map layout boundaries
    val filteredIssues = remember(selectedCategoryFilter, searchQuery, allIssues) {
        allIssues.filter { issue ->
            val matchCat = selectedCategoryFilter == "All" || 
                           selectedCategoryFilter == "Severity" || 
                           selectedCategoryFilter == "Status" || 
                           issue.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchSearch = searchQuery.isEmpty() || issue.title.contains(searchQuery, ignoreCase = true) || issue.address.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
        }
    }

    // Auto-preselect first issue to match the screenshot state with the card open initially
    LaunchedEffect(filteredIssues) {
        if (selectedIssueOnMap == null && filteredIssues.isNotEmpty()) {
            selectedIssueOnMap = filteredIssues.firstOrNull()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // 1. Real Interactive OSM Map
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        val preferences = context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)

        LaunchedEffect(Unit) {
            Configuration.getInstance().load(context, preferences)
        }

        val mapView = remember {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                controller.setZoom(13.0)
                // Default to San Francisco
                controller.setCenter(GeoPoint(37.7749, -122.4194))
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                when (event) {
                    androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                mapView.onDetach()
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("osmdroid_map_view"),
            factory = { mapView },
            update = { map ->
                map.overlays.clear()
                
                filteredIssues.forEach { issue ->
                    val marker = Marker(map)
                    marker.position = GeoPoint(issue.latitude, issue.longitude)
                    marker.title = issue.title
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    
                    val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.location_marker_svgrepo_com)
                    if (drawable != null) {
                        val density = context.resources.displayMetrics.density
                        val size = (36 * density).toInt() // 36dp icon size
                        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        marker.icon = android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
                    }
                    
                    marker.setOnMarkerClickListener { m, _ ->
                        selectedIssueOnMap = issue
                        m.showInfoWindow()
                        true
                    }
                    map.overlays.add(marker)
                }
                
                selectedIssueOnMap?.let { issue ->
                    map.controller.animateTo(GeoPoint(issue.latitude, issue.longitude))
                }
                
                map.invalidate()
            }
        )

        // 2. Floating Search & Filtering Overlay (Translucent and polished)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            // Top Header Row with Location indicator, "Jan Setu AI", and notification bell on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.location_svgrepo_com),
                    contentDescription = null,
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jan Setu AI",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color(0xFF111827)
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.remind_svgrepo_com),
                    contentDescription = "Notifications",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Polished Search Card
            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search_svgrepo_com),
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search locations...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black,
                                fontSize = 15.sp
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("map_search_bar")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontally scrolling Category filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // All Issues chip (with Tune icon and Black background)
                item {
                    val active = selectedCategoryFilter == "All"
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (active) Color.Black else Color.White)
                            .border(1.dp, if (active) Color.Black else Color(0xFFE5E7EB), RoundedCornerShape(50))
                            .clickable { selectedCategoryFilter = "All" }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = if (active) Color.White else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "All Issues",
                            color = if (active) Color.White else Color.Black,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Severity chip
                item {
                    val active = selectedCategoryFilter == "Severity"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (active) Color.Black else Color.White)
                            .border(1.dp, if (active) Color.Black else Color(0xFFE5E7EB), RoundedCornerShape(50))
                            .clickable { selectedCategoryFilter = "Severity" }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Severity",
                            color = if (active) Color.White else Color.Black,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Status chip
                item {
                    val active = selectedCategoryFilter == "Status"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (active) Color.Black else Color.White)
                            .border(1.dp, if (active) Color.Black else Color(0xFFE5E7EB), RoundedCornerShape(50))
                            .clickable { selectedCategoryFilter = "Status" }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Status",
                            color = if (active) Color.White else Color.Black,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Other categories dynamically listed
                items(categories.filter { it != "All" }) { cat ->
                    val active = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (active) Color.Black else Color.White)
                            .border(1.dp, if (active) Color.Black else Color(0xFFE5E7EB), RoundedCornerShape(50))
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (active) Color.White else Color.Black,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }

        // 3. Floating GPS & Zoom Controls on the Right
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // GPS Button
            Card(
                onClick = { Toast.makeText(context, "Centering map...", Toast.LENGTH_SHORT).show() },
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }


        }

        // 4. Bottom Preview Drawer Card (Animated entry on pin click)
        AnimatedVisibility(
            visible = selectedIssueOnMap != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            selectedIssueOnMap?.let { issue ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToIssueDetails(issue.id) }
                        .testTag("map_issue_preview_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // First row: CRITICAL tag & AI Verified + Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // CRITICAL Chip
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFDC2626)) // Crimson red
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "CRITICAL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // AI Verified indicator
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "AI Verified",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AI Verified",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6B7280)
                                        )
                                    )
                                }
                            }
                            
                            // Close Button
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4F6))
                                    .clickable { selectedIssueOnMap = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Second row: Bold Title
                        Text(
                            text = issue.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Third row: Image and description text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular cropped image on the left
                            if (issue.mediaUri != null) {
                                AsyncImage(
                                    model = issue.mediaUri,
                                    contentDescription = issue.title,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF3F4F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Description & Schedule info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = issue.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 15.sp,
                                        color = Color(0xFF4B5563),
                                        lineHeight = 20.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Schedule",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reported 2 hours ago",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF9CA3AF)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Fourth row: Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Black pill button for View Details
                            Button(
                                onClick = { onNavigateToIssueDetails(issue.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "View Details",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                            
                            // Share circle button
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                                    .clickable {
                                        Toast.makeText(context, "Sharing issue...", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.share_svgrepo_com),
                                    contentDescription = "Share",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


