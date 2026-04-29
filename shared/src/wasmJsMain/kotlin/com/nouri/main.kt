package com.nouri

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.nouri.domain.model.ConnectivityObserver
import com.nouri.presentation.shared.App
import kotlinx.browser.document
import org.koin.mp.KoinPlatform

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin(supabaseUrl = getSupabaseUrl(), supabaseAnonKey = getSupabaseAnonKey())
    val connectivityObserver = KoinPlatform.getKoin().get<ConnectivityObserver>()
    ComposeViewport(document.body!!) {
        App(connectivityObserver = connectivityObserver)
    }
}
