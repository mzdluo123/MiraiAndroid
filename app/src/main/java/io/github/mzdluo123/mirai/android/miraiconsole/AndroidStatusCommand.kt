package io.github.mzdluo123.mirai.android.miraiconsole

import io.github.mzdluo123.mirai.android.utils.MiraiAndroidStatus
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandOwner
import net.mamoe.mirai.console.command.SimpleCommand

object AndroidStatusCommand : SimpleCommand(
    ConsoleCommandOwner,
    "androidstatus",
    "astatus",
    description = "查询MiraiAndroid状态信息"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage(MiraiAndroidStatus.recentStatus().format())

    }
}