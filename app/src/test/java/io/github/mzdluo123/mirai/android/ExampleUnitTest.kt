package io.github.mzdluo123.mirai.android

import io.github.mzdluo123.mirai.android.utils.DexCompiler
import io.github.mzdluo123.mirai.android.utils.paste
import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
//    @Test
//    fun addition_isCorrect() {
//        val regex = "\\{.{8}-(.{4}-){3}.{12}}\\.mirai".toRegex()
//        assertEquals(4, 2 + 2)
//    }
//
//    @Test
//    fun compileTest(){
//        val compiler = DexCompiler(File("test"),"cache")
//        compiler.compile(File("test","HsoPro-1.0.0.jar"))
//    }
//
//    @Test
//    fun copyResourcesTest(){
//        val compiler = DexCompiler(File("test"))
//        val originFile =File("test","HsoPro-1.0.0.jar")
//        val newFile =File("test/temp","HsoPro-1.0.0-android.jar")
//        compiler.copyResourcesAndMove(originFile,newFile)
//    }

    @Test
    fun pastebinTest(){
       runBlocking {
           val url = GlobalScope.async(Dispatchers.IO) {
               paste("test")
           }
           println(url.await())
       }

    }
}
