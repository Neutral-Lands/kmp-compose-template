package com.nouri.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nouri.domain.model.ConnectivityObserver
import com.nouri.presentation.shared.App
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val connectivityObserver: ConnectivityObserver by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App(connectivityObserver = connectivityObserver)
        }
    }
}
