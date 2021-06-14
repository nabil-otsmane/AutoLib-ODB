package com.clovertech.autolibodb.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.clovertech.autolibodb.MainActivity
import com.clovertech.autolibodb.R
import com.clovertech.autolibodb.ui.SharedViewModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.lang.Exception
import java.net.URISyntaxException

class LoadingFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private lateinit var mSocket: Socket

    private val onConnected: Emitter.Listener = Emitter.Listener {
        activity?.runOnUiThread(Runnable {
            Toast.makeText(activity, "connected!", Toast.LENGTH_SHORT).show()
            activity?.findNavController(R.id.fragmentContainer)?.navigate(R.id.navigation_dashboard,
                null,
                navOptions { // Use the Kotlin DSL for building NavOptions
                    anim {
                        enter = android.R.animator.fade_in
                        exit = android.R.animator.fade_out
                    }
                })

        })
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel =
                ViewModelProvider(this).get(SharedViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_loading, container, false)

        mSocket = (activity as MainActivity).getSocket()

        mSocket.on("connected", onConnected)
        val obj = JSONObject()
        obj.put("id", 2)
        mSocket.emit("connected vehicule", obj)


        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.off("connected", onConnected)
    }
}