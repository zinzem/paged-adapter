# PagedAdapter
[![](https://jitpack.io/v/zinzem/paged-adapter.svg)](https://jitpack.io/#zinzem/paged-adapter)

RecyclerView Adapter for paged data and infinite scroll

## Install
This library is hosted on [JitPack](https://jitpack.io/). Make sure to add it to the repositories.
```
allprojects {
  repositories {
    ...
    maven { url "https://jitpack.io" }
  }
}
```
Then just add this to your dependencies
```
implementation 'com.github.zinzem:paged-adapter:1.0.0'
```

## Usage
#### 1. Create a `BaseListItem` sealed class
It that will define all the different types the list can hold. There are a leat 3:
- Data item
- Loading item
- Error item
```
sealed class BaseListItem {
    data class DataItem(val text: String) : BaseListItem()
    object LoadingItem : BaseListItem()
    object ErrorItem : BaseListItem()
}
```

#### 2. Create the adapter extending `PagedAdapter`
```
class Adapter(
    lifecycleOwner: LifecycleOwner
) : PagedAdapter<BaseListItem, ViewHolder>(
    TypedDiffUtil(), lifecycleOwner
) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is BaseListItem.Item -> R.layout.item_data
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
        ...
    }

    override fun getLoadingItem() = BaseListItem.LoadingItem
    override fun getErrorItem() = BaseListItem.ErrorItem
}
```
Note that we are using `TypedDiffUtil` which is provided by the library. You can also provide your own implementation of `DiffUtil.ItemCallback`.

#### 3. Use the adapter in the view
```
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModel
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        findViewById<RecyclerView>(R.id.rv).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = Adapter(this@MainActivity)
                .setCallback(object : PagedAdapterCallback<BaseListItem> {
                    override suspend fun onLoadNextPage(nextPageId: String?, pageSize: Int): Page<BaseListItem> {
                        val response = viewModel.getPage(nextPageId, pageSize)
                        return Page(
                            items = response.items.map { BaseListItem.Item(it) },
                            nextPageId = response.nextPageId,
                            error = null
                        )
                    }
                })
                .setPageSize(5)
        }

        adapter.loadFirstPage()
        ...
    }
}
```
The `PagedAdapter` has two setters:
- `setCallback(callback: PagedAdapterCallback)`: be notified when the adapter needs to load more data. `onLoadNextPage` is a supend function, that is why the `PagedAdapter` needs a `LifecycleOwner`, to be able to run and cancel the coroutines. You need to return a `Page` in the callback. 
  - `Page.items` will be added to the list. 
  - When `Page.nextPageId` is null, the adapter will stop trying to load more data. 
  - When `Page.error` is not null, the adapter will stop trying to load more data, and display the error item at the bottom.
- `setPageSize(pageSize: Int)`: set the number of items per page, default is `8`. Make sure that one page of items is bigger than screen height.
