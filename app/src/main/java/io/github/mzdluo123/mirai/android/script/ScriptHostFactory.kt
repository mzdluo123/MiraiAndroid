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

    fun getScriptHost(scriptFile: File, configFile: File, type: Int): ScriptHost {
        var trueType: Int = type
        if (trueType == UNKNOWN) {
            if (configFile.exists()) {
                FileReader(configFile).apply {
                    trueType = Json.parse(ScriptHost.ScriptConfig.serializer(), readText()).type
                }.close()
            } else {
                trueType = getTypeFromSuffix(scriptFile.getSuffix())
            }
        }
        return when (type) {
            LUA -> LuaScriptHost(scriptFile, configFile)
            else -> throw Exception("Unknown script type!")
        }
    }

    private fun File.getSuffix() = name.split(".").last()

}