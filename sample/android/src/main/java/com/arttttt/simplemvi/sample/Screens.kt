package com.arttttt.simplemvi.sample

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screens {

    @Serializable
    data object BottomNavigation : Screens

    @Serializable
    data object Counter : Screens
}