package io.github.mzdluo123.mirai.android.groovy

import groovy.util.GroovyScriptEngine
import io.github.mzdluo123.mirai.android.BotApplication
import net.mamoe.mirai.Bot
import java.nio.file.Path

class ScriptRuntime(val scriptPath: Path) {


    private val engine = GroovyScriptEngine(BotApplication.context.cacheDir.path)
    private val scriptClass = engine.loadScriptByName(scriptPath.toString()) as Class<GroovyScript>
    private lateinit var scriptInstance: GroovyScript

    fun onEnable(bot: Bot) {
        scriptInstance = scriptClass.newInstance()
        scriptInstance.onEnable(bot)
        bot.eventChannel.registerListenerHost(scriptInstance)
    }

}