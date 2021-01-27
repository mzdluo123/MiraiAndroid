package io.github.mzdluo123.mirai.android.console

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mzdluo123.mirai.android.miraiconsole.ApkPluginLoader
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApkPluginTest {
    @Test
    fun listAPKPlugin() {
        val data = ApkPluginLoader.listPlugins().toList()
        assertTrue(data.isNotEmpty())
    }
}