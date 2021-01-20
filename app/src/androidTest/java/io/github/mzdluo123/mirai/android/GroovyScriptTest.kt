package io.github.mzdluo123.mirai.android

import androidx.test.runner.AndroidJUnit4
import groovy.lang.GroovyShell
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroovyScriptTest {
    @Test
    fun testGroovy() {
        val shell = GroovyShell()
        shell.evaluate("int foo=123")

    }
}