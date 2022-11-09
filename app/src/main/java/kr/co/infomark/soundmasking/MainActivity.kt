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
import kr.co.infomark.soundmasking.intro.adapter.PairedDevicesAdapter
import kr.co.infomark.soundmasking.model.*
import java.nio.charset.StandardCharsets



class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object{
        const val MESSAGE_READ = 2 // used in bluetooth handler to identify message update
        private const val CONNECTING_STATUS = 3 // used in bluetooth handler to identify message status

    }
    var currentCommand = ""
    lateinit var binding : ActivityMainBinding
    var bluetoothManager = BluetoothManager(this)
    private var devices: MutableSet<BluetoothDevice>? = null
    private var device: BluetoothDevice? = null
    private var mHandler // Our main handler that will receive callback notifications
            : Handler? = null
    lateinit var gson : Gson
    lateinit var mBTAdapter: BluetoothAdapter
    lateinit var devicesAdapter : PairedDevicesAdapter
    private lateinit var bt: BluetoothSPP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        gson = Gson()
        bt =  BluetoothSPP(this); //Initializing
        bluetoothManager.enableBluetooth()
        mBTAdapter = BluetoothAdapter.getDefaultAdapter() // get a handle on the bluetooth radio
        setOnClickListener()
        //getting paired devices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        devices = BluetoothAdapter.getDefaultAdapter().bondedDevices
        setRecyclerview(::connectUsingBluetoothA2dp)

        bt.setOnDataReceivedListener { data, message ->
            if(currentCommand == WlanState){
                var model = gson.fromJson(message, DefaultModel::class.java)
                println(model)
            }
        }
        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                Toast.makeText(
                    applicationContext, "Connected to $name\n$address", Toast.LENGTH_SHORT
                ).show()
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
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_READ) {
                    var readMessage = String((msg.obj as ByteArray), StandardCharsets.UTF_8)
                    binding.readBuffer.text = readMessage
                }
                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) binding.bluetoothStatus.text = getString(R.string.BTConnected) + msg.obj else binding.bluetoothStatus.text =
                        getString(R.string.BTconnFail)
                }
            }
        }
        if (!bt.isBluetoothAvailable) { //블루투스 사용 불가라면
            // 사용불가라고 토스트 띄워줌
            Toast.makeText(this@MainActivity
                , "Bluetooth is not available"
                , Toast.LENGTH_SHORT).show();
            // 화면 종료
            finish();
        }

        binding.btnConnect.setOnClickListener {
            val intent = Intent(applicationContext, DeviceListActivity::class.java)
            startActivity(intent)
        }
        binding.btnSend.setOnClickListener {
            currentCommand = WlanState
            var commandModel = CommandModel(currentCommand)
            bt.send(gson.toJson(commandModel));
        }
    }
    private fun discover() {
        // Check if the device is already discovering
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (mBTAdapter.isDiscovering) {
            mBTAdapter.cancelDiscovery()
            Toast.makeText(applicationContext, getString(R.string.DisStop), Toast.LENGTH_SHORT)
                .show()
        } else {
            if (mBTAdapter.isEnabled) {
                mBTAdapter.startDiscovery()
                Toast.makeText(applicationContext, getString(R.string.DisStart), Toast.LENGTH_SHORT)
                    .show()
                registerReceiver(blReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            } else {
                Toast.makeText(applicationContext, getString(R.string.BTnotOn), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    var blReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // add the name to the list
                if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                device?.let { devicesAdapter.addItem(it) }

            }
        }
    }

    private fun setOnClickListener() {

        binding.btnShowPairedDevices.setOnClickListener(this)
        binding.btnDiscover.setOnClickListener(this)
        binding.btnPlay.setOnClickListener(this)
        binding.btnDisconnect.setOnClickListener(this)
    }


    override fun onDestroy() {
        bt.stopService()
        bluetoothManager.releaseMediaPlayer()
        bluetoothManager.disConnectUsingBluetoothA2dp(device)
        super.onDestroy()
    }

    override fun onPause() {
        bluetoothManager.releaseMediaPlayer()
        super.onPause()
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

    }




    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnPlay -> {
                bluetoothManager.playMusic()
            }
            R.id.btnShowPairedDevices -> {
                devices?.let { devicesAdapter.addItems(it) }

            }
            R.id.btnDiscover -> {
                discover()
            }

            R.id.btnDisconnect -> {
                bluetoothManager.releaseMediaPlayer()
                bluetoothManager.disConnectUsingBluetoothA2dp(device)
            }
        }
    }

    fun connectUsingBluetoothA2dp(deviceToConnect: BluetoothDevice?) {
        bluetoothManager.connectUsingBluetoothA2dp(deviceToConnect)
        bt.connect(deviceToConnect?.address ?: "")
    }

    private fun setRecyclerview(
        connect: (deviceToConnect: BluetoothDevice?) -> Unit
    ) {
        binding.rvPairedDevices.layoutManager = LinearLayoutManager(this)
        devicesAdapter = PairedDevicesAdapter(connect)
        binding.rvPairedDevices.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        binding.rvPairedDevices.apply {
            adapter = devicesAdapter
        }
    }
}
