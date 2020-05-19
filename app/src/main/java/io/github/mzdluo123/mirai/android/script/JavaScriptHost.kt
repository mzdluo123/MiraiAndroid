package io.github.mzdluo123.mirai.android.script

import android.util.Log
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import org.itxtech.miraijs.coreadapter.IPluginLogger
import org.itxtech.miraijs.coreadapter.JsRuntime
import java.io.File

class JavaScriptHost(scriptFile: File, configFile: File) : ScriptHost(scriptFile, configFile) {
    var runtimes = mutableListOf<JsRuntime>()

    override fun onFetchBot(bot: Bot) {
        if (!config.enable) return
        val runtime = JsRuntime(scriptFile, JsLogger)
        runtime.load()
        runtime.enable(bot)
        runtimes.add(runtime)
    }

    override fun onCreate(): ScriptInfo {
        //只获取信息，enable时再创建运行时
        val runtime = JsRuntime(scriptFile, JsLogger)
        runBlocking {
            runtime.load().join()
        }
        Log.e("Js", "loaded")
        val runtimeInfo = runtime.pluginInfo
        return ScriptInfo(
            runtimeInfo.name,
            runtimeInfo.author,
            runtimeInfo.version,
            runtimeInfo.website,
            scriptFile.length()
        )
    }

    override fun onDisable() {
        runtimes.forEach {
            it.disable()
            it.unload()
        }
        runtimes.clear()
    }

    override fun onEnable() {

    }

    var JsLogger = object : IPluginLogger {
        override fun debug(str: String) {
            logger(str)
        }

        override fun error(str: String, e: Any?) {
            logger(str)
        }

        override fun info(str: String) {
            logger(str)
        }

        override fun verbose(str: String) {
            logger(str)
        }

        override fun warning(str: String) {
            logger(str)
        }

    }

}