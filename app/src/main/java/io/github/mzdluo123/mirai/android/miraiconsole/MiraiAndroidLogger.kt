package io.github.mzdluo123.mirai.android.miraiconsole

import android.util.Log
import io.github.mzdluo123.mirai.android.AppSettings
import io.github.mzdluo123.mirai.android.BuildConfig
import io.github.mzdluo123.mirai.android.service.BotService
import io.github.mzdluo123.mirai.android.utils.LoopQueue
import net.mamoe.mirai.utils.SimpleLogger
import splitties.experimental.ExperimentalSplittiesApi

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
        logStorage.add(colorLog)
        for (i in 0 until BotService.consoleUi.beginBroadcast()) {
            try {
                BotService.consoleUi.getBroadcastItem(i).newLog(colorLog)
            } catch (remoteE: Exception) {
                Log.e("MA", remoteE.message ?: "发生错误")
                remoteE.printStackTrace()
            }

        }
        BotService.consoleUi.finishBroadcast()

        if (BuildConfig.DEBUG || printToSysLog) {
            Log.i("MA", log)
            e?.printStackTrace()
        }
    }) {
    val logs: MutableList<String>
        get() = logStorage.toMutableList()

    fun clearLog() {
        logStorage.clear()

    }
}