package abrasabro.worldwrite

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomdrawer_main_nav.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var instance: MainActivity
        lateinit var viewHolder: MainViewHolder
        lateinit var mapFragment: SupportMapFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.activity_main)
        viewHolder = MainViewHolder()
        viewHolder.onCreate()
        mapFragment = map as SupportMapFragment
    }

    override fun onCreateView(name: String?, context: Context?, attrs: AttributeSet?): View {
        val view = super.onCreateView(name, context, attrs)
        viewHolder.onCreateView()
        return view
    }
}
