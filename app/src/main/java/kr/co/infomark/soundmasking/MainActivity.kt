package kr.co.infomark.soundmasking

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.os.Environment
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
import kr.co.infomark.soundmasking.main.HomeFragment
import kr.co.infomark.soundmasking.main.ListFragment
import kr.co.infomark.soundmasking.main.MyPageFragment
import kr.co.infomark.soundmasking.main.PlayFragment
import kr.co.infomark.soundmasking.model.*
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Method


class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    lateinit var gson: Gson
    var bluetoothManager = BluetoothManager(this)
    lateinit var bt: BluetoothSPP
    lateinit var selectDeivice: BluetoothDevice


    var home = HomeFragment()
    var play = PlayFragment()
    var list = ListFragment()
    var mypage = MyPageFragment()


    val files = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        gson = Gson()
        bt =  BluetoothSPP.getInstance(this); //Initializing
        supportFragmentManager.beginTransaction().replace(R.id.main_content,home).commit()
        initTabbar()
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        lsDir(dir)
    }
    fun initTabbar(){
        binding.homeBtn.setOnClickListener {
            disalbeGrayEvent()
            binding.homeImage.setImageResource(R.drawable.ico_home_black)
            supportFragmentManager.beginTransaction().replace(R.id.main_content,home).commit()
        }
        binding.playBtn.setOnClickListener {
            disalbeGrayEvent()
            binding.playImage.setImageResource(R.drawable.ico_player_black)
            supportFragmentManager.beginTransaction().replace(R.id.main_content,play).commit()
        }
        binding.listBtn.setOnClickListener {
            disalbeGrayEvent()
            binding.listImage.setImageResource(R.drawable.ico_playlist_black)
            supportFragmentManager.beginTransaction().replace(R.id.main_content,list).commit()
        }
        binding.mypageBtn.setOnClickListener {
            disalbeGrayEvent()
            binding.mypageImage.setImageResource(R.drawable.ico_mypage_black)
            supportFragmentManager.beginTransaction().replace(R.id.main_content,mypage).commit()
        }
    }
    fun disalbeGrayEvent(){
        binding.homeImage.setImageResource(R.drawable.ico_home_gray)
        binding.playImage.setImageResource(R.drawable.ico_player_gray )
        binding.listImage.setImageResource(R.drawable.ico_playlist_gray)
        binding.mypageImage.setImageResource(R.drawable.ico_mypage_gray)
    }

    fun lsDir(dir: File) {
        //contains list of all files ending with


        val listFile = dir.listFiles()
        if (listFile != null) {
            for (i in listFile.indices) {
                if (listFile[i].isDirectory) { // if its a directory need to get the files under that directory
                    lsDir(listFile[i])
                } else { // add path of  files to your arraylist for later use

                    //Do what ever u want
                    files.add(listFile[i].absolutePath)
                }
            }
        }
    }

    fun resetDevice(){
        Toast.makeText(this,"장비 초기화를 시작합니다.", Toast.LENGTH_LONG).show()
        var commandModel = RemoveCommandModel(
            WlanRemoveNetwork,
            Util.getSharedPreferenceString(this, Util.WIFI_NAME))
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
    fun removeBond(device: BluetoothDevice) {
        try {
            device::class.java.getMethod("removeBond").invoke(device)
        } catch (e: Exception) {

        }
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
