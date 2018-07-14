package abrasabro.worldwrite

//import abrasabro.worldwrite.databinding.ActivityMainBinding
import abrasabro.worldwrite.databinding.ActivityMainBinding
import abrasabro.worldwrite.databinding.BottomdrawerMainWriteBinding
import android.arch.lifecycle.ViewModelProviders
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
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.bottomdrawer_main_write.view.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var instance: MainActivity
        lateinit var mapFragment: SupportMapFragment
        lateinit var mFirebaseAnalytics: FirebaseAnalytics
    }
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var writeBinding: BottomdrawerMainWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        val fabric = Fabric.Builder(this).kits(Crashlytics()).debuggable(true).build()
        Fabric.with(fabric)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val writeBind: BottomdrawerMainWriteBinding? = DataBindingUtil.bind<BottomdrawerMainWriteBinding>(binding.root.include_bottomdrawer_main_write)
        if(writeBind != null)
            writeBinding = writeBind
        viewModel.onCreate()
        binding.viewmodel = viewModel
        writeBinding.viewmodel = viewModel
        writeBinding.selectedwrite = viewModel.selectedWrite
        closeWrite()
    }

    fun closeWrite() {
        binding.root.include_bottomdrawer_main_write.visibility = View.GONE
        bottomdrawer_main_nav_layout.visibility = View.VISIBLE
    }

    fun showWrite() {
        bottomdrawer_main_nav_layout.visibility = View.GONE
        binding.root.include_bottomdrawer_main_write.visibility = View.VISIBLE
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

    fun getMapSupportFragment(): SupportMapFragment {
        mapFragment = main_mapfragment as SupportMapFragment
        return mapFragment
    }

    fun startMessageActivity(view: View) {
        mFirebaseAnalytics.logEvent("attempt_share", null)
        ContextCompat.startActivity(this, Intent(this, MessageActivity::class.java), null)
    }

}
