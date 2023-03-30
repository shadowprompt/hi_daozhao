package com.daozhao.hi.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData as MutableLiveData1

class UrlViewModel : ViewModel() {
    private val mutableSelectedItem = MutableLiveData1<String>("https://www.daozhao.com")
    val activeUrl: androidx.lifecycle.MutableLiveData<String> get() = mutableSelectedItem

    fun selectItem(item: String) {
        activeUrl.value = item
    }
}