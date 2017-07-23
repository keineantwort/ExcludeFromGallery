package de.keineantwort.android.excludefromgallery

import android.app.Application
import timber.log.Timber

/**
 * Created by martin on 15.07.17.
 */
class EFGApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}