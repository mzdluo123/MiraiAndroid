package io.github.mzdluo123.mirai.android.ui.plguin

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.mzdluo123.mirai.android.R
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


class PluginFragment : Fragment() {

    private lateinit var pluginViewModel: PluginViewModel
    private lateinit var adapter: PluginsAdapter


    companion object {
        const val SELECT_RESULT_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pluginViewModel =
            ViewModelProvider(this).get(PluginViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        setHasOptionsMenu(true)
        adapter = PluginsAdapter()

        adapter.setOnItemClickListener { _, view, position ->
            val menu = PopupMenu(activity, view)
            menu.gravity = Gravity.END
            menu.menuInflater.inflate(R.menu.plugin_manage, menu.menu)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        pluginViewModel.deletePlugin(position)
                        Toast.makeText(activity, "删除成功，重启后生效", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }

                    else -> return@setOnMenuItemClickListener true
                }
            }
            menu.show()
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
            type = "application/java-archive"
        }

        startActivityForResult(intent, SELECT_RESULT_CODE)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == SELECT_RESULT_CODE && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            val dialog = AlertDialog.Builder(activity)
                .setTitle("正在编译")
                .setMessage("这可能需要一些时间，请不要最小化")
                .setCancelable(false)
                .create()

            resultData?.data?.also { uri ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val path = getRealFilePath(activity!!, uri)
                    Log.e("PATH", path.toString())
                    val name = path?.split("/")?.last() ?: uri.lastPathSegment ?: return@launch
                    dialog.show()
                    withContext(Dispatchers.IO) {
                        copyToFileDir(uri, name)
                    }
                    pluginViewModel.compilePlugin(File(activity!!.getExternalFilesDir(null), name))
                    withContext(Dispatchers.IO) {
                        File(activity!!.getExternalFilesDir(null), name).delete()
                    }
                    dialog.dismiss()
                    Toast.makeText(activity, "安装成功,重启后即可加载", Toast.LENGTH_SHORT).show()
                    pluginViewModel.refreshPluginList()
                }

            }
        }
    }

    @Throws(IOException::class)
    private fun copyToFileDir(uri: Uri, name: String) {
        val plugin = File(activity!!.getExternalFilesDir(null), name)
        plugin.createNewFile()
        val output = plugin.outputStream()
        activity?.contentResolver?.openInputStream(uri)?.use {
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (it.read(buf).also { bytesRead = it } > 0) {
                output.write(buf, 0, bytesRead)
            }
        }
        output.close()
    }

    private fun getRealFilePath(context: Context, uri: Uri?): String? {
        if (null == uri) return null
        val scheme: String? = uri.scheme
        var data: String? = null
        if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.getPath()
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor: Cursor? = context.getContentResolver().query(
                uri,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }

}


class PluginsAdapter() :
    BaseQuickAdapter<File, BaseViewHolder>(R.layout.item_plugin) {
    override fun convert(holder: BaseViewHolder, item: File) {
        holder.setText(R.id.pluginName_text, item.name)
        holder.setText(R.id.pluginSize_text, "${item.length() / 1024}kb")
    }

}