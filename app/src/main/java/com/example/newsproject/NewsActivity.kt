package com.example.newsproject

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.newsproject.databinding.ActivityNewsBinding
import com.example.newsproject.util.NetworkConnectivityObserver
import com.example.newsproject.viewmodel.activity.NewsViewModel

class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding
    val viewModel by viewModels<NewsViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NewsViewModel(application) as T
                }
            }
        }
    )
    private var noConnectionDialog: AlertDialog? = null
    private var tooManyRequestsDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpBottomNavigation()
        observeNetworkConnectivity()
        if(!viewModel.hasInternetConnection())
            showNoConnectionDialog()
    }

    private fun observeNetworkConnectivity() {
        NetworkConnectivityObserver.connectivityObserver.observe(this) { status ->
            when(status) {
                NetworkConnectivityObserver.Status.Available -> {
                    noConnectionDialog?.dismiss()
                    viewModel.resumeAllRequests()
                }
                NetworkConnectivityObserver.Status.Losing,
                NetworkConnectivityObserver.Status.Lost,
                NetworkConnectivityObserver.Status.Unavailable -> {
                    viewModel.cancelAllRequests()
                    if(binding.navView.selectedItemId != R.id.navigation_saved_news)
                        showNoConnectionDialog()
                }
            }
        }
    }

    fun showNoConnectionDialog() {
        noConnectionDialog?.dismiss()

        noConnectionDialog = AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_no_wifi)
            .setTitle("No internet connection")
            .setMessage("You've lost your internet connection, you can see your saved news. Click Go to see them, Exit to exit the app.")
            .setPositiveButton("Go") { _, _ ->
                binding.navView.selectedItemId = R.id.navigation_saved_news
            }.setNegativeButton("Exit") {_, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    fun showTooManyRequestsDialog() {
        if(noConnectionDialog != null && noConnectionDialog?.isShowing!!)
            return

        tooManyRequestsDialog?.dismiss()

        tooManyRequestsDialog = AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_hour_glass)
            .setTitle("Too many requests")
            .setMessage("You have made too many requests for news recently. You are limited to 100 requests over a 24 hour period (50 requests available every 12 hours).\nYou still can see your saved list of news, click Go to see them, Exit to exit the app.")
            .setPositiveButton("Go") { _, _ ->
                binding.navView.selectedItemId = R.id.navigation_saved_news
            }.setNegativeButton("Exit") {_, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun setUpBottomNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_news)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_breaking_news,
                R.id.navigation_saved_news,
                R.id.navigation_search_news
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)*/
        navView.setupWithNavController(navController)
    }
}