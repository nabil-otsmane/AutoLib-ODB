package com.clovertech.autolibodb

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.clovertech.autolibodb.ui.SharedViewModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.lang.Exception
import java.net.URISyntaxException

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SharedViewModel

    private lateinit var mSocket: Socket

    private val onError: Emitter.Listener = Emitter.Listener {
        this@MainActivity.runOnUiThread(Runnable {
            try {
                val data: Exception = it[0] as Exception
                Toast.makeText(this@MainActivity, data.message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                val data: JSONObject = it[0] as JSONObject

            }

        })
    }
    private val onLink: Emitter.Listener = Emitter.Listener {
        this@MainActivity.runOnUiThread(Runnable {
            val data: JSONObject = it[0] as JSONObject

            println("linking ...")
        })
    }

    private val onDisconnect: Emitter.Listener = Emitter.Listener {
        this@MainActivity.runOnUiThread(Runnable {
            findNavController(R.id.fragmentContainer).navigate(R.id.navigation_loading)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel =
            ViewModelProvider(this).get(SharedViewModel::class.java)


        try {
            val opts = IO.Options()
            opts.port = 8000
            opts.path = "/socket"


            mSocket = IO.socket("http://192.168.43.222:8123", opts)
        } catch(e: URISyntaxException) {
            e.printStackTrace()
        }

        mSocket.on("error", onError)
        mSocket.on("connect_error", onError)
        mSocket.on("start link", onLink)
        mSocket.on("disconnect", onDisconnect)
        mSocket.connect()

    }

    fun getSocket(): Socket {
        return mSocket
    }

    override fun onDestroy() {
        super.onDestroy()

        mSocket.disconnect()
        mSocket.off("error", onError)
        mSocket.off("connect_error", onError)
        mSocket.off("start link", onLink)
    }
}