package io.github.mzdluo123.mirai.android.ui.script

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.script.ScriptHost
import io.github.mzdluo123.mirai.android.utils.FileUtils
import kotlinx.android.synthetic.main.fragment_script.*

class ScriptFragment : Fragment() {

    private lateinit var scriptViewModel: ScriptViewModel
    private lateinit var adapter: ScriptAdapter

    companion object {
        const val SELECT_SCRIPT = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scriptViewModel = ViewModelProvider(this).get(ScriptViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_script, container, false)
        setHasOptionsMenu(true)
        adapter = ScriptAdapter()
        scriptViewModel.hosts.observe(viewLifecycleOwner, Observer {
            adapter.data = it.toMutableList()
            adapter.notifyDataSetChanged()
        })
        adapter.setOnItemClickListener { _, view, position ->
            PopupMenu(activity, view).apply {
                gravity = Gravity.END
                menuInflater.inflate(R.menu.plugin_manage, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_delete -> {
                            scriptViewModel.deleteScript(position)
                            Toast.makeText(activity, "删除成功，重启后生效", Toast.LENGTH_SHORT).show()
                            return@setOnMenuItemClickListener true
                        }
                        else -> return@setOnMenuItemClickListener true
                    }
                }
            }.show()
        }
        return root
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
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }.let {
            startActivityForResult(it, SELECT_SCRIPT)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == SELECT_SCRIPT && resultCode == Activity.RESULT_OK) {
            intent?.data?.also { uri ->
                scriptViewModel.createScriptFromUri(uri)
                Toast.makeText(context, "导入成功", Toast.LENGTH_LONG).show()
            }
        }
    }
}

class ScriptAdapter : BaseQuickAdapter<ScriptHost, BaseViewHolder>(R.layout.item_plugin) {
    override fun convert(holder: BaseViewHolder, item: ScriptHost) {
        with(holder){
            setText(R.id.pluginName_text, item.config.alias)
            setText(R.id.pluginSize_text, FileUtils.formatFileLength(item.info.fileLength))
        }
    }
}
