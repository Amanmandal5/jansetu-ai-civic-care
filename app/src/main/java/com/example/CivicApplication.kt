package com.example

import android.app.Application
import com.example.di.AppContainer

class CivicApplication : Application() {

    /**
     * Central Dependency Injection / Service Locator container
     * providing singleton app dependencies across the application lifecycle.
     */
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
