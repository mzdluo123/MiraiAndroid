package io.github.mzdluo123.mirai.android.utils

import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object RequestUtil {
    suspend fun get(url: String, clientBuilder: OkHttpClient.Builder.() -> Unit = {}): String? {
        val client = OkHttpClient.Builder().apply(clientBuilder).build()
        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()
        val call: Call = client.newCall(request)
        return suspendCoroutine { continuation ->
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response.body?.string())
                }
            })
        }
    }
}