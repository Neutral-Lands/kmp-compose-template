package com.neutrallands.nouri.presentation.web.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.neutrallands.nouri.presentation.shared.NouriSnackbarHost
import com.neutrallands.nouri.presentation.shared.NouriTheme
import com.neutrallands.nouri.presentation.web.navigation.WebRoute
import com.neutrallands.nouri.presentation.web.navigation.WebSidebar
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WebScaffold(
    selectedRoute: WebRoute,
    onNavigate: (WebRoute) -> Unit,
    snackbarHostState: SnackbarHostState,
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Scaffold(
        snackbarHost = { NouriSnackbarHost(snackbarHostState) },
    ) { _ ->
        // Row measures sidebar first (fixed width via widthIn); fillMaxWidth() on the
        // content column correctly fills the remaining available width in Compose's
        // sequential Row measurement — no weight needed.
        Row(modifier = Modifier.fillMaxSize()) {
            WebSidebar(
                selectedRoute = selectedRoute,
                onNavigate = onNavigate,
                modifier = Modifier.fillMaxHeight(),
            )
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                topBar()
                Surface(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    content()
                }
            }
        }
    }
}

@Preview
@Composable
fun WebScaffoldPreview() {
    NouriTheme {
        WebScaffold(
            selectedRoute = WebRoute.Dashboard,
            onNavigate = {},
            snackbarHostState = remember { SnackbarHostState() },
        ) {
            Text("Main content area")
        }
    }
}
