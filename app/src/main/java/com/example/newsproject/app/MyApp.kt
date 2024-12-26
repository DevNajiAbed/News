package com.example.newsproject.app

import android.app.Application
import com.example.newsproject.util.NetworkConnectivityObserver

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NetworkConnectivityObserver.register(this)
    }
}