package com.clovertech.autolibodb

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.clovertech.autolibodb.ui.SharedViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.lang.Exception
import java.net.URISyntaxException
import java.io.IOException
import java.util.*

val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

class MainActivity : AppCompatActivity() {


    private lateinit var viewModel: SharedViewModel

    private lateinit var mSocket: Socket

    private var nameTablet: String? = null

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

        val data: JSONObject = it[0] as JSONObject
        nameTablet = data.getString("nomLocataire")
        this@MainActivity.runOnUiThread(Runnable {
            Toast.makeText(this@MainActivity, "Discovering ...", Toast.LENGTH_SHORT).show()
        })
        val discovering = bluetoothAdapter?.startDiscovery()
    }

    private val onDisconnect: Emitter.Listener = Emitter.Listener {
        this@MainActivity.runOnUiThread(Runnable {
            findNavController(R.id.fragmentContainer).navigate(R.id.navigation_loading)
            Toast.makeText(this@MainActivity, "Diconnected!", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton(
                                "OK"
                        ) { _, _ ->
                            //Prompt the user once explanation has been shown
                            requestLocationPermission()
                        }
                        .create()
                        .show()
            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        }

        val requestCode = 1
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivityForResult(discoverableIntent, requestCode)

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 100)
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(receiver, filter)

        viewModel =
            ViewModelProvider(this).get(SharedViewModel::class.java)


        try {
            val opts = IO.Options()
            opts.port = 8000
            opts.path = "/socket"


            mSocket = IO.socket("http://192.168.137.93:8123", opts)
        } catch(e: URISyntaxException) {
            e.printStackTrace()
        }

        mSocket.on("error", onError)
        mSocket.on("connect_error", onError)
        mSocket.on("start link", onLink)
        mSocket.on("disconnect", onDisconnect)
        mSocket.connect()

    }
//
//    private val onConnected: Emitter.Listener = Emitter.Listener {
//        val obj = JSONObject()
//        obj.put("id", 2)
//        mSocket.emit("connected vehicule", obj)
//    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    100
            )
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    100
            )
        }
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action!!) {

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Toast.makeText(this@MainActivity, "Starting discovery ...", Toast.LENGTH_SHORT).show()

                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Toast.makeText(this@MainActivity, "Finishing discovery ...", Toast.LENGTH_SHORT).show()

                }

                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address

                    if (deviceName.equals(nameTablet)) {
                        val connectThread = ConnectThread(device!!,this@MainActivity)
                        connectThread.start()
                    }
                }

            }
        }
    }


    fun getSocket(): Socket {
        return mSocket
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
        mSocket.disconnect()
        mSocket.off("error", onError)
        mSocket.off("connect_error", onError)
        mSocket.off("start link", onLink)
    }
}


private class ConnectThread(val device: BluetoothDevice, val activity: AppCompatActivity) : Thread() {

    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(UUID(100, 200))
    }

    override fun run() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter?.cancelDiscovery()

        mmSocket?.let { socket ->
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            socket.connect()

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            var output = ByteArray(1)
            output[0] = 10.toByte()
            socket.outputStream.write(output)

            activity.runOnUiThread { Toast.makeText(activity, "Connexion effectuée avec ${socket.remoteDevice.name}", Toast.LENGTH_LONG).show() }
            try {

                // mmSocket?.close()
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Could not close the client socket", e)
            }
        }
    }

}