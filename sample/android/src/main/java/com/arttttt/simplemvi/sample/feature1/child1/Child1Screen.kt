package com.arttttt.simplemvi.sample.feature1.child1

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arttttt.simplemvi.store.Store

@Composable
fun Child1Screen(
    store: Store<Child1Store.Intent, Child1Store.State, Child1Store.SideEffect>
) {
    val state by store.states.collectAsState()

    Child1ScreenContent()
}

@Composable
fun Child1ScreenContent() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Child 1",
        )
    }
}

@Preview
@Composable
fun Child1ScreenPreview() {
    Child1ScreenContent()
}