package com.nouri

import androidx.compose.ui.window.ComposeUIViewController
import com.nouri.domain.model.ConnectivityObserver
import com.nouri.presentation.shared.App
import org.koin.mp.KoinPlatform

@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController {
    val connectivityObserver = KoinPlatform.getKoin().get<ConnectivityObserver>()
    App(connectivityObserver = connectivityObserver)
}
