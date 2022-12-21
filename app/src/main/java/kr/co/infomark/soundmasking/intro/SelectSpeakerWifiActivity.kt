package kr.co.infomark.soundmasking.intro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.ActivitySelectSpeakerWifiBinding
import kr.co.infomark.soundmasking.intro.adapter.WifiListAdapter
import kr.co.infomark.soundmasking.model.CommandModel
import kr.co.infomark.soundmasking.model.LogModel
import kr.co.infomark.soundmasking.model.WlanNetworkListModel
import kr.co.infomark.soundmasking.model.WlanScanResult
import kr.co.infomark.soundmasking.popup.CanUseSpeakerDialogFragment
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject

class SelectSpeakerWifiActivity : AppCompatActivity() {
    var binding : ActivitySelectSpeakerWifiBinding? = null
    private lateinit var wifiListAdapter: WifiListAdapter
    var bt: BluetoothSPP? = null
    lateinit var gson : Gson
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_select_speaker_wifi)
        gson = Gson()
        bt =  BluetoothSPP.getInstance(this); //Initializing
        binding?.wifiRefreshButton?.setOnClickListener {
            scanWifiList()
        }
        binding?.wifiCancleButton?.setOnClickListener {
            var Empty = "설정 대기 중"

            Util.putSharedPreferenceString(this@SelectSpeakerWifiActivity,Util.WIFI_NAME, Empty)

            if(intent.getBooleanExtra("RESET",false)){
                finish()
            }else{
                val i = Intent(this, SpeakerConnectCompleteActivity::class.java)
                i.putExtra("SSID",Empty)
                i.putExtra("BSSID","")
                startActivity(i)
                finish()
            }

        }

        setRecyclerview()

    }
    private fun setRecyclerview() {
        wifiListAdapter = WifiListAdapter(::nextPage)
        binding?.wifiRecyclerview?.layoutManager = LinearLayoutManager(this)
        binding?.wifiRecyclerview?.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        binding?.wifiRecyclerview?.apply { adapter = wifiListAdapter }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes

            intent.getStringExtra("SSID")?.let {
                Util.putSharedPreferenceString(this@SelectSpeakerWifiActivity,Util.WIFI_NAME, it)
            }
            intent.getStringExtra("BSSID")?.let {
                Util.putSharedPreferenceString(this@SelectSpeakerWifiActivity,Util.BSSID_NAME, it)
            }

            if(intent.getBooleanExtra("RESET",false)){
                finish()
            }else{
                val i = Intent(this, SpeakerConnectCompleteActivity::class.java)
                startActivity(i)
                finish()
            }


        }
    }
    fun nextPage(ssid : String,bssid : String){
        val i = Intent(this,InputWifiPasswordActivity::class.java)
        i.putExtra("SSID",ssid)
        intent.putExtra("SSID",ssid)
        intent.putExtra("BSSID",bssid)
        resultLauncher.launch(i)



    }
    override fun onResume() {
        super.onResume()
        if (bt?.isBluetoothEnabled == false) {
            bt?.enable()
        } else {
            if (bt?.isServiceAvailable == false) {
                bt?.setupService()
                bt?.startService(BluetoothState.DEVICE_OTHER)

                if(bt?.connectedDeviceAddress == null){
                    val mac = Util.MAC
                    bt?.connect(Util.getSharedPreferenceString(this,mac))
                }
            }
        }
        if(wifiListAdapter.scanResult.data.size == 0){
            binding?.progressCir?.visibility = View.VISIBLE
        }
        scanWifiList()
        setBTListener()


    }
    fun setBTListener(){
        bt?.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {

                scanWifiList()
            }

            override fun onDeviceDisconnected() {
                Toast.makeText(
                    applicationContext, "SPP 연결 해제", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDeviceConnectionFailed() {
                Toast.makeText(
                    applicationContext, "SPP 연결 실패", Toast.LENGTH_SHORT
                ).show()
            }
        })
        bt?.setOnDataReceivedListener { data, message ->
            var isLog = JSONObject(message).isNull("log")
            if(!isLog){
                val item = gson.fromJson(message, LogModel::class.java)
                saveLog(item)
                return@setOnDataReceivedListener
            }
            var cmd = JSONObject(message).getString("cmd")
            if(cmd == WlanScanResult){
                println(message)
                binding?.progressCir?.visibility = View.GONE
                var res = gson.fromJson(message, WlanNetworkListModel::class.java)
                if(res.result == "ok"){
                    wifiListAdapter.addItems(res)
                }else{
                    println(res.toString())
                }
            }
        }
    }
    fun saveLog(item : LogModel){
        var logs = Util.getSharedPreferenceString(this, Util.LOGS)
        var list : MutableList<LogModel> = mutableListOf()
        if(!logs.isEmpty()){
            list = gson.fromJson(logs,Array<LogModel>::class.java).asList().toMutableList()
        }
        list.add(item)
        Util.putSharedPreferenceString(this, Util.LOGS,gson.toJson(list))
    }
    fun  scanWifiList(){
        var commandModel = CommandModel(WlanScanResult)
        bt?.send(gson.toJson(commandModel))
    }
}