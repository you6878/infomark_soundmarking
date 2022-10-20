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
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.infomark.soundmasking.databinding.ActivityMainBinding
import java.nio.charset.StandardCharsets
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object{
        private val TAG = MainActivity::class.java.simpleName

        private val BT_MODULE_UUID: UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // "random" unique identifier


        // #defines for identifying shared types between calling functions
        private const val REQUEST_ENABLE_BT = 1 // used to identify adding bluetooth names

        const val MESSAGE_READ = 2 // used in bluetooth handler to identify message update

        private const val CONNECTING_STATUS =
            3 // used in bluetooth handler to identify message status

    }
    lateinit var binding : ActivityMainBinding
    var bluetoothManager = BluetoothManager(this)
    private var devices: MutableSet<BluetoothDevice>? = null
    private var device: BluetoothDevice? = null
    private var mHandler // Our main handler that will receive callback notifications
            : Handler? = null

    lateinit var mBTAdapter: BluetoothAdapter
    lateinit var devicesAdapter : PairedDevicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bluetoothManager.enableBluetooth()
        mBTAdapter = BluetoothAdapter.getDefaultAdapter() // get a handle on the bluetooth radio
        setOnClickListener()

        //getting paired devices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        devices = BluetoothAdapter.getDefaultAdapter().bondedDevices
        setRecyclerview(::connectUsingBluetoothA2dp)

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
        bluetoothManager.releaseMediaPlayer()
        bluetoothManager.disConnectUsingBluetoothA2dp(device)
        super.onDestroy()
    }

    override fun onPause() {
        bluetoothManager.releaseMediaPlayer()
        super.onPause()
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
