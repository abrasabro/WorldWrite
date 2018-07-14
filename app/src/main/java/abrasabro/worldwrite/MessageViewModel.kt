package abrasabro.worldwrite

import android.content.Context
import android.content.pm.PackageManager
import android.databinding.ObservableField
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.util.*

private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

class MessageViewModel : ObservableViewModel(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val mDefaultZoom = 20f
    private val mDefaultLocation = LatLng(0.0, 0.0)
    private lateinit var mWriteMarker: Marker
    private val mFusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MessageActivity.instance)
    private val geocoder = Geocoder(context(), Locale.getDefault())
    val mAddress: ObservableField<String> = ObservableField("")
    var mMessage: String = ""

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener { marker: Marker -> onMarkerClick(marker) }
        mMap.setOnMapClickListener { latLng: LatLng? -> onMapClick(latLng) }
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, mDefaultZoom))
        getLocationPermission()
    }

    private fun onMarkerClick(marker: Marker?): Boolean {
        return true
    }

    private fun onMapClick(latLng: LatLng?) {
        if (latLng != null)
            putMarker(latLng)
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(context(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLocationUI()
        } else {
            Log.d("getLocationPermission", "do not have location permission")
            ActivityCompat.requestPermissions(activity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String>,
                                   grantResults: IntArray) {
        Log.d("onRequestPermissionsRes", "start")
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                Log.d("onRequestPermissionsRes", "PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION")
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("onRequestPermissionsRes", "permission granted")
                    updateLocationUI()
                } else {
                    Log.d("onRequestPermissionsRes", "call getLocationPermission")
                    errorDialog("Soapstone needs location permissions to complete this action")
                }
            }
        }
    }

    private fun updateLocationUI() {
        try {
            mMap.isMyLocationEnabled = true
            getDeviceLocation()
        } catch (e: SecurityException) {
            Log.d("updateLocationUI", "lost permission after checking")
            errorDialog("Unexpected loss of permissions")
        }
    }

    private fun getDeviceLocation() {
        try {
            val locationResult = mFusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(activity()) { task: Task<Location> ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    if (task.result != null) {
                        val location: Location = task.result
                        val locLatLng = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                locLatLng, mDefaultZoom))
                        mMap.setLatLngBoundsForCameraTarget(LatLngBounds(locLatLng,
                                locLatLng))
                        mMap.uiSettings.isZoomControlsEnabled = false
                        mMap.uiSettings.isZoomGesturesEnabled = false
                        val marker = mMap.addMarker(MarkerOptions()
                                .position(locLatLng))
                        if (marker != null) {
                            mWriteMarker = marker
                            val bitmapFactoryOptions = BitmapFactory.Options()
                            bitmapFactoryOptions.inSampleSize = 6
                            val mapPin = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(context().resources, R.drawable.pin, bitmapFactoryOptions))
                            mWriteMarker.setIcon(mapPin)
                            putMarker(mWriteMarker.position)
                            activity().writeButtonEnabled(true)
                        } else {
                            Log.d("getDeviceLocation()", "couldn't add marker to map")
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, mDefaultZoom))
                            errorDialog("Couldn't add marker to map")
                        }
                    } else {
                        Log.d("getDeviceLocation()", "getLastLocation() was successful with no result")
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, mDefaultZoom))
                        errorDialog("getLastLocation() was successful with no result")
                    }
                } else {
                    Log.d("getDeviceLocation()", "getLastLocation() was unsuccessful")
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, mDefaultZoom))
                    errorDialog("getLastLocation() was unsuccessful")
                }
            }
        } catch (e: SecurityException) {
            Log.d("getDeviceLocation()", "lost permissions after checking")
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, mDefaultZoom))
            errorDialog("unexpected loss of permissions")
        }

    }

    private fun putMarker(latLng: LatLng) {
        mWriteMarker.position = latLng

        var addresses = listOf<Address>()
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        } catch (e: IOException) {
            Log.d("putMarker", "geocoder network or I/O exception")
            errorDialog("geocoder could not access the network")
        }

        if (addresses.isNotEmpty()) {
            mAddress.set("")
            if (addresses[0].getAddressLine(0) != null) {
                mAddress.set(addresses[0].getAddressLine(0))
                if (addresses[0].getAddressLine(1) != null) {
                    mAddress.set(mAddress.get() + addresses[0].getAddressLine(1))
                }
                return
            }
            if (addresses[0].featureName != null) {
                mAddress.set(addresses[0].featureName)
                return
            }
        }
        mAddress.set("unknown")
    }

    fun makeMessage() {
        val write = Write()
        write.set(mMessage,
                lat = mWriteMarker.position.latitude,
                lon = mWriteMarker.position.longitude,
                address = "near ${mAddress.get()}")
        activity().writeButtonEnabled(false)
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val messagesDatabaseReference = firebaseDatabase.reference.child("messages")
        val message = messagesDatabaseReference.push()
        write.messageUID = message.key
        message.setValue(write)
        analytics().logEvent(FirebaseAnalytics.Event.SHARE, null)
        activity().finish()
    }

    private fun errorDialog(msg: String = "Error") {
        Log.d("errorDialog", "dialog: $msg")
        analytics().logEvent("error_message", Bundle().apply {
            putString("message", msg)
        })
        val builder = AlertDialog.Builder(MessageActivity.instance)
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Retry") { _, _: Int ->
            activity().recreate()
        }
        builder.setNegativeButton("Cancel") { _, _: Int ->
            activity().finish()
        }
        builder.setOnCancelListener { errorDialog(msg) }
        builder.create().show()
    }

    private fun context(): Context {
        return MessageActivity.instance
    }

    private fun activity(): MessageActivity {
        return MessageActivity.instance
    }

    private fun analytics(): FirebaseAnalytics {
        return MessageActivity.mFirebaseAnalytics
    }
}