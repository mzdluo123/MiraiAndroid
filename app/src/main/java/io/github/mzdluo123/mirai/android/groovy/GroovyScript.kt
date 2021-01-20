package io.github.mzdluo123.mirai.android.groovy

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.utils.MiraiLogger

open class GroovyScript : ListenerHost {
    lateinit var logger: MiraiLogger


    open fun onEnable(bot: Bot) {
        logger = bot.logger
        bot.eventChannel.registerListenerHost(this)
    }

    open fun onDisable() {

    }
}