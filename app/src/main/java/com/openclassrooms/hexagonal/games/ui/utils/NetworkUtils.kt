package com.openclassrooms.hexagonal.games.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class providing methods related to network connectivity.
 *
 * This class is used to verify whether the device currently has access
 * to an active internet connection. It leverages [ConnectivityManager]
 * and [NetworkCapabilities] for accurate network state detection.
 *
 * Annotated with [@Singleton] to ensure only one instance exists in the
 * dependency graph.
 *
 * @param context The application context used to access system services.
 */
@Singleton
class NetworkUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Checks whether the device has an active network connection with
     * internet capability.
     *
     * @return `true` if an active network connection with internet access is available,
     * or `false` otherwise.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
