package kr.co.infomark.soundmasking.intro

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.ActivityStartSpeakerSettingBinding
import kr.co.infomark.soundmasking.intro.adapter.PairedDevicesAdapter
import kr.co.infomark.soundmasking.model.*
import kr.co.infomark.soundmasking.popup.CanUseSpeakerDialogFragment
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject


class StartSpeakerSettingActivity : AppCompatActivity() {
    companion object {
        const val MESSAGE_READ = 2 // used in bluetooth handler to identify message update
        const val CONNECTING_STATUS = 3 // used in bluetooth handler to identify message status

    }

    lateinit var bt: BluetoothSPP
    lateinit var gson: Gson
    lateinit var binding: ActivityStartSpeakerSettingBinding
    lateinit var mHandler: Handler
    lateinit var devicesAdapter: PairedDevicesAdapter
    var bluetoothManager = BluetoothManager(this)
    lateinit var mBTAdapter: BluetoothAdapter
    var selectDevice: BluetoothDevice? = null
    var threadState = true

    var moveMainPage = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_start_speaker_setting
        )
        gson = Gson()
        bt = BluetoothSPP.getInstance(this)
        mBTAdapter = BluetoothAdapter.getDefaultAdapter() // get a handle on the bluetooth radio
        devicesAdapter = PairedDevicesAdapter(::connectDevice)
        setRecyclerview()
        setButton()

        if (!bt.isBluetoothAvailable) { //블루투스 사용 불가라면
            // 사용불가라고 토스트 띄워줌
            Toast.makeText(
                this, "Bluetooth is not available", Toast.LENGTH_SHORT
            ).show();
            // 화면 종료
            finish();
        }

        val searchFilter = IntentFilter()
//        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
//        searchFilter.addAction(BluetoothDevice.ACTION_FOUND) //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
//        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료

        searchFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        setBTListener()

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

                //이미 연결된 상태
                if(model.result == "ok" && model.state == "connected"){
                    var command = CommandModel(WlanNetworkList)
                    bt.send(gson.toJson(command))
                }else if(model.result == "ok" && model.state == "enabled"){
                    binding.progressCir.visibility = View.GONE
                    val dialog = CanUseSpeakerDialogFragment()
                    dialog.show(supportFragmentManager, "CanUseSpeakerDialogFragment")
                }else if(model.result == "ok" && model.state == "disabled"){
                    Toast.makeText(this, "프렌즈 스피커의 WIFI가 꺼져있어 설정할 수 없습니다.",Toast.LENGTH_LONG).show()
                }
            }
            if(cmd == WlanNetworkList){
                moveMainPage = true
                var wifis = gson.fromJson(message, WlanNetworkListModel::class.java)
                for (item in wifis.data){
                    if(item.status == "current"){
                        Util.putSharedPreferenceString(this@StartSpeakerSettingActivity,Util.WIFI_NAME, item.ssid)
                        binding.progressCir.visibility = View.GONE
                        val dialog = CanUseSpeakerDialogFragment()
                        dialog.show(supportFragmentManager, "CanUseSpeakerDialogFragment")
                    }
                }

            }
        }
        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                var commandModel = CommandModel(WlanState)
                bt.send(gson.toJson(commandModel))
            }

            override fun onDeviceDisconnected() {
                Toast.makeText(
                    applicationContext, "Connection lost", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDeviceConnectionFailed() {
                Toast.makeText(
                    applicationContext, "Try to connect", Toast.LENGTH_SHORT
                ).show()
            }
        })

    }



    fun setButton() {
        binding.speakerStartPopupCancel.setOnClickListener {
            binding.speakerStartPopupRela.visibility = View.GONE
            binding.bluetoothRefreshButton.visibility = View.VISIBLE
        }
        binding.speakerStartPopupApply.setOnClickListener {
            binding.speakerStartPopupRela.visibility = View.GONE
            binding.speakerSelectPopupRela.visibility = View.VISIBLE
        }
        binding.bluetoothRefreshButton.setOnClickListener {
            Toast.makeText(this, "블루투스 재검색을 시작합니다.", Toast.LENGTH_SHORT).show()
            discover()
        }
    }


    override fun onResume() {
        super.onResume()
        if (!bt.isBluetoothEnabled) {
            bt.enable()
        } else {
            if (!bt.isServiceAvailable) {
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
            }
        }

        discover()



    }

    private fun setRecyclerview() {
        devicesAdapter = PairedDevicesAdapter(::connectDevice)
        binding.deviceRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.deviceRecyclerview.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayout.VERTICAL
            )
        )
        binding.deviceRecyclerview.apply {
            adapter = devicesAdapter
        }
    }

    var blReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                threadState = false
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // add the name to the list

                println("====Deivice List====")
                println(device?.name)
                println(device?.address)
                if (device?.name == "FRIENDS") {

                    binding.progressCir.visibility = View.GONE
                    binding.bluetoothRefreshButton.visibility = View.GONE
                    binding.speakerStartPopupRela.visibility = View.VISIBLE
                    devicesAdapter.addItem(device)
                }
            }
        }
    }

    fun discover() {

        // Check if the device is already discovering
        if (mBTAdapter.isDiscovering) {
            mBTAdapter.cancelDiscovery()
            Toast.makeText(applicationContext, getString(R.string.DisStop), Toast.LENGTH_SHORT)
                .show()
        }
        if (mBTAdapter.isEnabled) {
            mBTAdapter.startDiscovery()
            Toast.makeText(
                applicationContext,
                getString(R.string.DisStart),
                Toast.LENGTH_SHORT
            ).show()
            registerReceiver(blReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.BTnotOn),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    fun handleDialogClose(handleDialogClose: DialogFragment) {

        Util.putSharedPreferenceString(this, Util.MAC, selectDevice?.address ?: "")
        if(moveMainPage){
            val mainActivity = Intent(this, MainActivity::class.java)
            startActivity(mainActivity)
        }else{
            val selectSpeakerWifiActivity = Intent(this, SelectSpeakerWifiActivity::class.java)
            selectSpeakerWifiActivity.putExtra("fromMain", intent.getBooleanExtra("fromMain", false))
            startActivity(selectSpeakerWifiActivity)
        }

        finish()
        handleDialogClose.dismiss()
    }

    fun connectDevice(deviceToConnect: BluetoothDevice?) {
        deviceToConnect?.let {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
            binding.progressCir.visibility = View.VISIBLE
            bt.connect(it.address ?: "")
            selectDevice = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
    }
}