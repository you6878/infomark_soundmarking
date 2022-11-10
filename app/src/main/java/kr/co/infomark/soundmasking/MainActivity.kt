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



class MainActivity : AppCompatActivity() {
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
    }
}
