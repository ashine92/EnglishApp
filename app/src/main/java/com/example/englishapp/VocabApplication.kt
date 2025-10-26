package com.example.englishapp

import android.app.Application
import com.example.englishapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class VocabApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)  // Thêm logging để debug
            androidContext(this@VocabApplication)
            modules(appModule)
        }
    }
}