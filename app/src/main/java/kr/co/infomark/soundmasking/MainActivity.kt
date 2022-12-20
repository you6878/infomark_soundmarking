package kr.co.infomark.soundmasking

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.ActivityMainBinding
import kr.co.infomark.soundmasking.intro.SelectSpeakerWifiActivity
import kr.co.infomark.soundmasking.intro.StartSpeakerSettingActivity
import kr.co.infomark.soundmasking.main.HomeFragment
import kr.co.infomark.soundmasking.main.ListFragment
import kr.co.infomark.soundmasking.main.MyPageFragment
import kr.co.infomark.soundmasking.main.PlayFragment
import kr.co.infomark.soundmasking.model.*
import kr.co.infomark.soundmasking.util.MusicBox
import kr.co.infomark.soundmasking.util.Util
import org.apache.tika.Tika
import org.json.JSONObject
import java.io.File


class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    lateinit var gson: Gson
    val tika = Tika()
    var bluetoothManager = BluetoothManager(this)
    lateinit var bt: BluetoothSPP
    lateinit var selectDeivice: BluetoothDevice


    var home = HomeFragment()
    var play = PlayFragment()
    var list = ListFragment()
    var mypage = MyPageFragment()

    val storageFiles = mutableListOf<File>()

    val musicBox = MusicBox()


    /*ViewModel*/

    var wifiConnectStatus : MutableLiveData<String> = MutableLiveData("Connecting")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        gson = Gson()
        bt =  BluetoothSPP.getInstance(this); //Initializing
        supportFragmentManager.beginTransaction().replace(R.id.main_content,home).commit()
        initTabbar()
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        lsDir(dir)
    }
    fun initTabbar(){
        binding.homeBtn.setOnClickListener {
            disalbeGrayEvent()
            binding.homeImage.setImageResource(R.drawable.ico_home_black)
            supportFragmentManager.beginTransaction().replace(R.id.main_content,home).commit()
            binding.titleTextview.text = "홈"
        }
        binding.playBtn.setOnClickListener {
            disalbeGrayEvent()
            binding.playImage.setImageResource(R.drawable.ico_player_black)
            supportFragmentManager.beginTransaction().replace(R.id.main_content,play).commit()
            binding.titleTextview.text = "플레이어"
        }
        binding.listBtn.setOnClickListener {
            disalbeGrayEvent()
            binding.listImage.setImageResource(R.drawable.ico_playlist_black)
            supportFragmentManager.beginTransaction().replace(R.id.main_content,list).commit()
            binding.titleTextview.text = "재생목록"
        }
        binding.mypageBtn.setOnClickListener {
            disalbeGrayEvent()
            binding.mypageImage.setImageResource(R.drawable.ico_mypage_black)
            supportFragmentManager.beginTransaction().replace(R.id.main_content,mypage).commit()
            binding.titleTextview.text = "마이페이지"
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
                    val file = listFile[i]
                    val mimeType = tika.detect(file)
                    if(mimeType == "audio/mpeg" || mimeType == "audio/vnd.wave"){
                        storageFiles.add(File(listFile[i].absolutePath))
                    }
                }
            }
        }
        musicBox.setPlayList(storageFiles)
    }
    fun setBTListener(){

        bt?.setOnDataReceivedListener { data, message ->
            println(message)
            var isLog = JSONObject(message).isNull("log")
            if(!isLog){
                return@setOnDataReceivedListener
            }
            var cmd = JSONObject(message).getString("cmd")
            if(cmd == WlanState){
                var model = gson.fromJson(message, DefaultModel::class.java)
                if(model.result == "ok"){
                    wifiConnectStatus.value = model.state
                }
            }
            if(cmd == WlanRemoveNetwork){
                var i = Intent(this, SelectSpeakerWifiActivity::class.java)
                i.putExtra("RESET",true)
                startActivity(i)
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
            musicBox.releaseMediaPlayer()
            bluetoothManager.disConnectUsingBluetoothA2dp(selectDeivice)
            removeBond(selectDeivice)
            Util.clearSharedPreference(this@MainActivity)
            finish()
        },1000)
    }
    fun resetWifi(){
        Toast.makeText(this,"와이파이 초기화를 시작합니다.", Toast.LENGTH_LONG).show()
        var wifiName = Util.getSharedPreferenceString(this, Util.WIFI_NAME)
        if(wifiName == "설정 대기 중"){
            var i = Intent(this, SelectSpeakerWifiActivity::class.java)
            startActivity(i)

        }else{

            var commandModel = RemoveCommandModel(WlanRemoveNetwork, Util.getSharedPreferenceString(this, Util.WIFI_NAME))
            bt.send(gson.toJson(commandModel));
        }



    }
    fun resetBluetoothDevice(){
        Toast.makeText(this,"스피커 연결을 초기화를 시작합니다.", Toast.LENGTH_LONG).show()
        removeBond(selectDeivice)

        Handler(Looper.getMainLooper()).postDelayed({
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
            bt.stopService()
            musicBox.releaseMediaPlayer()
            bluetoothManager.disConnectUsingBluetoothA2dp(selectDeivice)

            Util.clearSharedPreference(this@MainActivity)
            finish()
            val i = Intent(this,StartSpeakerSettingActivity::class.java);
            startActivity(i)
        },3000)


    }
    fun removeBond(device: BluetoothDevice) {
        try {
            device::class.java.getMethod("removeBond").invoke(device)
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()
        if (bt?.isBluetoothEnabled == false) {
            bt.enable()
        } else {
            if (bt?.isServiceAvailable == false) {
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
                //Auto Connect in exceoption
                if(bt.connectedDeviceAddress == null){
                    val mac = Util.MAC
                    bt.connect(Util.getSharedPreferenceString(this,mac))
                }
            }
            deviceCallback()
            setBTListener()
        }
    }
    fun deviceCallback(){
        //Bluetooth Status
        val mac = Util.getSharedPreferenceString(this,Util.MAC)
        selectDeivice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac)
        bluetoothManager?.enableBluetooth()
        bluetoothManager?.connectUsingBluetoothA2dpCallback(selectDeivice)
        //Wifi Status
        bt?.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {

            }

            override fun onDeviceDisconnected() {

            }

            override fun onDeviceConnectionFailed() {

            }
        })
        checkWifiState()
    }
    fun checkWifiState(){
        GlobalScope.launch {
            delay(2000)
            var commandModel = CommandModel(WlanState)
            bt?.send(gson.toJson(commandModel))
        }.start()
    }

    override fun onDestroy() {

        bt.stopService()
        musicBox.releaseMediaPlayer()
        bluetoothManager.disConnectUsingBluetoothA2dp(selectDeivice)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }
}
