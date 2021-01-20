package io.github.mzdluo123.mirai.android.groovy

import groovy.util.GroovyScriptEngine
import io.github.mzdluo123.mirai.android.BotApplication
import net.mamoe.mirai.Bot
import java.nio.file.Path

class ScriptRuntime(val scriptPath: Path) {

    private val engine =
        GroovyScriptEngine(BotApplication.context.cacheDir.path, javaClass.classLoader)
    private val scriptClass = engine.loadScriptByName(scriptPath.toString()) as Class<GroovyScript>
    private val scriptInstance: GroovyScript = scriptClass.newInstance()
    val fileName = scriptPath.fileName

    fun onEnable(bot: Bot) {
        scriptInstance.onEnable(bot)
    }

    fun onDisable() {
        scriptInstance.onDisable()
    }
}