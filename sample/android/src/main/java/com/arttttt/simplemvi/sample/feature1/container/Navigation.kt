package com.arttttt.simplemvi.sample.feature1.container

sealed interface Navigation {

    data object Child1 : Navigation
}