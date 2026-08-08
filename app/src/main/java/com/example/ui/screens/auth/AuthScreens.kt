package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CivicViewModel

@Composable
fun LoginIllustration(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.login_illustration),
        contentDescription = "Login Illustration",
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentScale = androidx.compose.ui.layout.ContentScale.Fit
    )
}

@Composable
fun SignUpIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFEBEFEA)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f

            drawCircle(
                color = Color(0xFFC0C7C2),
                radius = 54.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
            drawCircle(
                color = Color(0xFFC0C7C2),
                radius = 42.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            )

            drawLine(
                color = Color(0x22476800),
                start = androidx.compose.ui.geometry.Offset(centerX, centerY - 65.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(centerX, centerY + 65.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0x22476800),
                start = androidx.compose.ui.geometry.Offset(centerX - 65.dp.toPx(), centerY),
                end = androidx.compose.ui.geometry.Offset(centerX + 65.dp.toPx(), centerY),
                strokeWidth = 1.dp.toPx()
            )

            val headRadius = 7.dp.toPx()
            val centerPersonY = centerY + 8.dp.toPx()
            
            drawCircle(
                color = Color(0xFF1E2D24),
                radius = headRadius,
                center = androidx.compose.ui.geometry.Offset(centerX, centerPersonY - 14.dp.toPx())
            )
            val centralBodyPath = Path().apply {
                moveTo(centerX - 16.dp.toPx(), centerPersonY + 12.dp.toPx())
                cubicTo(
                    centerX - 12.dp.toPx(), centerPersonY - 4.dp.toPx(),
                    centerX + 12.dp.toPx(), centerPersonY - 4.dp.toPx(),
                    centerX + 16.dp.toPx(), centerPersonY + 12.dp.toPx()
                )
                close()
            }
            drawPath(path = centralBodyPath, color = Color(0xFF1E2D24))
            
            val leftPersonX = centerX - 24.dp.toPx()
            val leftPersonY = centerY + 12.dp.toPx()
            drawCircle(
                color = Color(0xFF384A40),
                radius = 5.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(leftPersonX, leftPersonY - 10.dp.toPx())
            )
            val leftBodyPath = Path().apply {
                moveTo(leftPersonX, leftPersonY - 3.dp.toPx())
                lineTo(leftPersonX + 10.dp.toPx(), leftPersonY + 4.dp.toPx())
                lineTo(leftPersonX, leftPersonY + 11.dp.toPx())
                lineTo(leftPersonX - 10.dp.toPx(), leftPersonY + 4.dp.toPx())
                close()
            }
            drawPath(path = leftBodyPath, color = Color(0xFF384A40))

            val rightPersonX = centerX + 24.dp.toPx()
            val rightPersonY = centerY + 12.dp.toPx()
            drawCircle(
                color = Color(0xFF384A40),
                radius = 5.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(rightPersonX, rightPersonY - 10.dp.toPx())
            )
            val rightBodyPath = Path().apply {
                moveTo(rightPersonX, rightPersonY - 3.dp.toPx())
                lineTo(rightPersonX + 10.dp.toPx(), rightPersonY + 10.dp.toPx())
                lineTo(rightPersonX - 10.dp.toPx(), rightPersonY + 10.dp.toPx())
                close()
            }
            drawPath(path = rightBodyPath, color = Color(0xFF384A40))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: CivicViewModel,
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var emailOrPhone by remember { mutableStateOf("citizen@jansetu.ai") }
    var password by remember { mutableStateOf("password123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        loginState?.onSuccess {
            onLoginSuccess()
        }?.onFailure { error ->
            Toast.makeText(context, error.message ?: "Login Failed", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4F2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "JanSetu AI",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Welcome back. Let's improve our city together.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = Color(0xFFE2F1E0),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFB7DFB3)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        emailOrPhone = "citizen@jansetu.ai"
                        password = "password123"
                        Toast.makeText(context, "Demo credentials pre-filled", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Demo Account",
                        tint = Color(0xFF2E6930),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Demo Account Credentials",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1B4D1E)
                        )
                        Text(
                            text = "Email: citizen@jansetu.ai • Pass: password123",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E6930)
                        )
                    }
                    Text(
                        text = "Fill",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1B4D1E),
                        modifier = Modifier
                            .background(Color(0xFFC4E8C1), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                placeholder = { Text("Email", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = Color(0xFF6B7280)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_username_input"),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryFixed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFF6B7280)) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                            tint = Color(0xFF6B7280)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_password_input"),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryFixed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot Password?",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Mock reset link sent!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (emailOrPhone.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.loginUser(emailOrPhone, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryFixed)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4B5563)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign up",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier
                        .clickable { onNavigateToSignUp() }
                        .testTag("navigate_to_signup_button")
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: CivicViewModel,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Dhanbad") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    val signUpState by viewModel.signUpState.collectAsState()

    LaunchedEffect(signUpState) {
        signUpState?.onSuccess {
            Toast.makeText(context, "Account created! Please verify your email before logging in.", Toast.LENGTH_LONG).show()
            onSignUpSuccess()
        }?.onFailure { error ->
            Toast.makeText(context, error.message ?: "Sign Up Failed", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "JanSetu AI",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Join the Community",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Create your account to start reporting and resolving civic issues together.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Full Name", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = Color(0xFF6B7280)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("signup_name_input"),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryFixed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email Address", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = Color(0xFF6B7280)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("signup_email_input"),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryFixed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = { Text("Phone Number", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = Color(0xFF6B7280)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("signup_phone_input"),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryFixed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFF6B7280)) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                            tint = Color(0xFF6B7280)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("signup_password_input"),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryFixed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                placeholder = { Text("City / Ward Location", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color(0xFF6B7280)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("signup_city_input"),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryFixed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$".toRegex()
                    if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || city.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    } else if (!email.matches(emailRegex)) {
                        Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                    } else if (password.length < 6) {
                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.signUpUser(name, email, phone, password, city, "Hirapur")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("signup_submit_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryFixed)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4B5563)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Log in",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .testTag("navigate_to_login_button")
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
