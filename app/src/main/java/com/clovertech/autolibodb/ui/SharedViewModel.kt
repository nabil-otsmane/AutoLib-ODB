package com.clovertech.autolibodb.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.socket.client.Socket

class SharedViewModel: ViewModel() {

    private val socket = MutableLiveData<Socket>()

    fun getSocket(): LiveData<Socket> {
        return socket
    }

    fun setSocket(mSocket: Socket) {
        socket.value = mSocket
    }
}