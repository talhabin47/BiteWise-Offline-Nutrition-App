package com.example.bitewise.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// A simple enum to define the two states of our app
enum class NetworkStatus {
    Available, Unavailable
}

class NetworkConnectivityObserver(context: Context) {

    // Tap into the Android system's core connectivity service
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // This 'Flow' acts as a live stream of data to your UI
    fun observe(): Flow<NetworkStatus> = callbackFlow {

        // This callback listens to the system for changes
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(NetworkStatus.Available)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                trySend(NetworkStatus.Unavailable)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(NetworkStatus.Unavailable)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                trySend(NetworkStatus.Unavailable)
            }
        }

        // Check the internet status the very second the app opens
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        val isInitiallyConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        trySend(if (isInitiallyConnected) NetworkStatus.Available else NetworkStatus.Unavailable)

        // Register the radar request
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        // When the observer is destroyed (app closes), safely shut down the radar to save battery
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}