package com.arttttt.simplemvi.sample.feature1.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.arttttt.simplemvi.composition.scoped
import com.arttttt.simplemvi.sample.feature1.child1.Child1Screen
import com.arttttt.simplemvi.store.Store

@Composable
fun Feature1ContainerScreen(
    store: Store<Feature1ContainerStore.State, Feature1ContainerStore.State, Feature1ContainerStore.SideEffect>,
) {
    val state by store.states.collectAsState()

    when (state.navigation) {
        is Navigation.Child1 -> Child1Screen(
            store = store.scoped(),
        )
    }
}

@Preview
@Composable
fun Feature1ContainerScreenContentPreview() {
    //Feature1ContainerScreenContent()
}