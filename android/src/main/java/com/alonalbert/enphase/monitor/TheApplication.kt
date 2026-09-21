package com.alonalbert.enphase.monitor

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TheApplication : Application() {
  @Inject
  lateinit var lifecycleObserver: ApplicationLifecycleObserver

  init {
    Timber.plant(object : Timber.DebugTree() {
      override fun createStackElementTag(element: StackTraceElement) =
        "EnphaseMonitor (${element.fileName}:${element.lineNumber})"
    })

  }
  override fun onCreate() {
    super.onCreate()

    ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
  }
}
