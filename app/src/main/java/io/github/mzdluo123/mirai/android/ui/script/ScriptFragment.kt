package io.github.mzdluo123.mirai.android.ui.script

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.github.mzdluo123.mirai.android.R

class ScriptFragment : Fragment() {

    private lateinit var slideshowViewModel: ScriptViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        slideshowViewModel =
                ViewModelProviders.of(this).get(ScriptViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_script, container, false)
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plugin_add,menu)

    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return false
        }
        // TODO 添加脚本


        return true
    }
}
