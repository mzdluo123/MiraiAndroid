package io.github.mzdluo123.mirai.android.miraiconsole

import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.console.util.ConsoleInput

object AndroidConsoleInput : ConsoleInput {
    val inputQueue = Channel<String> { }
    override suspend fun requestInput(hint: String): String {
        return inputQueue.receive()
    }
}