package io.github.mzdluo123.mirai.android.miraiconsole

import android.util.Log
import io.github.mzdluo123.mirai.android.AppSettings
import io.github.mzdluo123.mirai.android.BuildConfig
import io.github.mzdluo123.mirai.android.service.BotService
import io.github.mzdluo123.mirai.android.utils.LoopQueue
import net.mamoe.mirai.utils.SimpleLogger
import java.io.PrintWriter
import java.io.StringWriter

private const val LOGGER_IDENTITY = "MA"

private val logStorage = LoopQueue<String>(AppSettings.logBuffer)

private val printToSysLog = AppSettings.printToLogcat

private enum class LogColor(val color: String) {
    INFO("#28BB28"),
    VERBOSE("#44cef6"),
    DEBUG(" #136E70"),
    WARNING("#FEAC48"),
    ERROR("#DD1C1A")
}

fun logException(err: Throwable) {
    val stringWriter = StringWriter()
    err.printStackTrace(PrintWriter(stringWriter))
    MiraiAndroidLogger.error(stringWriter.toString())
}

object MiraiAndroidLogger :
    SimpleLogger(LOGGER_IDENTITY, { priority: LogPriority, message: String?, e: Throwable? ->
        val log = "[${priority.name}] ${message ?: e}"
        val colorLog =
            "<font color=\"${LogColor.valueOf(priority.name).color}\">[${priority.name}]</font> ${
                message?.replace(
                    "\n",
                    "<br>"
                ) ?: e
            }"

        synchronized(this) {
            logStorage.add(colorLog)
            for (i in 0 until BotService.consoleUi.beginBroadcast()) {
                try {
                    BotService.consoleUi.getBroadcastItem(i).newLog(colorLog)
                } catch (remoteE: Exception) {
                    Log.e("MA", remoteE.message ?: "发生错误")
                    remoteE.printStackTrace()
                    logException(remoteE)
                }
            }
            BotService.consoleUi.finishBroadcast()
        }

        if (BuildConfig.DEBUG || printToSysLog) {
            Log.i("MA", log)
            if (e != null) {
                e.printStackTrace()
                logException(e)
            }

        }
    }) {
    val logs: MutableList<String>
        get() = logStorage.toMutableList()


    fun clearLog() {
        logStorage.clear()
    }
}