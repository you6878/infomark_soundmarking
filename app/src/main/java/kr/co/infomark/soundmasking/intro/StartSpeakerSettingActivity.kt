package kr.co.infomark.soundmasking.intro

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
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.ActivityStartSpeakerSettingBinding
import kr.co.infomark.soundmasking.intro.adapter.PairedDevicesAdapter
import kr.co.infomark.soundmasking.popup.CanUseSpeakerDialogFragment
import kr.co.infomark.soundmasking.util.Util


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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            kr.co.infomark.soundmasking.R.layout.activity_start_speaker_setting
        )
        gson = Gson()
        bt = BluetoothSPP.getInstance(this)
        mBTAdapter = BluetoothAdapter.getDefaultAdapter() // get a handle on the bluetooth radio
        devicesAdapter = PairedDevicesAdapter(::connectUsingBluetoothA2dp)
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
        registerReceiver(mBluetoothSearchReceiver, searchFilter)

        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                selectDevice?.let { bluetoothManager.connectUsingBluetoothA2dp(it) }
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


    //블루투스 검색결과 BroadcastReceiver
    var mBluetoothSearchReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ActivityCompat.checkSelfPermission(
                    this@StartSpeakerSettingActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    if (device?.bondState == BluetoothDevice.BOND_BONDED) {
                        binding.progressCir.visibility = View.GONE
                        val dialog = CanUseSpeakerDialogFragment()
                        dialog.show(supportFragmentManager, "CanUseSpeakerDialogFragment")
                    }
                }
            }
        }
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
        devicesAdapter = PairedDevicesAdapter(::connectUsingBluetoothA2dp)
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
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // add the name to the list
                if (ActivityCompat.checkSelfPermission(
                        this@StartSpeakerSettingActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                if (device?.name == "FRIENDS") {
                    binding.progressCir.visibility = View.GONE
                    binding.bluetoothRefreshButton.visibility = View.GONE
                    binding.speakerStartPopupRela.visibility = View.VISIBLE
                    devicesAdapter.addItem(device)
                }
            }
        }
    }

    private fun discover() {
        // Check if the device is already discovering
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (mBTAdapter.isEnabled) {
            binding.progressCir.visibility = View.VISIBLE
            mBTAdapter.startDiscovery()
            registerReceiver(blReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        } else {
            Toast.makeText(
                applicationContext,
                getString(kr.co.infomark.soundmasking.R.string.BTnotOn),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    fun handleDialogClose(handleDialogClose: DialogFragment) {

        Util.putSharedPreferenceString(this, Util.MAC, selectDevice?.address ?: "")
        val selectSpeakerWifiActivity = Intent(this, SelectSpeakerWifiActivity::class.java)
        selectSpeakerWifiActivity.putExtra("fromMain", intent.getBooleanExtra("fromMain", false))
        startActivity(selectSpeakerWifiActivity)
        finish()
        handleDialogClose.dismiss()
    }

    fun connectUsingBluetoothA2dp(deviceToConnect: BluetoothDevice?) {
        deviceToConnect?.let {
            binding.progressCir.visibility = View.VISIBLE
            bt.connect(it.address ?: "")
            selectDevice = it
        }

    }
}