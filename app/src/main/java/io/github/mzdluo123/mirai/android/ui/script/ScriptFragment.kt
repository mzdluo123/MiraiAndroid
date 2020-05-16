package io.github.mzdluo123.mirai.android.ui.script

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.script.ScriptHost
import kotlinx.android.synthetic.main.fragment_script.*
import org.jetbrains.anko.*

class ScriptFragment : Fragment() {
    private val scriptViewModel: ScriptViewModel by lazy {
        ViewModelProvider(this)[ScriptViewModel::class.java]
    }

    private val adapter: ScriptListAdapter by lazy {
        ScriptListAdapter(scriptViewModel)
    }

    companion object {
        const val IMPORT_SCRIPT = 2
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
        context?.selector("请选择脚本的类型", typeList) { _, i ->
            var result = scriptViewModel.createScriptFromUri(uri, i)
            if (result) {
                context?.toast("导入成功！")
            } else {
                context?.toast("导入失败，请检查脚本是否有误！")
            }
        }
    }
}

class ScriptListAdapter(var scriptViewModel: ScriptViewModel) :
    BaseQuickAdapter<ScriptHost, BaseViewHolder>(R.layout.item_script) {
    override fun convert(holder: BaseViewHolder, item: ScriptHost) {
        with(holder){
            setText(R.id.tv_script_alias, item.config.alias)
            setText(R.id.tv_script_author, item.info.author)
            setText(R.id.tv_script_version, item.info.version)
            holder.getView<ImageButton>(R.id.btn_delete).setOnClickListener {
                context.alert("确定删除？") {
                    yesButton {
                        this@ScriptListAdapter.scriptViewModel.deleteScript(position)
                    }
                    noButton { }
                }.show()
            }
            holder.getView<ImageButton>(R.id.btn_reload).setOnClickListener {
                scriptViewModel.reloadScript(holder.layoutPosition)
            }
            holder.getView<ImageButton>(R.id.btn_edit).setOnClickListener {
                val scriptFile = scriptViewModel.getScriptFile(holder.layoutPosition)
                val provideUri: Uri
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    var contentValues = ContentValues(1)
                    contentValues.put(MediaStore.Images.Media.DATA, scriptFile.getAbsolutePath())
                    provideUri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )!!
                } else {
                    provideUri = Uri.fromFile(scriptFile);
                }
                context.startActivity(
                    Intent("android.intent.action.VIEW").apply {
                        addCategory("android.intent.category.DEFAULT")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(MediaStore.EXTRA_OUTPUT, provideUri)
                        type = "text/plain"
                        //setDataAndType(provideUri, )
                    })
            }
            holder.getView<ImageButton>(R.id.btn_setting).setOnClickListener {
            }
        }
    }
}