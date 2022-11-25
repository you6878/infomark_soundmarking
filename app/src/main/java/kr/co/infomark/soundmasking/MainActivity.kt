package kr.co.infomark.soundmasking

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import kotlinx.coroutines.Delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.ActivityMainBinding
import kr.co.infomark.soundmasking.intro.StartSpeakerSettingActivity
import kr.co.infomark.soundmasking.model.*
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject
import java.lang.reflect.Method


class MainActivity : AppCompatActivity() {
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
        initView()
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
                checkWifiState()
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
        binding.logBtn.setOnClickListener {
            startActivity(Intent(this,LogActivity::class.java))
        }
        binding.startSpeakerSettingBtn.setOnClickListener {
            Toast.makeText(this,"장비 초기화를 시작합니다.",Toast.LENGTH_LONG).show()
            var commandModel = RemoveCommandModel(WlanRemoveNetwork,Util.getSharedPreferenceString(this@MainActivity,Util.WIFI_NAME))
            bt.send(gson.toJson(commandModel));
            Handler(Looper.getMainLooper()).postDelayed({
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                bt.stopService()
                bluetoothManager.releaseMediaPlayer()
                bluetoothManager.disConnectUsingBluetoothA2dp(selectDeivice)
                removeBond(selectDeivice)
                Util.clearSharedPreference(this@MainActivity)
                finish()
            },1000)
        }
    }

    fun setBTListener(){
        bt.setOnDataReceivedListener { data, message ->
            println(message)
            var isLog = JSONObject(message).isNull("log")
            if(!isLog){
                return@setOnDataReceivedListener
            }
            var cmd = JSONObject(message).getString("cmd")
            if(cmd == WlanState){
                var model = gson.fromJson(message, DefaultModel::class.java)
                if(model.result == "ok"){
                    binding.wifiStatusTextview.text = model.state
                }
            }
        }

    }
    fun removeBond(device: BluetoothDevice) {
        try {
            device::class.java.getMethod("removeBond").invoke(device)
        } catch (e: Exception) {

        }
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
            deviceCallback()
        }
        checkWifiState()
        setBTListener()
    }
    fun checkWifiState(){
        GlobalScope.launch {
            delay(2000)
            var commandModel = CommandModel(WlanState)
            bt.send(gson.toJson(commandModel))
        }.start()

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
