package com.zebrostudio.imagecomparisonviewexample

import android.app.Application
import leakcanary.LeakSentry

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LeakSentry.config = LeakSentry.config.copy(watchFragmentViews = false)
    }
}