package io.github.mzdluo123.mirai.android

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import io.github.mzdluo123.mirai.android.service.BotService
import io.github.mzdluo123.mirai.android.service.ServiceConnector
import io.github.mzdluo123.mirai.android.ui.plugin.PluginViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PluginCompileTest {

    val testContext = getInstrumentation().context

    @Test
    fun compileTest() {
        val origin = testContext.assets.open("test.jar")
        val out = File(BotApplication.context.externalCacheDir, "tmp.jar")
        val outStream = out.outputStream()
        origin.use {
            it.copyTo(outStream)
            outStream.close()
        }

//        val dexCompile = DexCompiler(BotApplication.context.filesDir,BotApplication.context.cacheDir)
//        val outFile = dexCompile.compile(out,true)
//        dexCompile.copyResourcesAndMove(outFile, File(BotApplication.context.cacheDir,"test-android.jar"))

        runBlocking {
            PluginViewModel().compilePlugin(out, true)
            BotApplication.context.startBotService()
            val feature = CompletableDeferred<Boolean>()
            val conn = ServiceConnector(BotApplication.context)
            BotApplication.context.bindService(
                Intent(BotApplication.context, BotService::class.java),
                conn,
                Context.BIND_AUTO_CREATE
            )
            val console = object : IConsole.Stub() {
                override fun newLog(log: String?) {
                    if (log != null && "SimpleGroupAuth已启动" in log) {
                        feature.complete(true)
                    }
                }
            }
            conn.registerConsole(console)
            File(BotApplication.context.filesDir, "/plugins/test-android.jar").deleteOnExit()
            withTimeout(5000) {
                Assert.assertTrue(feature.await())
            }
        }

    }
}