package com.nouri

import androidx.compose.ui.ExperimentalComposeUiApi
import com.nouri.presentation.shared.App
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}
