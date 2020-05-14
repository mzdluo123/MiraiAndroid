package io.github.mzdluo123.mirai.android.script

import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader

object ScriptHostFactory {
    const val UNKNOWN = -1
    const val LUA = 0
    const val JAVASCRIPT = 1
    const val PYTHON = 2
    const val KOTLINSCRIPT = 3
    val NAMES = arrayOf("Lua", "Js", "Python", "Kts")
    fun getTypeFromSuffix(suffix: String) = when (suffix) {
        "lua" -> LUA
        "js" -> JAVASCRIPT
        "py" -> PYTHON
        "kts" -> KOTLINSCRIPT
        else -> UNKNOWN
    }

    fun getScriptHost(scriptFile: File, configFile: File) {
        val config: ScriptHost.ScriptConfig
        if (configFile.exists()) {
            FileReader(configFile).also {
                config = Json.parse(ScriptHost.ScriptConfig.serializer(), it.readText())
            }.close()
        } else {
            config = ScriptHost.ScriptConfig(
                getTypeFromSuffix(configFile.name.split(".").last()),
                configFile.name.split(".").first(),
                true,
                ""
            )
        }
    }
}