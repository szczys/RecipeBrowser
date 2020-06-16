package com.jumptuck.recipebrowser2

import android.app.Application
import timber.log.Timber

class RecipeBrowserApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}