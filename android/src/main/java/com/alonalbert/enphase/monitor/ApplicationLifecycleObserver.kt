package com.alonalbert.enphase.monitor

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.alonalbert.enphase.monitor.repository.Repository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationLifecycleObserver @Inject constructor(
  private val repository: Repository
) : DefaultLifecycleObserver {

  override fun onDestroy(owner: LifecycleOwner) {
    Timber.d("App process is being destroyed. Closing repository.")
    repository.close()
    super.onDestroy(owner)
  }
}
