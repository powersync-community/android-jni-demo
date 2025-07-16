package com.powersync.demo.android.jni.wrapper

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

val NativeDispatcher = Executors.newSingleThreadExecutor({ run ->
    Thread(run).also { it.name = "PowerSyncJNIThread" }
}).asCoroutineDispatcher()
