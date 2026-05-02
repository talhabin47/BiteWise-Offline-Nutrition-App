package com.example.bitewise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bitewise.ui.theme.BiteWiseGreen

import com.example.bitewise.network.NetworkConnectivityObserver
import com.example.bitewise.network.NetworkStatus
import com.example.bitewise.data.BiteWiseDatabase
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.bitewise.data.SyncRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch

//Helper to draw cards
@Composable
fun StatCard(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

//Helper for the info section
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 16.sp)
        Text(text = value, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
    }
}

//Main Profile Screen............
@Composable
fun ProfileScreen(navController: NavController? = null)
{
    // Core Tools
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val currentUser = auth.currentUser
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val coroutineScope = rememberCoroutineScope()

    // Network Observer Connection
    val networkObserver = remember { NetworkConnectivityObserver(context) }
    val networkStatus by networkObserver.observe().collectAsState(initial = NetworkStatus.Unavailable)
    val isOnline = networkStatus == NetworkStatus.Available

    // Database & Sync Connection
    val dao = remember { BiteWiseDatabase.getDatabase(context).userDao() }
    val foodDao = remember { BiteWiseDatabase.getDatabase(context).foodLogDao() }
    val syncRepository = remember { SyncRepository(dao, foodDao) }

    // The Live Video Stream (Flow) from Room!
    val userProfile by dao.getUser().collectAsState(initial = null)
    val allLogs by (if (isPreview) kotlinx.coroutines.flow.flowOf(emptyList()) else foodDao.getAllLogs()).collectAsState(initial = emptyList())

    // NEW: The Smart Filter!
    // This strips out deleted items AND favorites (negative timestamps) before counting anything.
    val validLogs = remember(allLogs) {
        allLogs.filter { it.timestamp > 0 && !it.isDeleted }
    }

    // Dynamic Streak & Food Count Logic (Now uses validLogs!)
    val totalFoodsLogged = validLogs.size

    val currentStreak = remember(validLogs) {
        if (validLogs.isEmpty()) return@remember 0
        val loggedDates = validLogs.map { log ->
            Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        var streak = 0
        var dateToCheck = today

        if (loggedDates.contains(today)) {
            streak = 1
            dateToCheck = yesterday
        } else if (loggedDates.contains(yesterday)) {
            streak = 1
            dateToCheck = yesterday.minusDays(1)
        } else {
            return@remember 0
        }

        while (loggedDates.contains(dateToCheck)) {
            streak++
            dateToCheck = dateToCheck.minusDays(1)
        }
        streak
    }

    // THE AUTO-SYNC TRIGGER
    LaunchedEffect(isOnline) {
        if (isOnline) {
            syncRepository.syncAllData()
        }
    }

    // THE MASTER LOCKOUT LOGIC
    val isUserSynced = userProfile?.isSynced ?: true
    val areLogsSynced = allLogs.isEmpty() || allLogs.all { it.isSynced }
    val isFullySynced = isUserSynced && areLogsSynced

    val canLogout = isOnline && isFullySynced

    //Variables
    val backgroundColor = Color(0xFFF9F9F9)

    //Profile contents........
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Box(modifier = Modifier.fillMaxWidth())
        {
            //The green box itself
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(BiteWiseGreen)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                //The header (profile text)
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(24.dp))

                //The profile avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                )
                {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier.size(60.dp),
                        tint = BiteWiseGreen
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                //Name and email
                Text(
                    text = userProfile?.name ?: "Loading...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = currentUser?.email ?: "No Email Found",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                //The stat cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                )
                {
                    StatCard(icon = "🔥", value = currentStreak.toString(), label = "Days Active", modifier = Modifier.weight(1f))
                    StatCard(icon = "🍎", value = totalFoodsLogged.toString(), label = "Foods Logged", modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        //The personal information section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        )
        {
            Column(modifier = Modifier.padding(20.dp))
            {
                Text(text = "Personal Information", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(label = "Age", value = "${userProfile?.age ?: "--"} years")
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                InfoRow(label = "Height", value = "${userProfile?.height ?: "--"} cm")
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                InfoRow(label = "Current Weight", value = "${userProfile?.weight ?: "--"} kg")
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                InfoRow(label = "Goal", value = userProfile?.goal ?: "--")

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile Button
                OutlinedButton(
                    onClick = { navController?.navigate("edit_profile") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Edit Profile", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Edit Profile", fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //App info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F1)), // Very light green
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Network Status", color = Color.Gray)
                    Text(
                        text = if (isOnline) "Online" else "Offline",
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) BiteWiseGreen else Color.Red
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Cloud Sync", color = Color.Gray)
                    Text(
                        text = if (isFullySynced) "Up to date ✓" else "Pending...",
                        fontWeight = FontWeight.Bold,
                        color = if (isFullySynced) BiteWiseGreen else Color(0xFFF57C00) // Orange warning
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // THE SMART LOGOUT BUTTON
        Button(
            onClick = {
                // Wrap this in a coroutine to clear the database before logging out!
                coroutineScope.launch {
                    // 1. Wipe the local database clean! No more ghost users!
                    dao.clearAllUsers()
                    foodDao.clearAllLogs()

                    // 2. Sign out of Firebase
                    auth.signOut()

                    // 3. Navigate back to log in
                    navController?.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp),
            enabled = canLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935), // Red
                disabledContainerColor = Color.LightGray // Gray when locked
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    !isOnline -> "Waiting for connection..."
                    !isFullySynced -> "Syncing data..."
                    else -> "Logout"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        //Copyright Bitwise
        Text(
            text = "© 2026 BiteWise. All rights reserved.",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Spacer to clear the bottom navigation bar
        Spacer(modifier = Modifier.height(100.dp))

    }
}

@Preview (showBackground = true, heightDp = 1200)
@Composable
fun ProfileScreenPreview(){
    ProfileScreen()
}