package com.zinzem.sampleapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zinzem.pagedadapter.Page
import com.zinzem.pagedadapter.PagedAdapterCallback
import kotlinx.coroutines.delay

private val items = (1..50).toList()

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<RecyclerView>(R.id.rv).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = Adapter(this@MainActivity)
                .setCallback(object : PagedAdapterCallback<BaseListItem> {
                    override suspend fun onLoadNextPage(nextPageId: String?, pageSize: Int): Page<BaseListItem> {
                        val pageNumber = nextPageId?.toInt() ?: 0
                        delay(1000)
                        return Page(
                            items.subList(pageNumber, pageNumber+pageSize).map { BaseListItem.Item(it.toString()) },
                            if (pageNumber+pageSize*2 > items.size) null else (pageNumber+pageSize).toString(),
                            null
                        )
                    }
                })
                .setPageSize(5)
        }

        adapter.loadFirstPage()
    }
}