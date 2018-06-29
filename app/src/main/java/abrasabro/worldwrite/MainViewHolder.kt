package abrasabro.worldwrite

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat

import android.view.View
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.*
import kotlinx.android.synthetic.main.bottomdrawer_main_nav.*
import kotlinx.android.synthetic.main.bottomdrawer_main_write.*

class MainViewHolder {
    companion object {
        lateinit var instance: MainViewHolder
        lateinit var presenter: MainPresenter
        lateinit var mainActivity: MainActivity
        var selectedWrite: Write? = null
    }

    fun onCreate(){
        instance = this
        presenter = MainPresenter()
        presenter.onCreate()
        mainActivity = MainActivity.instance
        context = mainActivity
        closeWrite()
    }

    fun onCreateView(){
        mainActivity.bottomdrawer_main_nav_new.setOnClickListener {
            presenter.startMessageActivity()
        }
        presenter.onCreateView()
    }

    fun getMapSupportFragment(): SupportMapFragment{
        return MainActivity.mapFragment
    }

    fun closeWrite(){
        selectedWrite = null
        mainActivity.bottomdrawer_main_write_layout.visibility = View.GONE
        mainActivity.bottomdrawer_main_nav_layout.visibility = View.VISIBLE
    }

    fun showWrite(write: Write, hasRatedGood: Boolean, hasRatedPoor: Boolean){
        selectedWrite = write
        mainActivity.bottomdrawer_main_nav_layout.visibility = View.GONE
        mainActivity.bottomdrawer_main_write_message.text = write.message
        mainActivity.bottomdrawer_main_write_address.text = write.address
        mainActivity.bottomdrawer_main_write_good.text = write.ratingGood.toString()
        mainActivity.bottomdrawer_main_write_poor.text = write.ratingPoor.toString()
        mainActivity.bottomdrawer_main_write_layout.visibility = View.VISIBLE
        if (hasRatedGood) {
            mainActivity.bottomdrawer_main_write_rategood.isEnabled = false
        } else {
            mainActivity.bottomdrawer_main_write_rategood.isEnabled = true
            mainActivity.bottomdrawer_main_write_rategood.setOnClickListener {
                mainActivity.bottomdrawer_main_write_rategood.isEnabled = false
                presenter.rateGood(write)
            }
        }
        if (hasRatedPoor) {
            mainActivity.bottomdrawer_main_write_ratepoor.isEnabled = false
        } else {
            mainActivity.bottomdrawer_main_write_ratepoor.isEnabled = true
            mainActivity.bottomdrawer_main_write_ratepoor.setOnClickListener {
                mainActivity.bottomdrawer_main_write_ratepoor.isEnabled = false
                presenter.ratePoor(write)
            }
        }
    }

    fun getContext(): Context{
        return mainActivity
    }
}