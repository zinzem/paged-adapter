package com.zinzem.pagedadapter

import java.lang.Exception

data class Page<ITEM>(
    val items: List<ITEM>,
    val nextPageId: String?,
    val error: Exception?
)