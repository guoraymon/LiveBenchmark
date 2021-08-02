package com.kuolw.livebenchmark

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SourcesAdapter(list: MutableList<Source>) :
    BaseQuickAdapter<Source, BaseViewHolder>(R.layout.source_item, list) {
    override fun convert(holder: BaseViewHolder, item: Source) {
        holder.setText(R.id.name, item.name)
        holder.setText(R.id.url, item.url)
    }
}