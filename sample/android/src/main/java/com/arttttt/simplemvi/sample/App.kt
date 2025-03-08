package com.arttttt.simplemvi.sample

import android.app.Application
import com.arttttt.simplemvi.config.configureSimpleMVI

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        configureSimpleMVI {
            strictMode = !BuildConfig.DEBUG
        }
    }
}