package com.neutrallands.nouri

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.neutrallands.nouri.domain.model.ConnectivityObserver
import com.neutrallands.nouri.presentation.web.WebApp
import kotlinx.browser.document
import org.koin.mp.KoinPlatform

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin(supabaseUrl = getSupabaseUrl(), supabaseAnonKey = getSupabaseAnonKey())
    val connectivityObserver = KoinPlatform.getKoin().get<ConnectivityObserver>()
    ComposeViewport(document.body!!) {
        WebApp(connectivityObserver = connectivityObserver)
    }
}
