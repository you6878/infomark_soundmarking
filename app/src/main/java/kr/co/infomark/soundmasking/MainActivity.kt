package kr.co.infomark.soundmasking

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.ActivityMainBinding
import kr.co.infomark.soundmasking.intro.StartSpeakerSettingActivity
import kr.co.infomark.soundmasking.intro.adapter.PairedDevicesAdapter
import kr.co.infomark.soundmasking.model.*
import kr.co.infomark.soundmasking.util.Util
import java.nio.charset.StandardCharsets



class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    lateinit var deviceModel: DeviceModel
    lateinit var gson: Gson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        gson = Gson()


        initView()
    }
    fun initView(){

        binding.startSpeakerSettingBtn.setOnClickListener {
            val i = Intent(this, StartSpeakerSettingActivity::class.java)
            i.putExtra("fromMain",true)
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        deviceModel = gson.fromJson(Util.getSharedPreferenceString(this,Util.DEVICE),DeviceModel::class.java)
        binding.ssidTextview.text = deviceModel.ssid
        binding.wifiNameTextview.text = deviceModel.wifi_name
    }
}
