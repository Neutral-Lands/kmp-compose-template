package com.neutrallands.nouri.presentation.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.neutrallands.nouri.domain.model.ConnectivityObserver

@Composable
fun App(connectivityObserver: ConnectivityObserver) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isOnline by connectivityObserver.isOnline.collectAsState()

    NouriTheme {
        Scaffold(
            snackbarHost = { NouriSnackbarHost(snackbarHostState) },
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                OfflineBanner(isOnline = isOnline)
                Text("Hello, Nouri!")
            }
        }
    }
}
