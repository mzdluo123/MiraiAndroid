package io.github.mzdluo123.mirai.android

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val regex = "\\{.{8}-(.{4}-){3}.{12}}\\.mirai".toRegex()
        assertEquals(4, 2 + 2)
    }
}
