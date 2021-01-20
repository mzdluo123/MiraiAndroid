package io.github.mzdluo123.mirai.android.ui.script

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.mzdluo123.mirai.android.R

class ScriptListAdapter(var fragment: ScriptFragment) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_script) {
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.tv_script_alias, item)

    }
}