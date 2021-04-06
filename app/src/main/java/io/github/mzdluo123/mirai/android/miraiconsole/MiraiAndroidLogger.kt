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

fun logException(err: Throwable?) {
    if (err == null) {
        return
    }
    val stringWriter = StringWriter()
    err.printStackTrace(PrintWriter(stringWriter))
    MiraiAndroidLogger.error(stringWriter.toString())
}

private val lock = Object()

internal fun pushLog(log: String) {
    synchronized(lock) {
        logStorage.add(log)
        for (i in 0 until BotService.consoleUi.beginBroadcast()) {
            try {
                BotService.consoleUi.getBroadcastItem(i).newLog(log)
            } catch (remoteE: Exception) {
                Log.e("MA", remoteE.message ?: "发生错误")
                remoteE.printStackTrace()
                logException(remoteE)
            }
        }
        BotService.consoleUi.finishBroadcast()

    }
}


object MiraiAndroidLogger :
    SimpleLogger(LOGGER_IDENTITY, { priority: LogPriority, message: String?, e: Throwable? ->
        e?.printStackTrace()
        logException(e)
        synchronized(this) {
            message?.split("\n")?.forEach {
                val log = "[${priority.name}] ${it}"
                val colorLog =
                    "<font color=\"${LogColor.valueOf(priority.name).color}\">[${priority.name}]</font><![CDATA[${
                        it.replace(
                            "\n",
                            "\r"
                        )
                    }]]>"
                pushLog(colorLog)


                if (BuildConfig.DEBUG || printToSysLog) {
                    Log.i("MA", log)
                }
            }
        }
    }

    ) {
    val logs: MutableList<String>
        get() = logStorage.toMutableList()


    fun clearLog() {
        logStorage.clear()
    }
}