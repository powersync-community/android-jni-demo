package com.powersync.demo.android.jni.wrapper

import android.util.Log
import okhttp3.Request
import okhttp3.OkHttpClient

object OkHttpClient {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    @JvmStatic
    fun get(url: String): String {
        val request = Request.Builder().url(url).build()
        return try {
            client.newCall(request).execute().use { response ->
                response.body.string()
            }
        } catch (e: Exception) {
            Log.e("OkHttpClient", "GET request failed", e)
            ""
        }
    }
}
