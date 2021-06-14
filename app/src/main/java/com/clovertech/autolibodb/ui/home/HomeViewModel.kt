package com.clovertech.autolibodb.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _id = MutableLiveData<Int>().apply {
        value = 1
    }
    var id: LiveData<Int> = _id;

    fun update(valu: Int) {
        _id.value = valu
    }
}