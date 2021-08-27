package io.github.mzdluo123.mirai.android.utils

import android.annotation.SuppressLint
import io.github.mzdluo123.mirai.android.BotApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalUnsignedTypes::class)
//
//suspend fun paste(text: String): String {
//    val res = withContext(Dispatchers.IO) {
//        BotApplication.httpClient.value.newCall(
//            Request.Builder().url("https://paste.ubuntu.com/")
//                .post(
//                    FormBody.Builder().add("poster", "MiraiAndroid")
//                        .add("syntax", "text")
//                        .add("expiration", "")
//                        .add("content", text).build()
//                ).build()
//        ).execute()
//    }
//
//    return res.request.url.toString()
//}

//
suspend fun paste(text: String): String {
    val res = withContext(Dispatchers.IO) {
        BotApplication.httpClient.value.newCall(
            Request.Builder().url("https://paste.rs/web")
                .post(
                    FormBody.Builder()
                        .add("extension", "txt")
                        .add("content", text).build()
                ).build()
        ).execute()
    }
    return res.request.url.toString()
}


//suspend fun paste(text: String): String {
//    return withContext(Dispatchers.Main) {
//        val webView = WebView(BotApplication.context)
//
//        webView.settings.apply {
//            javaScriptEnabled = true
//        }
//        webView.webViewClient = object :WebViewClient(){
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//
//                    webView.evaluateJavascript(
//                        """
//                        document.getElementById("id_expires").selectedIndex = 4
//                        document.getElementById("id_lexer").selectedIndex = 0
//                        document.getElementById("id_content").value = `$text`
//                        document.getElementsByClassName("btn")[0].click()
//                    """.trimIndent()
//                    ) {}
//
//            }
//
//        }
//        webView.loadUrl("https://pastebin.mozilla.org/")
//        while (webView.url == "https://pastebin.mozilla.org/") {
//            delay(100)
//        }
//        val url = webView.url ?: "上传出错"
//        webView.destroy()
//        url
//    }
//}
