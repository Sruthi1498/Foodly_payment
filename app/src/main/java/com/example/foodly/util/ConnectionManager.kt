package com.example.foodly.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectionManager {

    //TODO this class doesn't have any state. So this should be static ( companion object ).
    // No need to create a ConnectionManager() manager instance everytime to call this method
    fun checkConnectivity(context: Context): Boolean {

        val connectionManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectionManager.activeNetworkInfo

        return if (activeNetwork?.isConnected != null) {
            activeNetwork.isConnected
        } else {
            false
        }
    }


}