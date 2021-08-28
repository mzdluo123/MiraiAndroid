package io.github.mzdluo123.mirai.android.activity

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonParser
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.appcenter.trace
import io.github.mzdluo123.mirai.android.miraiconsole.AndroidLoginSolver
import io.github.mzdluo123.mirai.android.service.ServiceConnector
import kotlinx.android.synthetic.main.activity_unsafe_login.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.toast.toast

class UnsafeLoginActivity : AppCompatActivity() {

    private lateinit var conn: ServiceConnector
    val gson = JsonParser()

    companion object {
        const val TAG = "UnsafeLogin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conn = ServiceConnector(this)
        lifecycle.addObserver(conn)
        setContentView(R.layout.activity_unsafe_login)
        initWebView()
        refresh_unsafe_web.setOnRefreshListener {
            unsafe_login_web.reload()
            lifecycleScope.launch {
                delay(1000)
                refresh_unsafe_web.isRefreshing = false
            }
        }
        //  Toast.makeText(this, "请在完成验证后点击右上角继续登录", Toast.LENGTH_LONG).show()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        unsafe_login_web.webViewClient = object : WebViewClient() {
//            override fun shouldInterceptRequest(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): WebResourceResponse? {
//                if (request != null) {
//                    if ("https://report.qqweb.qq.com/report/compass/dc00898" in request.url.toString()) {
//                        authFinish()
//                    }
//                }
//                return super.shouldInterceptRequest(view, request)
//            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                unsafe_login_web.evaluateJavascript(
                    """
                    mqq.invoke = function(a,b,c){ return bridge.invoke(a,b,JSON.stringify(c))}"""
                        .trimIndent()
                ) {}
            }
        }
        unsafe_login_web.webChromeClient = object : WebChromeClient() {

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                val msg = consoleMessage?.message()
                // 按下回到qq按钮之后会打印这句话，于是就用这个解决了。。。。
                if (msg?.startsWith("手Q扫码验证") == true) {
                    authFinish("")
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }
        WebView.setWebContentsDebuggingEnabled(true)
        unsafe_login_web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        unsafe_login_web.addJavascriptInterface(Bridge(), "bridge")

        conn.connectStatus.observe(this, Observer {
            if (it) {
                if (conn.botService.url == null) {
                    toast("获取URL失败，请重试")
                    finish()
                    return@Observer

                }
                unsafe_login_web.loadUrl(conn.botService.url.replace("verify", "qrcode"))
            }
        })
    }

    private fun authFinish(token: String) {
        conn.botService.submitVerificationResult(token)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(AndroidLoginSolver.CAPTCHA_NOTIFICATION_ID)
        finish()
        trace("finish UnsafeLogin")
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (unsafe_login_web.canGoBack()) {
                unsafe_login_web.goBack()
                return true
            }
        }
        return false
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.unsafe_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        authFinish()
//        return true
//    }

    inner class Bridge {
        @JavascriptInterface
        fun invoke(cls: String?, method: String?, data: String?) {
            if (data != null) {
                val jsData = gson.parse(data)
                if (method == "onVerifyCAPTCHA") {
                    authFinish(jsData.asJsonObject["ticket"].asString)
                }
            }
        }
    }
}

