package io.github.mzdluo123.mirai.android.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.mzdluo123.mirai.android.BotService
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.R
import kotlinx.android.synthetic.main.activity_unsafe_login.*

class UnsafeLoginActivity : AppCompatActivity() {

    private val conn = object : ServiceConnection {
        lateinit var botService: IbotAidlInterface
        fun sendResult(result: String) {
            botService.submitVerificationResult(result)
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            botService = IbotAidlInterface.Stub.asInterface(service)
            unsafe_login_web.loadUrl(botService.url)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unsafe_login)
        initWebView()
        Toast.makeText(this,"请在完成验证后点击右上角继续登录",Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(baseContext, BotService::class.java)
        bindService(intent, conn, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(conn)
    }

    private fun initWebView() {
        unsafe_login_web.webViewClient = object :WebViewClient(){


        }
        unsafe_login_web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.unsafe_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        conn.sendResult("")
        finish()
        return true
    }

}
