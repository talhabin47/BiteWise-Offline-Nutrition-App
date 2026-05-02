package com.example.bitewise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.bitewise.navigation.AppNavigation
import com.example.bitewise.ui.theme.BiteWiseTheme

// NEW: Sync and Database Imports
import com.example.bitewise.data.BiteWiseDatabase
import com.example.bitewise.data.SyncRepository
import com.example.bitewise.network.NetworkConnectivityObserver
import com.example.bitewise.network.NetworkStatus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiteWiseTheme {

                // --- 1. GLOBAL SYNC ENGINE SETUP ---
                val context = LocalContext.current

                // Watch the network status
                val networkObserver = remember { NetworkConnectivityObserver(context) }
                val networkStatus by networkObserver.observe().collectAsState(initial = NetworkStatus.Unavailable)
                val isOnline = networkStatus == NetworkStatus.Available

                // Initialize Databases and Repository
                val userDao = remember { BiteWiseDatabase.getDatabase(context).userDao() }
                val foodDao = remember { BiteWiseDatabase.getDatabase(context).foodLogDao() }
                val syncRepository = remember { SyncRepository(userDao, foodDao) }

                // The Global Background Trigger
                LaunchedEffect(isOnline) {
                    if (isOnline) {
                        syncRepository.syncAllData()
                    }
                }
                // -----------------------------------

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)){
                        AppNavigation()
                    }
                }
            }
        }
    }
}