package io.github.mzdluo123.mirai.android.utils

import io.github.mzdluo123.mirai.android.BotApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Dns
import okhttp3.Request
import java.net.InetAddress

@ExperimentalUnsignedTypes
class SafeDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return runBlocking {
            val res =
                BotApplication.httpClient.value.newCall(
                    Request.Builder()
                        .url("https://cloudflare-dns.com/dns-query?name=$hostname&type=A")
                        .header("accept", "application/dns-json").build()
                ).execute()
            val json = BotApplication.json.value.parseToJsonElement(res.body!!.string())
            return@runBlocking listOf(
                InetAddress.getByName(
                    json.jsonObject["Answer"]?.jsonArray?.get(
                        0
                    )?.jsonObject?.get("data")?.jsonPrimitive?.content
                )
            )
        }
    }
}
