package kr.co.infomark.soundmasking.intro

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.intro.adapter.PairedDevicesAdapter
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.databinding.ActivityStartSpeakerSettingBinding
import kr.co.infomark.soundmasking.popup.CanUseSpeakerDialogFragment

class StartSpeakerSettingActivity : AppCompatActivity() {
    companion object{
        const val MESSAGE_READ = 2 // used in bluetooth handler to identify message update
        const val CONNECTING_STATUS = 3 // used in bluetooth handler to identify message status

    }
    lateinit var bt: BluetoothSPP
    lateinit var gson: Gson
    lateinit var binding : ActivityStartSpeakerSettingBinding
    lateinit var mHandler : Handler
    lateinit var devicesAdapter : PairedDevicesAdapter
    var bluetoothManager = BluetoothManager(this)
    lateinit var mBTAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_start_speaker_setting)
        gson = Gson()
        bt =  BluetoothSPP(this)
        mBTAdapter = BluetoothAdapter.getDefaultAdapter() // get a handle on the bluetooth radio
        devicesAdapter = PairedDevicesAdapter(::connectUsingBluetoothA2dp)
        setRecyclerview()
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_READ) {

                }
                if (msg.what == CONNECTING_STATUS) {

                }
            }
        }
        binding.speakerStartPopupCancel.setOnClickListener {
            binding.speakerStartPopupRela.visibility = View.GONE
        }
        binding.speakerStartPopupApply.setOnClickListener {
            binding.speakerStartPopupRela.visibility = View.GONE
            binding.speakerSelectPopupRela.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        discover()
    }
    private fun setRecyclerview() {
        devicesAdapter = PairedDevicesAdapter(::connectUsingBluetoothA2dp)
        binding.deviceRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.deviceRecyclerview.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        binding.deviceRecyclerview.apply {
            adapter = devicesAdapter
        }
    }
    var blReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // add the name to the list
                if (ActivityCompat.checkSelfPermission(this@StartSpeakerSettingActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                println(device?.address)
                binding.speakerStartPopupRela.visibility = View.VISIBLE
                device?.let { devicesAdapter.addItem(it) }
            }
        }
    }
    private fun discover() {
        // Check if the device is already discovering
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (mBTAdapter.isEnabled) {
            mBTAdapter.startDiscovery()
            registerReceiver(blReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        } else {
            Toast.makeText(applicationContext, getString(R.string.BTnotOn), Toast.LENGTH_SHORT)
                .show()
        }
    }
    fun handleDialogClose(handleDialogClose : DialogFragment){

        val selectSpeakerWifiActivity = Intent(this, SelectSpeakerWifiActivity::class.java)
        selectSpeakerWifiActivity.putExtra("fromMain",intent.getBooleanExtra("fromMain",false))
        startActivity(selectSpeakerWifiActivity)
        finish()
        handleDialogClose.dismiss()
    }
    fun connectUsingBluetoothA2dp(deviceToConnect: BluetoothDevice?) {
        binding.speakerSelectPopupRela.visibility = View.GONE
        val dialog = CanUseSpeakerDialogFragment()
        dialog.show(supportFragmentManager,"CanUseSpeakerDialogFragment")
    }
}