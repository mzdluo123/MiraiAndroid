package io.github.mzdluo123.mirai.android.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse

suspend fun paste(text: String): String {
    val res = HttpClient().post<HttpResponse>("https://paste.ubuntu.com/") {
        body = MultiPartFormDataContent(formData {
            append("poster", "MiraiAndroid")
            append("syntax", "text")
            append("expiration", "")
            append("content", text)
        })
    }

    return "https://paste.ubuntu.com" + res.headers["Location"].toString()
}