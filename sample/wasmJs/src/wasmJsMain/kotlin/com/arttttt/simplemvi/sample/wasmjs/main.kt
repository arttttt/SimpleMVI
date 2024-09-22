package com.arttttt.simplemvi.sample.wasmjs

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arttttt.simplemvi.sample.wasmjs.bottomnavigation.BottomNavigationContent
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        MaterialTheme {
            BottomNavigationContent()
        }
    }
}