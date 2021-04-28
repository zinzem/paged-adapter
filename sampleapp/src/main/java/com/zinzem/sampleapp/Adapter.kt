package com.zinzem.sampleapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.zinzem.pagedadapter.PagedAdapter
import com.zinzem.pagedadapter.TypedDiffUtil

class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(text: String) {
        view.findViewById<TextView>(R.id.tv)?.text = text
    }
}

class Adapter(
    lifecycleOwner: LifecycleOwner,
) : PagedAdapter<BaseListItem, ViewHolder>(TypedDiffUtil(), lifecycleOwner) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is BaseListItem.Item -> R.layout.item_text
            is BaseListItem.LoadingItem -> R.layout.item_loading
            is BaseListItem.ErrorItem -> R.layout.item_error
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is BaseListItem.Item -> holder.bind(item.text)
        }
    }

    override fun getLoadingItem() = BaseListItem.LoadingItem
    override fun getErrorItem() = BaseListItem.ErrorItem
}