package io.github.mzdluo123.mirai.android.groovy

import groovy.lang.Binding
import groovy.lang.GroovyCallable
import groovy.util.GroovyScriptEngine
import io.github.mzdluo123.mirai.android.BotApplication
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class ScriptRuntime(val scriptPath: Path) {


    private val sharedData = Binding()
    private val engine = GroovyScriptEngine(BotApplication.context.cacheDir.path)
    private val data = ScriptData()

    init {
        sharedData.setVariable("$", data)
        engine.run(scriptPath.toString(), sharedData)

    }

    class ScriptData() {
        var name = ""
        var version = ""
        val listeners: ConcurrentHashMap<Class<Event>, GroovyCallable<Event>> = ConcurrentHashMap()


        fun registerListener(event: Class<Event>, callable: GroovyCallable<Event>) {
            if (!listeners.contains(event)) {
                Bot.instances.first().eventChannel.subscribeAlways(event) {
                }
            }
            listeners[event] = callable

        }
    }
}