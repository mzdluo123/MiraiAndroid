package io.github.mzdluo123.mirai.android.ui.console

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import io.github.mzdluo123.mirai.android.BotService
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.R
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*


class ConsoleFragment : Fragment() {

    private lateinit var consoleViewModel: ConsoleViewModel

    private val conn = object : ServiceConnection {
        lateinit var botService: IbotAidlInterface

        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            botService = IbotAidlInterface.Stub.asInterface(service)
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                while (isActive) {
                    val text = botService.log.joinToString(separator = "\n")
                    if (isActive) {
                        withContext(Dispatchers.Main) {
                            log_text?.text = text
//                            main_scroll.scrollTo(0, log_text.bottom)
                        }
                    }
                    delay(200)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        consoleViewModel =
            ViewModelProviders.of(this).get(ConsoleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        return root
    }

    override fun onStart() {
        super.onStart()

        commandSend_btn.setOnClickListener {
            var command = command_input.text.toString()
            lifecycleScope.launch(Dispatchers.Default) {
                if (command.startsWith("/")) {
                    command = command.substring(1)
                }
                conn.botService.runCmd(command)
            }
            command_input.text.clear()
        }
        command_input.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(100)
                main_scroll.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }

        val bindIntent = Intent(activity, BotService::class.java)
        activity?.bindService(bindIntent, conn, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unbindService(conn)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_exit -> {
                val intent = Intent(activity, BotService::class.java)
                intent.putExtra("action", BotService.STOP_SERVICE)
//                conn.botService.clearLog()
                activity?.startService(intent)
                activity?.finish()
            }
            R.id.action_setAutoLogin -> {
                setAutoLogin()
            }

        }
        return false
    }

    private fun setAutoLogin() {
        val alertView = View.inflate(activity, R.layout.alert_autologin, null)
        val pwdInput = alertView.findViewById<EditText>(R.id.password_input)
        val qqInput = alertView.findViewById<EditText>(R.id.qq_input)
        val accountStore = activity!!.getSharedPreferences("account", Context.MODE_PRIVATE)
        val dialog = AlertDialog.Builder(activity)
            .setView(alertView)
            .setCancelable(true)
            .setTitle("设置自动登录")
            .setPositiveButton("设置自动登录", DialogInterface.OnClickListener { dialog, which ->
                accountStore.edit().putString("qq", qqInput.text.toString())
                    .putString("pwd", pwdInput.text.toString()).apply()
                Toast.makeText(activity, "设置成功,重启后生效", Toast.LENGTH_SHORT).show()
            })

            .setNegativeButton("取消自动登录", DialogInterface.OnClickListener { dialog, which ->
                accountStore.edit().putString("qq", "").putString("pwd", "").apply()
                Toast.makeText(activity, "设置成功,重启后生效", Toast.LENGTH_SHORT).show()
            })
            .setNeutralButton("取消", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
        dialog.show()
    }
}



