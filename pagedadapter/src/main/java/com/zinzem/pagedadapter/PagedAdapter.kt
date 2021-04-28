package com.zinzem.pagedadapter

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

private const val DEFAULT_PAGE_SIZE = 8

interface PagedAdapterCallback<ITEM> {
    suspend fun onLoadNextPage(nextPageId: String?, pageSize: Int): Page<ITEM>
}

abstract class PagedAdapter<ITEM, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<ITEM>,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<ITEM, VH>(diffCallback) {

    private var callback: PagedAdapterCallback<ITEM>? = null
    private var pageSize: Int = DEFAULT_PAGE_SIZE
    private var isLoading = false
    private var nextPageId: String? = null
    private var hasError = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            nextPageId?.let {nextPageId ->
                if (isLoading || hasError)
                    return

                val isScrollingDown = dy > 0
                recyclerView.layoutManager.takeIf { isScrollingDown }?.apply {
                    when (this) {
                        is LinearLayoutManager -> {
                            val lastItemPosition = itemCount - 1
                            val lastVisibleItemPosition = findLastCompletelyVisibleItemPosition()
                            if (lastVisibleItemPosition >= lastItemPosition - DEFAULT_PAGE_SIZE) {
                                loadNextPage(nextPageId)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(scrollListener)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        recyclerView.removeOnScrollListener(scrollListener)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    abstract fun getLoadingItem(): ITEM
    abstract fun getErrorItem(): ITEM

    fun loadFirstPage() = loadNextPage(null)

    fun refresh() {
        submitList(emptyList())
        loadNextPage(null)
    }

    fun setCallback(callback: PagedAdapterCallback<ITEM>): PagedAdapter<ITEM, VH> {
        this.callback = callback
        return this
    }

    fun setPageSize(pageSize: Int): PagedAdapter<ITEM, VH> {
        this.pageSize = pageSize
        return this
    }

    private fun loadNextPage(pageId: String?) {
        if (!isLoading) {
            isLoading = true
            submitList(currentList.plus(getLoadingItem()))
            lifecycleOwner.lifecycleScope.launch {
                callback?.onLoadNextPage(pageId, pageSize)?.apply {
                    if (hasError) {
                        submitList(currentList.filterNot { it == getLoadingItem() }
                            .plus(getErrorItem()))
                    } else {
                        submitList(currentList.filterNot { it == getLoadingItem() }
                            .plus(items))
                    }
                    this@PagedAdapter.nextPageId = nextPageId
                    isLoading = false
                }
            }
        }
    }
}
