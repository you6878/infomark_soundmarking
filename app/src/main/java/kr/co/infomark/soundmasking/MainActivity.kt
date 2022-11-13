package kr.co.infomark.soundmasking

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.ActivityMainBinding
import kr.co.infomark.soundmasking.model.*
import kr.co.infomark.soundmasking.util.Util


class MainActivity : AppCompatActivity() {
    private var currentCommand = ""
    lateinit var binding : ActivityMainBinding
    lateinit var gson: Gson
    var bluetoothManager = BluetoothManager(this)
    lateinit var bt: BluetoothSPP
    lateinit var selectDeivice: BluetoothDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        gson = Gson()
        bt =  BluetoothSPP.getInstance(this); //Initializing
        bt.setOnDataReceivedListener { data, message ->
            if(currentCommand == WlanState){
                var model = gson.fromJson(message, DefaultModel::class.java)
                println(model)
                if(model.result == "ok"){
                    binding.wifiStatusTextview.text = model.state
                }
            }

        }


        initView()
        deviceCallback()
    }
    fun deviceCallback(){

        //Bluetooth Status
        val mac = Util.getSharedPreferenceString(this,Util.MAC)
        selectDeivice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac)
        bluetoothManager.enableBluetooth()
        bluetoothManager.connectUsingBluetoothA2dpCallback(selectDeivice) {
            if(it){
                binding.speakerStatusTextview.text = "Connected"
            }
        }
        //Wifi Status
        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                var commandModel = CommandModel(WlanState)
                currentCommand = WlanState
                bt.send(gson.toJson(commandModel))
            }

            override fun onDeviceDisconnected() {
                Toast.makeText(
                    applicationContext, "Connection lost", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDeviceConnectionFailed() {
                Toast.makeText(
                    applicationContext, "Unable to connect", Toast.LENGTH_SHORT
                ).show()
            }
        })


    }


    fun initView(){
        binding.playMusic.setOnClickListener {
            bluetoothManager.playMusic()
        }
//        binding.debugBtn.setOnClickListener {
//            Util.putSharedClear(this)
//        }
//        binding.startSpeakerSettingBtn.setOnClickListener {
//            val i = Intent(this, StartSpeakerSettingActivity::class.java)
//            i.putExtra("fromMain",true)
//            startActivity(i)
//        }
    }

    override fun onResume() {
        super.onResume()
        binding.ssidTextview.text = Util.getSharedPreferenceString(this,Util.MAC)
        binding.wifiNameTextview.text = Util.getSharedPreferenceString(this,Util.WIFI_NAME)
        if (!bt.isBluetoothEnabled) {
            bt.enable()
        } else {
            if (!bt.isServiceAvailable) {
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)

                //Auto Connect in exceoption
                if(bt.connectedDeviceAddress == null){
                    val mac = Util.MAC
                    bt.connect(Util.getSharedPreferenceString(this,mac))
                }
            }
        }
    }

    override fun onDestroy() {
        bt.stopService()
        bluetoothManager.releaseMediaPlayer()
        bluetoothManager.disConnectUsingBluetoothA2dp(selectDeivice)
        super.onDestroy()
    }

    override fun onPause() {
        bluetoothManager.releaseMediaPlayer()
        super.onPause()
    }
}
