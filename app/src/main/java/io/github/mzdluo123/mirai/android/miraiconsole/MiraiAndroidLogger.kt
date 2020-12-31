package io.github.mzdluo123.mirai.android.miraiconsole

import android.util.Log
import io.github.mzdluo123.mirai.android.AppSettings
import io.github.mzdluo123.mirai.android.BuildConfig
import io.github.mzdluo123.mirai.android.utils.LoopQueue
import net.mamoe.mirai.utils.SimpleLogger
import splitties.experimental.ExperimentalSplittiesApi

private const val LOGGER_IDENTITY = "MA"

@ExperimentalSplittiesApi
private val logStorage = LoopQueue<String>(AppSettings.logBuffer)

@ExperimentalSplittiesApi
private val printToSysLog = AppSettings.printToLogcat


@OptIn(ExperimentalSplittiesApi::class)
object MiraiAndroidLogger :
    SimpleLogger(LOGGER_IDENTITY, { priority: LogPriority, message: String?, e: Throwable? ->
        logStorage.add("[${priority.name}] ${message ?: e}")
        if (BuildConfig.DEBUG || printToSysLog) {
            Log.i("MA", "[${priority.name}] ${message ?: "error"}")
            e?.printStackTrace()
        }
    }) {
    val logs: MutableList<String>
        get() = logStorage.toMutableList()

    fun clearLog() {
        logStorage.clear()

    }
}