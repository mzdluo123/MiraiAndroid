package io.github.mzdluo123.mirai.android.ui.script

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.mzdluo123.mirai.android.BotService
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.script.ScriptHost
import io.github.mzdluo123.mirai.android.script.ScriptManager
import kotlinx.android.synthetic.main.fragment_script.*
import org.jetbrains.anko.*

class ScriptFragment : Fragment() {
    companion object {
        const val IMPORT_SCRIPT = 2
    }
    private val scriptViewModel: ScriptViewModel by lazy {
        ViewModelProvider(this)[ScriptViewModel::class.java]
    }

    private val adapter: ScriptListAdapter by lazy {
        ScriptListAdapter(scriptViewModel)
    }

    private val botServiceConnection = object : ServiceConnection {
        lateinit var helper: IbotAidlInterface

        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            helper = IbotAidlInterface.Stub.asInterface(service)
            scriptViewModel.serviceHelper = helper
            scriptViewModel.refreshScriptList()
        }
    }

    override fun onResume() {
        super.onResume()
        val bindIntent = Intent(activity, BotService::class.java)
        activity?.bindService(bindIntent, botServiceConnection, Context.BIND_AUTO_CREATE)

    }

    override fun onPause() {
        super.onPause()
        activity?.unbindService(botServiceConnection)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_script, container, false).also {
        setHasOptionsMenu(true)
        scriptViewModel.observe(viewLifecycleOwner, Observer {
            adapter.data = it.toMutableList()
            adapter.notifyDataSetChanged()
            adapter.setEmptyView(TextView(context).apply { setText("当前无脚本") })
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        script_recycler.adapter = adapter
        script_recycler.layoutManager = LinearLayoutManager(activity)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plugin_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) return false
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }, IMPORT_SCRIPT)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == IMPORT_SCRIPT && resultCode == Activity.RESULT_OK) {
            intent?.data?.also { importScript(it) }
        }
    }

    private fun importScript(uri: Uri) {
        val typeList = listOf("自动识别后缀名", "Lua", "JavaScript", "Python", "KotlinScript")
        context?.selector("请选择脚本的类型", typeList) { _, type ->
            val scriptFile = ScriptManager.copyFileToScriptDir(context!!, uri)
            val result = scriptViewModel.createScriptFromFile(scriptFile, type)
            if (result) {
                context?.toast("导入成功，当前脚本数量：${scriptViewModel.hostSize}")
            } else {
                context?.toast("导入失败，请检查脚本是否有误！")
            }
        }
    }
}

class ScriptListAdapter(var scriptViewModel: ScriptViewModel) :
    BaseQuickAdapter<ScriptHost.ScriptInfo, BaseViewHolder>(R.layout.item_script) {
    override fun convert(holder: BaseViewHolder, item: ScriptHost.ScriptInfo) {
        with(holder){
            setText(R.id.tv_script_alias, item.name)
            setText(R.id.tv_script_author, item.author)
            setText(R.id.tv_script_version, item.version)
            holder.getView<ImageButton>(R.id.btn_delete).setOnClickListener {
                context.alert("删除脚本后无法恢复，是否确定？") {
                    yesButton {
                        this@ScriptListAdapter.scriptViewModel.deleteScript(holder.layoutPosition)
                    }
                    noButton { }
                }.show()
            }
            holder.getView<ImageButton>(R.id.btn_reload).setOnClickListener {
                context.alert("重新加载该脚本？") {
                    yesButton {
                        this@ScriptListAdapter.scriptViewModel.reloadScript(holder.layoutPosition)
                        context.toast("重载完毕")
                    }
                    noButton { }
                }.show()
            }
            holder.getView<ImageButton>(R.id.btn_edit).setOnClickListener {
                scriptViewModel.openScript(holder.layoutPosition)
            }
            holder.getView<ImageButton>(R.id.btn_setting).setOnClickListener {
            }
        }
    }
}