package com.zinzem.sampleapp

sealed class BaseListItem {
    data class Item(val text: String) : BaseListItem()
    object LoadingItem : BaseListItem()
    object ErrorItem : BaseListItem()
}