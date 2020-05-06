package io.github.mzdluo123.mirai.android.ui.script

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.utils.FileUtils
import io.github.mzdluo123.mirai.android.utils.copyToFileDir
import kotlinx.android.synthetic.main.fragment_script.*
import java.io.File

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
        scriptViewModel =
            ViewModelProvider(this).get(ScriptViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_script, container, false)
        setHasOptionsMenu(true)
        adapter = ScriptAdapter()

        scriptViewModel.pluginList.observe(viewLifecycleOwner, Observer {
            adapter.data = it.toMutableList()
            adapter.notifyDataSetChanged()
        })

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        script_recycler.adapter = adapter
        script_recycler.layoutManager = LinearLayoutManager(activity)
    }

    override fun onResume() {
        super.onResume()
        scriptViewModel.refreshScriptList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plugin_add, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return false
        }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            addCategory(Intent.CATEGORY_OPENABLE)

            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            type = "*/*"
        }

        startActivityForResult(intent, SELECT_SCRIPT)

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_SCRIPT && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            data?.data?.also { uri ->
                val name = uri.lastPathSegment?.replace(":","-")
                if (name?.split(".")?.last() ?: "" != "lua") {
                    Toast.makeText(context, "非法文件", Toast.LENGTH_LONG).show()
                    return
                }
                context?.copyToFileDir(
                    uri,
                    name!!,
                    context!!.getExternalFilesDir("scripts")!!.absolutePath
                )
                Toast.makeText(context, "导入成功", Toast.LENGTH_LONG).show()

            }
        }

    }
}

class ScriptAdapter : BaseQuickAdapter<File, BaseViewHolder>(R.layout.item_plugin) {
    override fun convert(holder: BaseViewHolder, item: File) {
        holder.setText(R.id.pluginName_text, item.name)
        holder.setText(R.id.pluginSize_text, "${item.length() / 1024}kb")
    }


}
