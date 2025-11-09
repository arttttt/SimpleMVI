package com.arttttt.simplemvi.sample.root

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arttttt.simplemvi.sample.Screens
import com.arttttt.simplemvi.sample.bottomnavigation.BottomNavigationContent
import com.arttttt.simplemvi.store.plusAssign

@Composable
fun RootScreen() {
    val rootStore = remember { RootStore() }

    val state by rootStore.states.collectAsState()

    when (state.mode) {
        SampleMode.Classic -> ClassicModeContent()
        SampleMode.Composition -> {}
        SampleMode.Unknown -> ChooseMode(
            onClassicModeClick = {
                rootStore += RootStore.Intent.SetClassicMode
            },
            onCompositionModeClick = {
                rootStore += RootStore.Intent.SetCompositionMode
            }
        )
    }

    BackHandler(
        enabled = state.mode != SampleMode.Unknown,
    ) {
        rootStore += RootStore.Intent.SetUnknownMode
    }
}

@Composable
private fun ChooseMode(
    onClassicModeClick: () -> Unit,
    onCompositionModeClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onClassicModeClick,
        ) {
            Text(
                text = "Classic"
            )
        }

        Button(
            onClick = onCompositionModeClick,
        ) {
            Text(
                text = "Composition"
            )
        }
    }
}

@Composable
private fun ClassicModeContent() {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.imePadding(),
        navController = navController,
        startDestination = Screens.BottomNavigation,
    ) {
        composable<Screens.BottomNavigation> {
            BottomNavigationContent()
        }
    }
}

@Preview
@Composable
private fun RootScreenPreview() {
    RootScreen()
}