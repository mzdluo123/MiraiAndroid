package io.github.mzdluo123.mirai.android.miraiconsole

import android.os.Build
import dalvik.system.DexClassLoader
import net.mamoe.mirai.console.plugin.jvm.ExportManager
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * copy from net.mamoe.mirai.console.internal.plugin.JvmPluginClassLoader
 * */

internal class DexPluginClassLoader(
    val file: File,
    odexPath: String,
    parent: ClassLoader?,
    val classLoaders: Collection<DexPluginClassLoader>,
) : DexClassLoader(file.path, odexPath, file.path, parent) {
    //// 只允许插件 getResource 时获取插件自身资源, #205
    override fun getResources(name: String?): Enumeration<URL> = findResources(name)
    override fun getResource(name: String?): URL? = findResource(name)
    // getResourceAsStream 在 URLClassLoader 中通过 getResource 确定资源
    //      因此无需 override getResourceAsStream

    override fun toString(): String {
        return "DexPluginClassLoader{source=$file}"
    }

    private val cache = ConcurrentHashMap<String, Class<*>>()
    internal var declaredFilter: ExportManager? = null

    companion object {
        val loadingLock = ConcurrentHashMap<String, Any>()

        init {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ClassLoader.registerAsParallelCapable()
            }
        }
    }

    override fun findClass(name: String): Class<*> {
        synchronized(kotlin.run {
            val lock = Any()
            loadingLock.putIfAbsent(name, lock) ?: lock
        }) {
            return findClass(name, false) ?: throw ClassNotFoundException(name)
        }
    }

    internal fun findClass(name: String, disableGlobal: Boolean): Class<*>? {
        // First. Try direct load in cache.
        val cachedClass = cache[name]
        if (cachedClass != null) {
            if (disableGlobal) {
                val filter = declaredFilter
                if (filter != null && !filter.isExported(name)) {
                    throw LoadingDeniedException(name)
                }
            }
            return cachedClass
        }
        if (disableGlobal) {
            // ==== Process Loading Request From JvmPluginClassLoader ====
            //
            // If load from other classloader,
            // means no other loaders are cached.
            // direct load
            return kotlin.runCatching {
                super.findClass(name).also { cache[name] = it }
            }.getOrElse {
                if (it is ClassNotFoundException) null
                else throw it
            }?.also {
                // This request is from other classloader,
                // so we need to check the class is exported or not.
                val filter = declaredFilter
                if (filter != null && !filter.isExported(name)) {
                    throw LoadingDeniedException(name)
                }
            }
        }

        // ==== Process Loading Request From JDK ClassLoading System ====

        // First. scan other classLoaders's caches
        classLoaders.forEach { otherClassloader ->
            if (otherClassloader === this) return@forEach
            val filter = otherClassloader.declaredFilter
            if (otherClassloader.cache.containsKey(name)) {
                return if (filter == null || filter.isExported(name)) {
                    otherClassloader.cache[name]
                } else throw LoadingDeniedException("$name was not exported by $otherClassloader")
            }
        }
        classLoaders.forEach { otherClassloader ->
            val other = kotlin.runCatching {
                if (otherClassloader === this) super.findClass(name).also { cache[name] = it }
                else otherClassloader.findClass(name, true)
            }.onFailure { err ->
                if (err is LoadingDeniedException || err !is ClassNotFoundException)
                    throw err
            }.getOrNull()
            if (other != null) return other
        }
        throw ClassNotFoundException(name)
    }
}

internal class LoadingDeniedException(name: String) : ClassNotFoundException(name)
