package io.github.mzdluo123.mirai.android

import android.content.Context
import android.content.Intent
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import io.github.mzdluo123.mirai.android.service.BotService
import io.github.mzdluo123.mirai.android.service.ServiceConnector
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
class BotServiceTest {
    @get:Rule
    val rule = ServiceTestRule()

    @Before
    fun init() {
        IdlingRegistry.getInstance().register(IdleResources.botServiceLoading)
    }

    @Test
    fun startBotTest() {
        BotApplication.context.startBotService()
        val conn = ServiceConnector(BotApplication.context)
        rule.bindService(
            Intent(BotApplication.context, BotService::class.java),
            conn,
            Context.BIND_AUTO_CREATE
        )
        Assert.assertTrue(conn.botService.botInfo.isNotEmpty())
    }

    @Test
    fun liveLogTest() {
        runBlocking {
            BotApplication.context.startBotService()
            val feature = CompletableDeferred<Boolean>()
            val conn = ServiceConnector(BotApplication.context)
            rule.bindService(
                Intent(BotApplication.context, BotService::class.java),
                conn,
                Context.BIND_AUTO_CREATE
            )
            val console = object : IConsole.Stub() {
                override fun newLog(log: String?) {
                    if (log != null) {
                        feature.complete(true)
                    }
                }
            }
            conn.botService.registerConsole(console)

            withTimeout(5000) {
                Assert.assertTrue(feature.await())

            }
        }
    }

    @Test
    fun helpTest() {
        runBlocking {
            BotApplication.context.startBotService()
            val feature = CompletableDeferred<Boolean>()
            val conn = ServiceConnector(BotApplication.context)
            rule.bindService(
                Intent(BotApplication.context, BotService::class.java),
                conn,
                Context.BIND_AUTO_CREATE
            )
            val console = object : IConsole.Stub() {
                override fun newLog(log: String?) {
                    if (log != null && "/help" in log) {
                        feature.complete(true)
                    }
                }
            }
            conn.botService.registerConsole(console)
            launch {
                repeat(10) {
                    conn.botService.runCmd("/help")
                    delay(100)
                }
            }
            withTimeout(5000) {
                Assert.assertTrue(feature.await())
            }
        }
    }

    @Test
    fun fastRestartTest() {
        runBlocking {
            BotApplication.context.startBotService()
            val feature = CompletableDeferred<Boolean>()
            val conn = ServiceConnector(BotApplication.context)
            val console = object : IConsole.Stub() {
                var count = 0
                override fun newLog(log: String?) {
                    if (log != null && "mirai-console started successfully." in log) {
                        count++
                        if (count == 1) {
                            thread {
                                BotApplication.context.stopBotService()
                                Thread.sleep(1000)
                                BotApplication.context.startBotService()
                                rule.bindService(
                                    Intent(BotApplication.context, BotService::class.java),
                                    conn,
                                    Context.BIND_AUTO_CREATE
                                )
                            }.start()
                        }
                        if (count == 2) {
                            feature.complete(true)

                        }
                    }
                }
            }
            conn.registerConsole(console)
            rule.bindService(
                Intent(BotApplication.context, BotService::class.java),
                conn,
                Context.BIND_AUTO_CREATE
            )
            withTimeout(8000) {
                Assert.assertTrue(feature.await())
            }

        }
    }
//
//    @Test
//    fun broadcastReceiverTest() {
//        runBlocking {
//            AppSettings.allowPushMsg = true
//            BotApplication.context.startBotService()
//            val feature = CompletableDeferred<Boolean>()
//            val conn = ServiceConnector(BotApplication.context)
//            val console = object : IConsole.Stub() {
//                override fun newLog(log: String?) {
//                    if (log != null && "成功处理一个群消息推送请求" in log) {
//                        feature.complete(true)
//                    }
//                }
//            }
//            conn.registerConsole(console)
//            rule.bindService(
//                Intent(BotApplication.context, BotService::class.java),
//                conn,
//                Context.BIND_AUTO_CREATE
//            )
//
//            launch {
//                repeat(30) {
//                    LocalBroadcastManager.getInstance(BotApplication.context)
//                        .sendBroadcast(Intent("io.github.mzdluo123.mirai.android.PushMsg").apply {
//                            data =
//                                Uri.parse("ma://sendGroupMsg?msg=HelloWorld&id=4234234&at=43434343")
//                        })
//                    delay(300)
//                }
//            }
//            withTimeout(6000) {
//                Assert.assertTrue(feature.await())
//            }
//        }
//    }
}