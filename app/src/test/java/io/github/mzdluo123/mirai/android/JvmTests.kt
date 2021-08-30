package io.github.mzdluo123.mirai.android

import io.github.mzdluo123.mirai.android.utils.paste
import kotlinx.coroutines.runBlocking
import org.junit.Test

class JvmTests {

    @Test
    fun pasteBinTest() {
        runBlocking {
            val url = paste("1234")
            println(url)
            assert(url.isNotBlank())
        }

    }
}