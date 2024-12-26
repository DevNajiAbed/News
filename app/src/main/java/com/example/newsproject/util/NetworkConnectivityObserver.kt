package com.example.newsproject.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NetworkConnectivityObserver {
    sealed class Status(val value: String = "Unknown") {
        object Available : Status("Available")
        object Unavailable : Status("Unavailable")
        object Losing : Status("Losing")
        object Lost : Status("Lost")
    }

    private lateinit var connectivityManager: ConnectivityManager

    private val _connectivityObserver = MutableLiveData<Status>()
    val connectivityObserver: LiveData<Status> = _connectivityObserver

    fun register(context: Context) {
        if(!::connectivityManager.isInitialized)
            connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val  callback = object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        _connectivityObserver.value = Status.Available
                    }
                }
            }
            override fun onUnavailable() {
                super.onUnavailable()
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        _connectivityObserver.value = Status.Unavailable
                    }
                }
            }
            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        _connectivityObserver.value = Status.Losing
                    }
                }
            }
            override fun onLost(network: Network) {
                super.onLost(network)
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        _connectivityObserver.value = Status.Lost
                    }
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
    }
}