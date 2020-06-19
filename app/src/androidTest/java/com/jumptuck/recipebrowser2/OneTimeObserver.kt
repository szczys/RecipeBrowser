package com.jumptuck.recipebrowser2

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer

// Code by: Alessandro Diaferia
// https://alediaferia.com/2018/12/17/testing-livedata-room-android/

/**
 * Observer implementation that owns its lifecycle and achieves a one-time only observation
 * by marking it as destroyed once the onChange handler is executed.
 *
 * @param handler the handler to execute on change.
 */
class OneTimeObserver<T>(private val handler: (T) -> Unit) : Observer<T>, LifecycleOwner {
    private val lifecycle = LifecycleRegistry(this)
    init {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun onChanged(t: T) {
        handler(t)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}