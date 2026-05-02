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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bitewise.ui.theme.BiteWiseGreen

// Firebase & Database Imports
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.bitewise.data.BiteWiseDatabase
import com.example.bitewise.data.SyncRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController? = null) {
    //Focus Manager to control the keyboard
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() // NEW: Needed to run background downloads!

    // Initialize Firebase Auth & Database
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val auth = if (isPreview) null else com.google.firebase.Firebase.auth

    // NEW: Set up the Sync Engine to pull data down
    val userDao = if (isPreview) null else BiteWiseDatabase.getDatabase(context).userDao()
    val foodDao = if (isPreview) null else BiteWiseDatabase.getDatabase(context).foodLogDao()
    val syncRepository = if (isPreview || userDao == null || foodDao == null) null else SyncRepository(userDao, foodDao)

    // ALL THE VARIABLES
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // States for loading and errors
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    // Only enable the login button if these basic formats are met
    val isEmailValid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-z]{2,}\$".toRegex())
    val isPasswordValid = password.length == 12

    // Make sure we don't allow clicking if it's currently loading
    val isFormValid = isEmailValid && isPasswordValid && !isLoading

    // UI contents
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // Hide keyboard when tapping anywhere outside a text field
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(BiteWiseGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🔐", fontSize = 50.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Header text
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Login to continue your journey",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Input fields (email and password)
        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { if (it.length <= 12) password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password"
                    )
                }
            }
        )

        // Forgot Password Link
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = {
                navController?.navigate("forgot_password")
            }) {
                Text(text = "Forgot Password?", color = BiteWiseGreen)
            }
        }

        // Display Firebase Error Message if login fails
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // The login button
        Button(
            onClick = {
                focusManager.clearFocus()
                isLoading = true
                errorMessage = null

                auth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // NEW: Firebase Login worked! Now download everything from the cloud.
                            coroutineScope.launch {
                                // Keep the loading spinner active while we restore
                                syncRepository?.restoreDataFromCloud()

                                isLoading = false // Turn off spinner

                                // Navigate to Home AFTER the data is fully restored
                                navController?.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        } else {
                            // Login failed (wrong password, no internet, etc.)
                            isLoading = false
                            errorMessage = task.exception?.localizedMessage ?: "Login failed."
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isFormValid, //Locking the button if data format is bad
            colors = ButtonDefaults.buttonColors(
                containerColor = BiteWiseGreen,
                disabledContainerColor = Color.LightGray // Turns grey when locked
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            // Show a loading spinner if talking to Firebase or downloading data
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFormValid) Color.White else Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't have an account? ", color = Color.Gray)
            Text(
                text = "Sign Up",
                color = BiteWiseGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    // Navigate back to Register
                    navController?.navigate("register") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}