package com.example.ui.screens.report

import com.example.R
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.AiAnalysisState
import com.example.data.local.entity.IssueEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.CivicViewModel
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView

// --- ReportIssueScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(
    viewModel: CivicViewModel,
    onNavigateToAiAnalysis: (String, String, Double, Double, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefilledTitle by viewModel.prefilledTitle.collectAsState()
    val prefilledDesc by viewModel.prefilledDescription.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Infra") }

    LaunchedEffect(prefilledTitle, prefilledDesc) {
        if (prefilledTitle.isNotEmpty()) {
            title = prefilledTitle
        }
        if (prefilledDesc.isNotEmpty()) {
            description = prefilledDesc
        }
        viewModel.clearPrefilledReport()
    }
    
    // Media attachment Uri holder
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri
        }
    }
    
    // GPS Latitude/Longitude coordinates state
    val reportLocation by viewModel.reportLocation.collectAsState()

    // Trigger loading GPS coordinates from FusedLocationProvider inside location state
    LaunchedEffect(Unit) {
        viewModel.detectLocation()
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Report Issue", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    // 3 dots removed as requested
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP 1 OF 3 Progress indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STEP 1 OF 3",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Clean 1/3 filled progress bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(SecondaryFixed)
                    )
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                            .background(Color(0xFFE5E7EB))
                    )
                }
            }

            // Card 1: Issue Title
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ISSUE TITLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                text = "e.g., Deep pothole on Main St.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.LightGray
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_title_input")
                    )
                }
            }

            // Card 2: Category Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val categoriesList = listOf(
                            Triple("Infra", R.drawable.shop_svgrepo_com, "Infra"),
                            Triple("Waste", R.drawable.dumper_truck_svgrepo_com, "Waste"),
                            Triple("Safety", R.drawable.danger_zone_svgrepo_com, "Safety"),
                            Triple("Other", Icons.Default.MoreHoriz, "Other")
                        )
                        
                        categoriesList.forEach { (name, icon, label) ->
                            val isSelected = selectedCategory == name
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategory = name }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) SecondaryFixed else Color(0xFFF3F4F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (icon is androidx.compose.ui.graphics.vector.ImageVector) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = name,
                                            tint = Color.Black,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else if (icon is Int) {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = icon),
                                            contentDescription = name,
                                            tint = Color.Black,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DESCRIPTION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "Optional",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color.Gray
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = {
                            Text(
                                text = "Provide any additional details that might help identify or resolve the issue...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.LightGray
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Black
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("report_description_input")
                    )
                }
            }

            // Card 4: Visual Evidence Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "VISUAL EVIDENCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                photoPicker.launch("image/*")
                            }
                            .testTag("snap_evidence_button")
                    ) {
                        if (attachedImageUri != null) {
                            AsyncImage(
                                model = attachedImageUri,
                                contentDescription = "Attached Evidence",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .clickable { attachedImageUri = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            // Dash Border Drawing
                            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRoundRect(
                                    color = Color(0xFFD1D5DB),
                                    style = stroke,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                                )
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Camera",
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tap to capture or upload",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "AI will analyze tags and severity automatically",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Card 5: Location Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "LOCATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
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
                                controller.setZoom(15.0)
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
                            modifier = Modifier.fillMaxSize(),
                            factory = { mapView },
                            update = { map ->
                                map.overlays.clear()
                                val lat = reportLocation?.latitude ?: 23.8122
                                val lon = reportLocation?.longitude ?: 86.4425
                                val point = GeoPoint(lat, lon)
                                map.controller.setCenter(point)
                                
                                val marker = Marker(map)
                                marker.position = point
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                
                                val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.location_marker_svgrepo_com)
                                if (drawable != null) {
                                    val density = context.resources.displayMetrics.density
                                    val size = (36 * density).toInt()
                                    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                                    val canvas = android.graphics.Canvas(bitmap)
                                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                                    drawable.draw(canvas)
                                    marker.icon = android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
                                }
                                map.overlays.add(marker)
                                map.invalidate()
                            }
                        )
                        
                        // Detecting location pill
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Target",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (reportLocation == null) "Detecting location..." else "${reportLocation?.address ?: "Location detected"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                        
                        // Pencil edit button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    Toast.makeText(context, "Manually setting coordinates...", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Location",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Analysis Button (Neon pill)
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please provide an issue title", Toast.LENGTH_SHORT).show()
                    } else {
                        val lat = reportLocation?.latitude ?: 23.804
                        val lon = reportLocation?.longitude ?: 86.431
                        val addr = reportLocation?.address ?: "Hirapur, Dhanbad"
                        onNavigateToAiAnalysis(title, description, lat, lon, addr)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_report_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryFixed, contentColor = OnSecondaryFixed)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome, 
                        contentDescription = null, 
                        modifier = Modifier.size(24.dp),
                        tint = OnSecondaryFixed
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Analyze with AI", 
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = OnSecondaryFixed
                        )
                    )
                }
            }
            
            Text(
                text = "By continuing, you agree to the JanSetu Terms.",
                style = MaterialTheme.typography.labelSmall,
                color = OutlineColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

// --- AIAnalysisScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalysisScreen(
    viewModel: CivicViewModel,
    title: String,
    description: String,
    lat: Double,
    lon: Double,
    address: String,
    onNavigateToDuplicate: (String) -> Unit,
    onNavigateToSuccess: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val aiState by viewModel.aiState.collectAsState()
    val duplicateResult by viewModel.duplicateResult.collectAsState()
    
    // Timeline animation states
    var activeStep by remember { mutableIntStateOf(1) }
    var hasSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetAiState()
        viewModel.analyzeIssueWithAi(title, description, lat, lon)
    }

    // Step animation sequencer
    LaunchedEffect(aiState, duplicateResult) {
        when (val state = aiState) {
            is AiAnalysisState.Idle -> {
                activeStep = 1
            }
            is AiAnalysisState.AnalyzingImage -> {
                activeStep = 1
            }
            is AiAnalysisState.ReadingDescription -> {
                activeStep = 1
            }
            is AiAnalysisState.Categorizing -> {
                activeStep = 2
            }
            is AiAnalysisState.CheckingDuplicates -> {
                activeStep = 3
            }
            is AiAnalysisState.CalculatingSeverity -> {
                activeStep = 3
            }
            is AiAnalysisState.GeneratingTrustScore -> {
                activeStep = 3
            }
            is AiAnalysisState.Success -> {
                activeStep = 4
                if (!hasSubmitted) {
                    hasSubmitted = true
                    delay(800) // let user see step 4 complete
                    val dup = duplicateResult
                    if (dup != null && dup.duplicateFound && dup.duplicateIssueId != null) {
                        onNavigateToDuplicate(dup.duplicateIssueId)
                    } else {
                        viewModel.submitReportedIssue(
                            title = title,
                            description = description,
                            category = state.result.category,
                            lat = lat,
                            lon = lon,
                            address = address,
                            mediaUri = "https://images.unsplash.com/photo-1515162305285-0293e4767cc2?auto=format&fit=crop&w=600&q=80"
                        ) { issueId ->
                            onNavigateToSuccess(issueId, state.result.category, state.result.severity)
                        }
                    }
                }
            }
            is AiAnalysisState.Error -> {
                Toast.makeText(context, "AI Analysis Error: ${state.error}", Toast.LENGTH_LONG).show()
                onBack()
            }
        }
    }

    Scaffold(
        containerColor = PrimaryContainer, // dark green background
        topBar = {
            TopAppBar(
                title = { Text("AI Analysis", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Surface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Surface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryContainer)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Glowing Background Effect
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center)
                    .background(SecondaryFixed.copy(alpha = 0.05f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Central Visualizer
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, SecondaryFixed.copy(alpha = 0.3f * pulseScale), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .border(2.dp, SecondaryFixed.copy(alpha = 0.5f * pulseScale), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(PrimaryContainer, CircleShape)
                            .border(2.dp, SecondaryFixed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "AI Processing",
                            tint = SecondaryFixed,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("AI Analysis", style = MaterialTheme.typography.headlineLarge, color = Surface)
                Text(
                    if (activeStep == 4) "ANALYSIS COMPLETE" else "IN PROGRESS",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = OutlineColor,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Progress Steps
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimelineProgressRow(text = "Analyzing image...", stepActive = activeStep == 1, stepDone = activeStep > 1)
                    TimelineProgressRow(text = "Detecting issue category...", stepActive = activeStep == 2, stepDone = activeStep > 2)
                    TimelineProgressRow(text = "Checking duplicates...", stepActive = activeStep == 3, stepDone = activeStep > 3)
                    TimelineProgressRow(text = "Calculating severity...", stepActive = activeStep == 3, stepDone = activeStep > 3)
                }
            }
        }
    }
}

@Composable
fun TimelineProgressRow(
    text: String,
    stepActive: Boolean,
    stepDone: Boolean
) {
    val color = if (stepDone) SuccessGreen else if (stepActive) SecondaryFixed else OutlineVariant.copy(alpha = 0.5f)
    val icon = if (stepDone) Icons.Default.CheckCircle else if (stepActive) Icons.Default.Sync else Icons.Default.RadioButtonUnchecked
    val textColor = if (stepDone) Surface.copy(alpha = 0.7f) else if (stepActive) Surface else OutlineVariant.copy(alpha = 0.5f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (stepActive) FontWeight.Bold else FontWeight.Normal),
            color = textColor
        )
    }
}

// --- DuplicateSuggestionScreen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateSuggestionScreen(
    viewModel: CivicViewModel,
    duplicateId: String,
    title: String,
    description: String,
    lat: Double,
    lon: Double,
    address: String,
    onNavigateToHome: () -> Unit,
    onNavigateToSuccess: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val duplicateIssue = remember { mutableStateOf<IssueEntity?>(null) }

    LaunchedEffect(duplicateId) {
        viewModel.getIssueById(duplicateId) { issue ->
            duplicateIssue.value = issue
        }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("JanSetu AI", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Back", tint = OutlineColor)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(WarningYellow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulating the ping effect with a slightly larger border box
                    Box(modifier = Modifier.fillMaxSize().border(2.dp, WarningYellow.copy(alpha = 0.5f), CircleShape))
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Possible duplicate found", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface, textAlign = TextAlign.Center)
                Text(
                    "Our AI matched your report with an existing issue in this area. You can verify it or submit a new one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // AI Confidence Indicator
                Row(
                    modifier = Modifier
                        .background(SecondaryFixed.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .border(1.dp, SecondaryFixed.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = SecondaryFixed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Match Confidence: 82%", style = MaterialTheme.typography.labelMedium, color = SecondaryFixed)
                }

                Spacer(modifier = Modifier.height(32.dp))

                duplicateIssue.value?.let { issue ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("existing_duplicate_issue_card"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column {
                            // Top image placeholder with tags
                            Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(SurfaceVariant)) {
                                Row(modifier = Modifier.padding(16.dp).align(Alignment.TopStart), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(WarningYellow)
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(issue.status.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(SurfaceCard.copy(alpha = 0.9f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnSurface)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("120m away", style = MaterialTheme.typography.labelSmall, color = OnSurface)
                                        }
                                    }
                                }
                            }
                            
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(issue.title, style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                                Text(issue.description, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 12.dp))
                                
                                Divider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineVariant.copy(alpha = 0.3f))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = OutlineColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reported 2 days ago by another citizen", style = MaterialTheme.typography.labelSmall, color = OutlineColor)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        viewModel.verifyIssue(duplicateId, "Yes, this issue exists", "Upvoted as duplicate from submission flow", null) { _ -> }
                        Toast.makeText(viewModel.getApplication(), "Verification logged! +3 Civic points earned.", Toast.LENGTH_LONG).show()
                        onNavigateToHome()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("upvote_existing_button"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryFixed, contentColor = OnSecondaryFixed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify existing issue", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                OutlinedButton(
                    onClick = {
                        // Create it anyway
                        viewModel.submitReportedIssue(
                            title = title,
                            description = description,
                            category = "Pothole",
                            lat = lat,
                            lon = lon,
                            address = address,
                            mediaUri = "https://images.unsplash.com/photo-1515162305285-0293e4767cc2?auto=format&fit=crop&w=600&q=80"
                        ) { newId ->
                            onNavigateToSuccess(newId, "Pothole", "High")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("force_submit_duplicate_button"),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, OutlineColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit as new issue anyway", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

// --- SuccessScreen ---

@Composable
fun SuccessScreen(
    issueId: String,
    category: String,
    severity: String,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            // Celebratory Icon
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard)
                    .border(4.dp, SecondaryFixed, CircleShape)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Simulating glow-success
                Box(modifier = Modifier.fillMaxSize().background(SecondaryFixed.copy(alpha = 0.2f), CircleShape))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(64.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Text("Issue Reported Successfully", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = OnSurface, textAlign = TextAlign.Center)
            Text(
                text = "Thank you for contributing to your community. Our AI is now processing your report.",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), // Simulating top border with generic border
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                // Simulating top-border specifically
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(SecondaryFixed))
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Report Summary", style = MaterialTheme.typography.headlineSmall, color = OnSurface, modifier = Modifier.padding(bottom = 16.dp))

                        // Detail Row 1 - Issue ID
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Issue ID", style = MaterialTheme.typography.bodyMedium, color = OutlineColor)
                            Box(modifier = Modifier.background(SurfaceContainer, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text("#${issueId.take(8).uppercase()}", style = MaterialTheme.typography.labelMedium, color = OnSurface)
                            }
                        }
                        Divider(color = OutlineVariant.copy(alpha = 0.3f))

                        // Detail Row 2 - Category
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Category", style = MaterialTheme.typography.bodyMedium, color = OutlineColor)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = OutlineColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = OnSurface)
                            }
                        }
                        Divider(color = OutlineVariant.copy(alpha = 0.3f))

                        // Detail Row 3 - Severity
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Severity", style = MaterialTheme.typography.bodyMedium, color = OutlineColor)
                            Box(modifier = Modifier.background(SeverityHigh, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text(severity, style = MaterialTheme.typography.labelMedium, color = OnError)
                            }
                        }
                        Divider(color = OutlineVariant.copy(alpha = 0.3f))

                        // Detail Row 4 - Status
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Status", style = MaterialTheme.typography.bodyMedium, color = OutlineColor)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).background(SecondaryFixed, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("REPORTED", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { onNavigateToDetails(issueId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("view_details_success_button"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryFixed, contentColor = OnSecondaryFixed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("View Issue Details", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("back_to_home_button"),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(2.dp, OutlineColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface)
                ) {
                    Text("Go Home", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                }
            }
        }
    }
}
