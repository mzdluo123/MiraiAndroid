package io.github.mzdluo123.mirai.android.ui.plguin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.mzdluo123.mirai.android.R
import kotlinx.android.synthetic.main.fragment_gallery.*
import java.io.File

class PluginFragment : Fragment() {

    private lateinit var pluginViewModel: PluginViewModel
    private lateinit var adapter: PluginsAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pluginViewModel =
            ViewModelProvider(this).get(PluginViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        adapter = PluginsAdapter()

        adapter.setOnItemChildClickListener { adapter, view, position ->
            val menu  = PopupMenu(context,view)

        }

        pluginViewModel.pluginList.observe(viewLifecycleOwner, Observer {
            adapter.data = it.toMutableList()
            adapter.notifyDataSetChanged()
        })
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        plugin_recycler.adapter = adapter
        plugin_recycler.layoutManager = LinearLayoutManager(activity)
    }
}


class PluginsAdapter() :
    BaseQuickAdapter<File, BaseViewHolder>(R.layout.item_plugin) {
    override fun convert(holder: BaseViewHolder, item: File) {
        holder.setText(R.id.pluginName_text, item.name)
        holder.setText(R.id.pluginSize_text, "${item.length() / 1024 }kb")
    }

}