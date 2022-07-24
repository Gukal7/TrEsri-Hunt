package com.example.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.content.pm.PackageManager
import android.Manifest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.LocationDisplay

import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem


import com.example.testapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private fun requestPermissions(dataSourceStatusChangedEvent: LocationDisplay.DataSourceStatusChangedEvent) {
        val requestCode = 2
        val reqPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // fine location permission
        val permissionCheckFineLocation =
            ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[0]) ==
                    PackageManager.PERMISSION_GRANTED
        // coarse location permission
        val permissionCheckCoarseLocation =
            ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[1]) ==
                    PackageManager.PERMISSION_GRANTED
        if (!(permissionCheckFineLocation && permissionCheckCoarseLocation)) { // if permissions are not already granted, request permission from the user
            ActivityCompat.requestPermissions(this@MainActivity, reqPermissions, requestCode)
        } else {
            // report other unknown failure types to the user - for example, location services may not
            // be enabled on the device.
            val message = String.format(
                "Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
                    .source.locationDataSource.error.message
            )
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // if request is cancelled, the results array is empty
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationDisplay.startAsync()
        }
        else {
            Toast.makeText(
                this@MainActivity,
                resources.getString(R.string.location_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
    }

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }
    private val locationDisplay: LocationDisplay by lazy { mapView.locationDisplay }


    private fun setupMap() {
        val portalItemId = "0cfa02796e204976a4f80325cbcd8897"
        val portal = Portal("https://www.arcgis.com/", false)
        val portalItem = PortalItem(portal, portalItemId)

        val layerId = 0
        val layer = FeatureLayer(portalItem, layerId.toLong())
        mapView.map = ArcGISMap(portalItem)
        // set the viewpoint, Viewpoint(latitude, longitude, scale)
        mapView.setViewpoint(Viewpoint(34.056781, -117.194731, 720.0))
//        mapView.setViewpoint(Viewpoint(34.0270, -118.8050, 200000.0))

        portalItem.addDoneLoadingListener {
            if (portalItem.loadStatus != LoadStatus.LOADED) {
                val error = "Failed to load portal item ${portalItem.loadError.message}"
                Log.e(TAG, error)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                return@addDoneLoadingListener
            }
        }

        locationDisplay.addDataSourceStatusChangedListener {
            // if LocationDisplay isn't started or has an error
            if (!it.isStarted && it.error != null) {
                // check permissions to see if failure may be due to lack of permissions
                requestPermissions(it)
            }
        }

        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER)
        locationDisplay.startAsync()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        setApiKeyForApp()
        setupMap()
    }
    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }

    private fun setApiKeyForApp(){
        // set API key
        ArcGISRuntimeEnvironment.setApiKey("AAPK9cebc01cf0ba46f48cd1cb6688af7f66YUXosiE--0EUI08MFJAt2EZi4lNj-iQ7EziSbOlXuN_Lw2DkWJhfZlB6nRUu0GEW")

    }
}