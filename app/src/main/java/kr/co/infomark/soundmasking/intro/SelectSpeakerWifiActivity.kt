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
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.ActivitySelectSpeakerWifiBinding
import kr.co.infomark.soundmasking.intro.adapter.WifiListAdapter
import kr.co.infomark.soundmasking.model.CommandModel
import kr.co.infomark.soundmasking.model.WlanNetworkListModel
import kr.co.infomark.soundmasking.model.WlanScanResult
import kr.co.infomark.soundmasking.popup.CanUseSpeakerDialogFragment
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject

class SelectSpeakerWifiActivity : AppCompatActivity() {
    lateinit var binding : ActivitySelectSpeakerWifiBinding
    private lateinit var wifiListAdapter: WifiListAdapter
    private lateinit var bt: BluetoothSPP
    lateinit var gson : Gson
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_select_speaker_wifi)
        gson = Gson()
        bt =  BluetoothSPP.getInstance(this); //Initializing
        binding.wifiRefreshButton.setOnClickListener {
            scanWifiList()
        }

        setRecyclerview()

    }
    private fun setRecyclerview() {
        wifiListAdapter = WifiListAdapter(::nextPage)
        binding.wifiRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.wifiRecyclerview.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        binding.wifiRecyclerview.apply {
            adapter = wifiListAdapter
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val i = Intent(this, SpeakerConnectCompleteActivity::class.java)
            i.putExtra("fromMain",intent.getBooleanExtra("fromMain",false))
            intent.getStringExtra("SSID")?.let {
                Util.putSharedPreferenceString(this@SelectSpeakerWifiActivity,Util.WIFI_NAME, it)
            }
            startActivity(i)
            finish()
        }
    }
    fun nextPage(id : String){
        val i = Intent(this,InputWifiPasswordActivity::class.java)
        i.putExtra("SSID",id)
        intent.putExtra("SSID",id)
        resultLauncher.launch(i)



    }
    override fun onResume() {
        super.onResume()
        if (!bt.isBluetoothEnabled) {
            bt.enable()
        } else {
            if (!bt.isServiceAvailable) {
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)

                if(bt.connectedDeviceAddress == null){
                    val mac = Util.MAC
                    bt.connect(Util.getSharedPreferenceString(this,mac))
                }
            }
        }
        if(wifiListAdapter.scanResult.data.size == 0){
            binding.progressCir.visibility = View.VISIBLE
        }
        scanWifiList()
        setBTListener()


    }
    fun setBTListener(){
        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
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
        bt.setOnDataReceivedListener { data, message ->
            var isLog = JSONObject(message).isNull("log")
            if(!isLog){
                return@setOnDataReceivedListener
            }
            var cmd = JSONObject(message).getString("cmd")
            if(cmd == WlanScanResult){
                binding.progressCir.visibility = View.GONE
                var res = gson.fromJson(message, WlanNetworkListModel::class.java)
                if(res.result == "ok"){
                    wifiListAdapter.addItems(res)
                }else{
                    println(res.toString())
                }
            }
        }
    }
    fun  scanWifiList(){
        var commandModel = CommandModel(WlanScanResult)
        bt.send(gson.toJson(commandModel))
    }
}