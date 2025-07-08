package com.example.pulseplayer.views

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(
    private val onAppBackgrounded: () -> Unit,
    private val onAppForegrounded: () -> Unit
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        onAppBackgrounded()
    }

    override fun onStart(owner: LifecycleOwner) {
        onAppForegrounded()
    }
}