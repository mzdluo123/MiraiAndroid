package io.github.mzdluo123.mirai.android.utils

import io.github.mzdluo123.mirai.android.BotApplication
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.content
import okhttp3.Dns
import java.net.InetAddress

class SafeDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return runBlocking {
            val res =
                BotApplication.httpClient.value.get<String>("https://cloudflare-dns.com/dns-query?name=$hostname&type=A") {
                    headers.append("accept", "application/dns-json")
                }
            val json = BotApplication.json.value.parseJson(res)
            return@runBlocking listOf(
                InetAddress.getByName(
                    json.jsonObject["Answer"]?.jsonArray?.get(
                        0
                    )?.jsonObject?.get("data")?.content
                )
            )
        }
    }
}
