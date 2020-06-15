package io.github.mzdluo123.mirai.android

import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mzdluo123.mirai.android.activity.MainActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val regex = "\\{.{8}-(.{4}-){3}.{12}\\}\\.mirai".toRegex()
        assertEquals("io.github.mzdluo123.mirai.android", appContext.packageName)
    }

    @Test
    fun pushMsgTest(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        appContext.startActivity(Intent(appContext,MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        Thread.sleep(10000)

        appContext.sendBroadcast(Intent("io.github.mzdluo123.mirai.android.PushMsg").apply {
            data = Uri.parse("ma://sendGroupMsg?msg=HelloWorld&id=655057127")
        })

        Thread.sleep(1000)
    }
}
