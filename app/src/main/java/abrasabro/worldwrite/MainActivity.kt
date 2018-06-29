package abrasabro.worldwrite

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomdrawer_main_nav.*
import kotlinx.android.synthetic.main.bottomdrawer_main_write.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var instance: MainActivity
        lateinit var viewModel: MainViewModel
        lateinit var mapFragment: SupportMapFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        viewModel = MainViewModel()
        setContentView(R.layout.activity_main)
        viewModel.onCreate()
        mapFragment = main_mapfragment as SupportMapFragment
        closeWrite()
    }

    override fun onCreateView(name: String?, context: Context?, attrs: AttributeSet?): View {
        val view = super.onCreateView(name, context, attrs)
        viewModel.onCreateView()
        //val binding
        return view
    }

    fun closeWrite() {
        bottomdrawer_main_write_layout.visibility = View.GONE
        bottomdrawer_main_nav_layout.visibility = View.VISIBLE
    }

    fun showWrite() {
        bottomdrawer_main_nav_layout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        closeWrite()
        viewModel.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    fun recreateActivity(){
        recreate()
    }

    fun finishActivity(){
        finish()
    }

    fun rateGood(view: View){
        viewModel.rateGood()
    }

    fun ratePoor(view: View){
        viewModel.ratePoor()
    }
}
