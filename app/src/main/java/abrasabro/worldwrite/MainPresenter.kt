package abrasabro.worldwrite

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.view.View
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.bottomdrawer_main_nav.*
import kotlinx.android.synthetic.main.bottomdrawer_main_write.*


class MainPresenter {

    companion object {
        lateinit var instance: MainPresenter
        lateinit var viewHolder: MainViewHolder
        lateinit var mMap: GoogleMap
    }

    fun onCreate(){
        instance = this
        viewHolder = MainViewHolder.instance
        val mapFragment = viewHolder.getMapSupportFragment()
        mapFragment.getMapAsync { googleMap: GoogleMap -> onMapReady(googleMap) }
    }

    fun onCreateView(){

        val bitmapFactoryOptions = BitmapFactory.Options()
        bitmapFactoryOptions.inSampleSize = 6
        mapPin = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.pin, bitmapFactoryOptions))
    }

    private fun closeWrite() {
        viewHolder.closeWrite()
    }

    private fun showWrite(write: Write) {
        var hasRatedGood: Boolean = false
        var hasRatedPoor: Boolean = false
        if (currentUser.goodRatings.contains(write.messageUID)) {
            hasRatedGood = true
        }
        if (currentUser.poorRatings.contains(write.messageUID)) {
            hasRatedPoor = true
        }
        viewHolder.showWrite(write, hasRatedGood, hasRatedPoor)
    }

    fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener({marker: Marker? ->  onMarkerClick(marker)})
        mMap.setOnMapClickListener({latLng: LatLng? ->  onMapClick(latLng)})
        val defaultMapCenter = LatLng(40.045204, -96.803178)
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(defaultMapCenter, 1f)))
    }

    fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            val write = writesHashMap[marker.id]
            if (write != null) {
                showWrite(write)
                return true
            }
        }
        return false
    }

    fun onMapClick(latLng: LatLng?) {
        closeWrite()
    }

    fun context(): Context{
        return viewHolder.getContext()
    }

    fun rateGood(write: Write){
        messagesDatabaseReference.child(write.messageUID).child("ratingGood").setValue(write.ratingGood + 1)
        currentUser.goodRatings.add(write.messageUID)
        usersDatabaseReference.setValue(currentUser)
    }

    fun ratePoor(write: Write){
        messagesDatabaseReference.child(write.messageUID).child("ratingPoor").setValue(write.ratingPoor + 1)
        currentUser.poorRatings.add(write.messageUID)
        usersDatabaseReference.setValue(currentUser)
    }

    fun startMessageActivity(){
        ContextCompat.startActivity(context(), Intent(context(), MessageActivity::class.java), null)
    }

    fun signOut(){
        AuthUI.getInstance().signOut(context())
    }
}