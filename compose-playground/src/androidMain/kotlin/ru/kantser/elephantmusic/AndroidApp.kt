package ru.kantser.elephantmusic

import android.app.Application
import android.content.Context
import org.koin.core.context.startKoin
import ru.kantser.elephantmusic.di.appModule

object AppContextHolder {
    lateinit var context: Context
}

class ElephantApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.context = applicationContext
        startKoin { modules(appModule) }
    }
}
