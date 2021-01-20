package io.github.mzdluo123.mirai.android.utils

import io.github.mzdluo123.mirai.android.BotApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request

@OptIn(ExperimentalUnsignedTypes::class)

suspend fun paste(text: String): String {
    val res = withContext(Dispatchers.IO) {
        BotApplication.httpClient.value.newCall(
            Request.Builder().url("https://paste.ubuntu.com/")
                .post(
                    FormBody.Builder().add("poster", "MiraiAndroid")
                        .add("syntax", "text")
                        .add("expiration", "")
                        .add("content", text).build()
                ).build()
        ).execute()
    }

    return res.request.url.toString()
}
