package abrasabro.worldwrite

import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.database.Observable
import android.databinding.ObservableField
import android.graphics.BitmapFactory
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v7.app.AlertDialog
import android.util.Log

import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainViewModel : ObservableViewModel() {

    companion object {
        lateinit var instance: MainViewModel
        var selectedWriteHasRatedGood = true
        var selectedWriteHasRatedPoor = true
        const val RC_SIGN_IN = 1
    }

    //var selectedWriteMessage: ObservableField<String> = ObservableField<String>()
    val selectedWriteMessage = ObservableField("binded")
    var selectedWrite: Write = Write()
    lateinit var mMap: GoogleMap
    lateinit var mapPin: BitmapDescriptor
    val mapMarkerToWriteHashMap = mutableMapOf<String, Write>()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val messagesDatabaseReference = firebaseDatabase.reference.child("messages")
    lateinit var usersDatabaseReference: DatabaseReference
    lateinit var currentUser: User
    lateinit var firebaseUser: FirebaseUser

    fun onCreate() {
        selectedWriteMessage.set("not set")
        instance = this
        val mapFragment = getMapSupportFragment()
        mapFragment.getMapAsync { googleMap: GoogleMap -> onMapReady(googleMap) }
    }

    fun onCreateView() {

    }

    private fun getMapSupportFragment(): SupportMapFragment {
        return activity().getMapSupportFragment()
    }

    private fun closeWrite() {
        //selectedWrite = null
        activity().closeWrite()
    }

    private fun showWrite(write: Write) {
        selectedWriteHasRatedGood = false
        selectedWriteHasRatedPoor = false
        if (currentUser.goodRatings.contains(write.messageUID)) {
            selectedWriteHasRatedGood = true
        }
        if (currentUser.poorRatings.contains(write.messageUID)) {
            selectedWriteHasRatedPoor = true
        }
        /*selectedWrite.message = write.message
        selectedWrite = write*/
        selectedWrite.set(write)
        //selectedWrite.notifyChange()
        activity().showWrite()
    }

    private fun context(): Context {
        return MainActivity.instance
    }

    private fun activity(): MainActivity {
        return MainActivity.instance
    }

    private fun onMapReady(googleMap: GoogleMap) {
        val bitmapFactoryOptions = BitmapFactory.Options()
        bitmapFactoryOptions.inSampleSize = 6
        mapPin = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(
                context().resources, R.drawable.pin, bitmapFactoryOptions))
        mMap = googleMap
        mMap.setOnMarkerClickListener { marker: Marker? -> onMarkerClick(marker) }
        mMap.setOnMapClickListener { latLng: LatLng? -> onMapClick(latLng) }
        val defaultMapCenter = LatLng(40.045204, -96.803178)
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(defaultMapCenter, 1f)))
        addFirebaseListeners()
    }

    private fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            val write = mapMarkerToWriteHashMap[marker.id]
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

    fun rateGood() {
        val write: Write? = selectedWrite
        if(write != null) {
            messagesDatabaseReference.child(write.messageUID).child("ratingGood").setValue(write.ratingGood + 1)
            currentUser.goodRatings.add(write.messageUID)
            usersDatabaseReference.setValue(currentUser)
            selectedWriteHasRatedGood = true
        }
    }

    fun ratePoor() {
        val write: Write? = selectedWrite
        if(write != null) {
            messagesDatabaseReference.child(write.messageUID).child("ratingPoor").setValue(write.ratingPoor + 1)
            currentUser.poorRatings.add(write.messageUID)
            usersDatabaseReference.setValue(currentUser)
            selectedWriteHasRatedPoor = true
        }
    }

    fun startMessageActivity() {
        //ContextCompat.startActivity(context(), Intent(context(), MessageActivity::class.java), null)
    }

    fun signOut() {
        AuthUI.getInstance().signOut(context())
    }


    //manages the data for each Write in the database
    private val messagesEventListener: ChildEventListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError?) {
            Log.d("messagesEventListener", "onCancelled, ${error?.details}")
            errorDialog("Error communicating with Firebase servers \n" + error?.details)
            //This method will be triggered in the event that this listener either failed at the server, or is removed as a result of the security and Firebase rules.
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?) {
            return
            //This method is triggered when a child location's priority changes. See setPriority(Object) and Ordered Data for more information on priorities and ordering data.
            //should never be called as all entries should be ordered by key and never change priority
        }

        //called on ratings change
        override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?) {
            val write = dataSnapshot?.getValue(Write::class.java)
            if (write != null) {
                if (write.messageUID == selectedWrite?.messageUID) {
                    selectedWrite.set(write)
                }
                mapMarkerToWriteHashMap.forEach { key: String, value: Write ->
                    if (value.messageUID == write.messageUID) {
                        mapMarkerToWriteHashMap[key] = write
                    }
                }
            }
        }

        //called for every message at the start and for any new messages added
        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?) {
            val write = dataSnapshot?.getValue(Write::class.java)
            if (write != null) {
                val marker = mMap.addMarker(MarkerOptions()
                        .position(LatLng(write.lat, write.lon))
                        .title(write.message)
                        .icon(mapPin))
                mapMarkerToWriteHashMap[marker.id] = write
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            return
            //only called when a message is removed while the app is running
        }

    }

    //manages user UID and ratings
    private val userEventListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError?) {
            Log.d("userEventListener", "onCancelled, ${error?.details}")
            errorDialog("Error communicating with Firebase servers \n" + error?.details)
        }

        //called immediately with the contents of the current user and every time the user's data changes (after rating)
        override fun onDataChange(snapshot: DataSnapshot?) {
            val user = snapshot?.getValue(User::class.java)
            if (user != null) {
                currentUser = user
            } else {//first time user
                usersDatabaseReference.setValue(User(firebaseUser.uid))
            }
        }

    }

    //called when the user signs in or out
    private val authStateListener = { auth: FirebaseAuth ->
        val user = auth.currentUser
        if (user != null) {
            onSignedInInitialize(user)
        } else {
            onSignedOutCleanup()
            startActivityForResult(
                    activity(),
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(mutableListOf(
                                    AuthUI.IdpConfig.EmailBuilder().build(),
                                    AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RC_SIGN_IN,
                    null)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                //signed in
            } else if (resultCode == Activity.RESULT_CANCELED) {
                activity().finishActivity()
            }
        }
    }

    fun onPause() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        messagesDatabaseReference.removeEventListener(messagesEventListener)
    }

    fun onResume() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    private fun onSignedInInitialize(user: FirebaseUser) {
        firebaseUser = user
        addFirebaseListeners()
    }

    private fun addFirebaseListeners(){
        var googleMapReady = true
        var firebaseReady = true
        //verify google map has been loaded asynchronously
        try {
            mMap.isMyLocationEnabled
        } catch (e: UninitializedPropertyAccessException) {
            Log.d("addFirebaseListeners", "googlemap not ready before firebase")
            //errorDialog("Error creating Google Map")
            googleMapReady = false
        }
        try {
            firebaseUser.isAnonymous
        } catch (e: UninitializedPropertyAccessException) {
            Log.d("addFirebaseListeners", "firebase not ready before googlemap")
            firebaseReady = false
        }
        if(googleMapReady && firebaseReady) {
            messagesDatabaseReference.addChildEventListener(messagesEventListener)
            usersDatabaseReference = firebaseDatabase.reference.child("users").child(firebaseUser.uid)
            usersDatabaseReference.addValueEventListener(userEventListener)
        }
    }

    private fun onSignedOutCleanup() {
        messagesDatabaseReference.removeEventListener(messagesEventListener)
    }

    private fun errorDialog(msg: String = "Error") {
        Log.d("errorDialog", "dialog: $msg")
        val builder = AlertDialog.Builder(context())
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Retry")
        { _, _: Int ->
            activity().recreateActivity()
        }
        builder.setNegativeButton("Quit")
        { _, _: Int ->
            activity().finishActivity()
        }
        builder.setOnCancelListener { errorDialog(msg) }
        builder.create().show()
    }
}