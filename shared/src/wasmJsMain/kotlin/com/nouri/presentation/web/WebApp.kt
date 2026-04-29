package com.nouri.presentation.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nouri.domain.model.ConnectivityObserver
import com.nouri.presentation.shared.NouriTheme
import com.nouri.presentation.shared.OfflineBanner
import com.nouri.presentation.web.layout.WebScaffold
import com.nouri.presentation.web.navigation.WebRoute
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WebApp(connectivityObserver: ConnectivityObserver) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isOnline by connectivityObserver.isOnline.collectAsState()
    var selectedRoute by remember { mutableStateOf(WebRoute.Dashboard) }

    NouriTheme {
        Column {
            OfflineBanner(isOnline = isOnline)
            WebScaffold(
                selectedRoute = selectedRoute,
                onNavigate = { selectedRoute = it },
                snackbarHostState = snackbarHostState,
            ) {
                WebScreenPlaceholder(route = selectedRoute)
            }
        }
    }
}

@Composable
private fun WebScreenPlaceholder(route: WebRoute) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = route.label,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview
@Composable
fun WebAppPreview() {
    NouriTheme {
        WebScreenPlaceholder(route = WebRoute.Dashboard)
    }
}
