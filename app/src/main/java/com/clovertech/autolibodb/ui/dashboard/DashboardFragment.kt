package com.clovertech.autolibodb.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.clovertech.autolibodb.R
import com.clovertech.autolibodb.ui.SharedViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private lateinit var dashboardViewModel: SharedViewModel
    var map: GoogleMap? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(SharedViewModel::class.java)
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        val latLng = LatLng(36.704998, 3.173918)
        map!!.addMarker(MarkerOptions().position(latLng).title("Your position"))

        val zoomLevel = 16.0f //This goes up to 21

        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }
}