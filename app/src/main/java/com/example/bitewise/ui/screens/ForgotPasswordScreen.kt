package com.example.bitewise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bitewise.ui.theme.BiteWiseGreen

// Firebase Imports
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController? = null) {
    val focusManager = LocalFocusManager.current

    // Check if we are in the Android Studio Preview
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    // Only load Firebase if we are running on a real app
    val auth = if (isPreview) null else Firebase.auth

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val isEmailValid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-z]{2,}\$".toRegex())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = {
                // THE FIX: Only pop the screen if we are actually still on it!
                // This completely prevents the double-tap white screen bug.
                if (navController?.currentDestination?.route == "forgot_password") {
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(BiteWiseGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✉️", fontSize = 50.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Enter your email to receive a reset link",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // NEW: The 12-Character Warning Notice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp)) // Light Orange
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Warning",
                    tint = Color(0xFFE65100) // Dark Orange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Important: When creating your new password on the Google webpage, please ensure it is exactly 12 characters long. Otherwise, you will not be able to log back into the BiteWise app. We apologize for any inconvenience!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE65100)
                )
            }
        }

        // Status Message (Success or Error)
        if (message != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message!!,
                color = if (isSuccess) BiteWiseGreen else Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                isLoading = true
                message = null

                // FIREBASE MAGIC HAPPENS HERE
                auth?.sendPasswordResetEmail(email)
                    ?.addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            isSuccess = true
                            message = "Reset link sent! Please check your inbox."
                        } else {
                            isSuccess = false
                            message = task.exception?.localizedMessage ?: "Failed to send link."
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isEmailValid && !isLoading && !isSuccess,
            colors = ButtonDefaults.buttonColors(
                containerColor = BiteWiseGreen,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isSuccess) "Email Sent ✓" else "Send Reset Link",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEmailValid && !isSuccess) Color.White else Color.DarkGray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen()
}