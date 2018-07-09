package abrasabro.worldwrite

import abrasabro.worldwrite.databinding.ActivityMessageBinding
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_message.*


class MessageActivity: AppCompatActivity() {

    companion object {
        lateinit var instance: MessageActivity
        lateinit var viewModel: MessageViewModel
    }

    lateinit var binding: ActivityMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        viewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message)
        binding.address = viewModel.mAddress.get()
        binding.message = viewModel.mMessage
        val mapFragment = message_map as SupportMapFragment
        mapFragment.getMapAsync {  }
        message_write.isEnabled = false
    }

    fun writeButtonEnabled(isEnabled: Boolean){
        message_write.isEnabled = isEnabled
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun onWriteClicked(view: View){
        viewModel.makeMessage()
    }
}