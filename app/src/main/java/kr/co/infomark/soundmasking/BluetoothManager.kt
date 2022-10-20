package kr.co.infomark.soundmasking

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.lang.reflect.Method

class BluetoothManager(var context : MainActivity) {


    private var isEnabled: Boolean = false
    private var REQUEST_ENABLE_BT = 0
    private var mPlayer: MediaPlayer? = null
    private var devices: MutableSet<BluetoothDevice>? = null
    private var device: BluetoothDevice? = null
    private var b: IBinder? = null
    private lateinit var a2dp: BluetoothA2dp  //class to connect to an A2dp device
    private lateinit var ia2dp: IBluetoothA2dp
    private lateinit var devicesAdapter: PairedDevicesAdapter
    private var mIsA2dpReady = false
    var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    fun setIsA2dpReady(ready: Boolean) {
        mIsA2dpReady = ready
    }

    fun connectUsingBluetoothA2dp(deviceToConnect: BluetoothDevice?) {
        try {
            val c2 = Class.forName("android.os.ServiceManager")
            val m2: Method = c2.getDeclaredMethod("getService", String::class.java)
            b = m2.invoke(c2.newInstance(), "bluetooth_a2dp") as IBinder?
            if (b == null) {
                // For Android 4.2 Above Devices
                device = deviceToConnect
                //establish a connection to the profile proxy object associated with the profile
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(
                    context,
                    // listener notifies BluetoothProfile clients when they have been connected to or disconnected from the service
                    object : BluetoothProfile.ServiceListener {
                        override fun onServiceDisconnected(profile: Int) {
                            setIsA2dpReady(false)
                            disConnectUsingBluetoothA2dp(device)
                        }

                        override fun onServiceConnected(
                            profile: Int,
                            proxy: BluetoothProfile
                        ) {
                            a2dp = proxy as BluetoothA2dp
                            try {
                                //establishing bluetooth connection with A2DP devices
                                a2dp.javaClass
                                    .getMethod("connect", BluetoothDevice::class.java)
                                    .invoke(a2dp, deviceToConnect)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            setIsA2dpReady(true)
                        }
                    }, BluetoothProfile.A2DP
                )
            } else {
                val c3 =
                    Class.forName("android.bluetooth.IBluetoothA2dp")
                val s2 = c3.declaredClasses
                val c = s2[0]
                val m: Method = c.getDeclaredMethod("asInterface", IBinder::class.java)
                m.isAccessible = true
                ia2dp = m.invoke(null, b) as IBluetoothA2dp
                ia2dp.connect(deviceToConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



     fun playMusic() {
        //streaming music on the connected A2DP device
        mPlayer = MediaPlayer()
        try {
            mPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            val descriptor = context.assets.openFd("testsong_20_sec.mp3")
            mPlayer?.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length);
//            mPlayer?.setDataSource(
//                this,
//                descriptor.fileDescriptor
//            )
            mPlayer?.prepare()
            mPlayer?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun disConnectUsingBluetoothA2dp(deviceToConnect: BluetoothDevice?) {
        try {
            // For Android 4.2 Above Devices
            if (b == null) {
                try {
                    //disconnecting bluetooth device
                    a2dp.javaClass.getMethod(
                        "disconnect",
                        BluetoothDevice::class.java
                    ).invoke(a2dp, deviceToConnect)
                    BluetoothAdapter.getDefaultAdapter()
                        .closeProfileProxy(BluetoothProfile.A2DP, a2dp)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                ia2dp.disconnect(deviceToConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

     fun releaseMediaPlayer() {
        mPlayer?.release()
    }

    fun enableBluetooth() {
        //Checking if bluetooth is on or off
        if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
            context.binding.tbBluetooth.isChecked = true
            isEnabled = true
        }
        context.binding.tbBluetooth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                    //turn bluetooth on
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        return@setOnCheckedChangeListener
                    }
                    context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    isEnabled = true
                }
            } else {
                if (isEnabled) {
                    //turn bluetooth off
                    BluetoothAdapter.getDefaultAdapter().disable()
                    isEnabled = false
                }
            }
        }
    }
}