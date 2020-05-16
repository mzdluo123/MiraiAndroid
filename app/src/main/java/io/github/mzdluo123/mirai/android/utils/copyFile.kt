package io.github.mzdluo123.mirai.android.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

@Throws(IOException::class)
fun Context.copyToFileDir(uri: Uri, name: String, path: String): File {
    val plugin = File(path, name)
    plugin.createNewFile()
    val output = plugin.outputStream()
    this.contentResolver?.openInputStream(uri)?.use {
        val buf = ByteArray(1024)
        var bytesRead: Int
        while (it.read(buf).also { bytesRead = it } > 0) {
            output.write(buf, 0, bytesRead)
        }
    }
    output.close()
    return plugin
}