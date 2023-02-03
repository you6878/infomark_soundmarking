package kr.co.infomark.soundmasking

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.*
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
    var binding: ActivityMainBinding? = null
    lateinit var gson: Gson
    val tika = Tika()
    var bluetoothManager = BluetoothManager(this)
    var bt: BluetoothSPP? = null
    lateinit var selectDeivice: BluetoothDevice


    var home = HomeFragment()
    var play = PlayFragment()
    var list = ListFragment()
    var mypage = MyPageFragment()

    val storageFiles = mutableListOf<File>()

    val musicBox = MusicBox(this);

    var stateJob : Job? = null
    var stateThreadWhile = true
    /*ViewModel*/


    var bluetoothConnectStatus: MutableLiveData<String>? = MutableLiveData("Connecting")
    var bluetoothNameStatus: MutableLiveData<String>? = MutableLiveData("정보 확인 중..")
    var bluetoothFirmwareVersion: MutableLiveData<String>? = MutableLiveData("정보 확인 중..")



    var wifiName: MutableLiveData<String>? = MutableLiveData("정보 확인 중..")
    var wifiBssid: MutableLiveData<String>? = MutableLiveData("정보 확인 중..")
    var wifiConnectStatus: MutableLiveData<String>? = MutableLiveData("Connecting")


    var progress: MutableLiveData<Int>? = MutableLiveData(0)


    var volumeFloat: MutableLiveData<Float>? = MutableLiveData(0f)
    var visualizerFloat1: MutableLiveData<Float>? = MutableLiveData(0f)
    var visualizerFloat2: MutableLiveData<Float>? = MutableLiveData(0f)
    var visualizerFloat3: MutableLiveData<Float>? = MutableLiveData(0f)
    var visualizerFloat4: MutableLiveData<Float>? = MutableLiveData(0f)




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        gson = Gson()
        bt = BluetoothSPP.getInstance(this); //Initializing

        supportFragmentManager.beginTransaction().replace(R.id.main_content, home).commitAllowingStateLoss()
        initTabbar()

        loadFile()

        checkWifiState()
    }
    fun initSetting(){

        musicBox.currentIndex = 0;
        musicBox.selectFileSize = -1;
        musicBox.currentPlayMusicName.value = ""
        progress?.value = 0
    }
    fun loadFile(){

        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        lsDir(dir)
        //오룸차순 설정
        storageFiles.sortBy{ it.name }
        musicBox.setPlayList(storageFiles.toList())
        storageFiles.clear()

    }
    fun displayOn(){
        val win: Window = window
        win.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        win.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }
    fun displayOff(){
        val win: Window = window
        win.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        win.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        win.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        win.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
    }

    fun initTabbar() {
        binding?.homeBtn?.setOnClickListener {
            disalbeGrayEvent()
            binding?.homeImage?.setImageResource(R.drawable.ico_playerbar_home_icon_on)
            supportFragmentManager.beginTransaction().replace(R.id.main_content, home).commitAllowingStateLoss()
            binding?.titleTextview?.text = "홈"
            displayOff()
        }
        binding?.playBtn?.setOnClickListener {
            disalbeGrayEvent()
            binding?.playImage?.setImageResource(R.drawable.ico_playerbar_player_icon_on)
            supportFragmentManager.beginTransaction().replace(R.id.main_content, play).commitAllowingStateLoss()
            binding?.titleTextview?.text = "플레이어"
            displayOn()
        }
        binding?.listBtn?.setOnClickListener {
            disalbeGrayEvent()
            binding?.listImage?.setImageResource(R.drawable.ico_playerbar_playlist_icon_on)
            supportFragmentManager.beginTransaction().replace(R.id.main_content, list).commit()
            binding?.titleTextview?.text = "재생목록"
            displayOn()
        }
        binding?.mypageBtn?.setOnClickListener {
            disalbeGrayEvent()
            binding?.mypageImage?.setImageResource(R.drawable.ico_playerbar_mypage_icon_on)
            supportFragmentManager.beginTransaction().replace(R.id.main_content, mypage).commit()
            binding?.titleTextview?.text = "마이페이지"
            displayOff()
        }
    }

    fun disalbeGrayEvent() {
        binding?.homeImage?.setImageResource(R.drawable.ico_home_gray)
        binding?.playImage?.setImageResource(R.drawable.ico_player_gray)
        binding?.listImage?.setImageResource(R.drawable.ico_playlist_gray)
        binding?.mypageImage?.setImageResource(R.drawable.ico_mypage_gray)
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
                    val mimeType = file.extension
                    if(mimeType == "mp3" || mimeType == "wav"){
                        storageFiles.add(File(listFile[i].absolutePath))
                    }
                }
            }
        }




    }

    fun setBTListener() {

        bt?.setOnDataReceivedListener { data, message ->
            println(message)
            volumeFloat


            var state = JSONObject(message).isNull("state")
            if(!state){
                val item = gson.fromJson(message, StateModel::class.java)
                if(item.state == "masking" && item.bands.size == 4){
                    visualizerFloat1?.value = item.bands[0].gain.toFloat()
                    visualizerFloat2?.value = item.bands[1].gain.toFloat()
                    visualizerFloat3?.value = item.bands[2].gain.toFloat()
                    visualizerFloat4?.value = item.bands[3].gain.toFloat()
                    volumeFloat?.value = item.gain.toFloat() * 100
                }
            }



            var isLog = JSONObject(message).isNull("log")
            if (!isLog) {
                val item = gson.fromJson(message, LogModel::class.java)
                saveLog(item)
                return@setOnDataReceivedListener
            }

            var isCmd = JSONObject(message).isNull("cmd")
            if(!isCmd){
                var cmd = JSONObject(message).getString("cmd")
                if (cmd == WlanState) {
                    var model = gson.fromJson(message, DefaultModel::class.java)
                    if (model.result == "ok") {
                        wifiConnectStatus?.value = model.state
                    }
                }
                if (cmd == WlanNetworkList) {
                    var model = gson.fromJson(message, WlanNetworkListModel::class.java)
                    if (model.result == "ok") {
                        selectWifiConnectInfo(model.data)
                    }
                }
                if (cmd == WlanRemoveNetwork) {
                    var i = Intent(this, SelectSpeakerWifiActivity::class.java)
                    i.putExtra("RESET", true)
                    startActivity(i)
                }
                if(cmd == SystemInfo){
                    var model = gson.fromJson(message, SystemInfoResultModel::class.java)
                    if(model.result == "ok"){
                        bluetoothFirmwareVersion?.value = "FW Version: " +  model.revision +" Serial No.: "  + model.serialno
                        bluetoothNameStatus?.value = model.model +"(" + model.brand + "-" + model.character + ")"
                    }
                }
            }

        }
    }
    fun selectWifiConnectInfo(datas : MutableList<WlanNetworkListModel.Data>){
        for (item in datas){
            if(item.status == "current"){
                var ssid = item.ssid
                var bssid = item.bssid

                if(ssid.length > 1){
                    ssid = ssid.substring(1,ssid.length - 1)
                }
                wifiName?.value = ssid
                wifiBssid?.value = bssid
            }
        }
    }

    fun resetDevice() {
        Toast.makeText(this, "장비 초기화를 시작합니다.", Toast.LENGTH_LONG).show()
        var commandModel = RemoveCommandModel(
            WlanRemoveNetwork,
            Util.getSharedPreferenceString(this, Util.WIFI_NAME)
        )
        bt?.send(gson.toJson(commandModel));
        Handler(Looper.getMainLooper()).postDelayed({
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
            bt?.stopService()
            musicBox.releaseMediaPlayer()
            bluetoothManager.disConnectUsingBluetoothA2dp(selectDeivice)
            removeBond(selectDeivice)
            Util.clearSharedPreference(this@MainActivity)
            finish()
        }, 1000)
    }

    fun resetWifi() {
        Toast.makeText(this, "와이파이 초기화를 시작합니다.", Toast.LENGTH_LONG).show()
        var wifiName = Util.getSharedPreferenceString(this, Util.WIFI_NAME)
        if (wifiName == "설정 대기 중") {
            var i = Intent(this, SelectSpeakerWifiActivity::class.java)
            i.putExtra("RESET", true)
            startActivity(i)

        } else {

            var commandModel = RemoveCommandModel(
                WlanRemoveNetwork,
                Util.getSharedPreferenceString(this, Util.WIFI_NAME)
            )
            bt?.send(gson.toJson(commandModel));
        }


    }

    fun saveLog(item: LogModel) {
        var logs = Util.getSharedPreferenceString(this, Util.LOGS)
        var list: MutableList<LogModel> = mutableListOf()
        if (!logs.isEmpty()) {
            list = gson.fromJson(logs, Array<LogModel>::class.java).asList().toMutableList()
        }
        list.add(item)
        Util.putSharedPreferenceString(this, Util.LOGS, gson.toJson(list))
    }

    fun resetBluetoothDevice() {
        Toast.makeText(this, "스피커 연결을 초기화를 시작합니다.", Toast.LENGTH_LONG).show()
        removeBond(selectDeivice)

        Handler(Looper.getMainLooper()).postDelayed({
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
            bt?.stopService()
            musicBox.releaseMediaPlayer()
            bluetoothManager.disConnectUsingBluetoothA2dp(selectDeivice)

            Util.clearSharedPreference(this@MainActivity)
            finish()
            val i = Intent(this, StartSpeakerSettingActivity::class.java);
            startActivity(i)
        }, 3000)


    }

    fun removeBond(device: BluetoothDevice) {
        try {
            device::class.java.getMethod("removeBond").invoke(device)
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()
        buletoothInit()
    }
    fun buletoothInit(){
        if(bt?.isBluetoothEnabled == true){
            if (bt?.isServiceAvailable == false) {
                bt?.setupService()
                bt?.startService(BluetoothState.DEVICE_OTHER)
                //Auto Connect in exceoption
                bluetoothSppConnet()
            }
            deviceCallback()
            setBTListener()
        }
    }
    fun bluetoothSppConnet(){
        if (bt?.isConnected == false) {
            val address = Util.getSharedPreferenceString(this,Util.MAC)
            bt?.connect(address)
        }
    }

    fun deviceCallback() {
        //Bluetooth Status
        val mac = Util.getSharedPreferenceString(this, Util.MAC)
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

    }

    fun checkWifiState() {
        stateJob = GlobalScope.launch {
            while (stateThreadWhile) {


                delay(500)
                wifiState()
                delay(500)
                spearkInfoState()
                delay(500)
                wlanListState()
                delay(500)
                bluetoothState()
                delay(2000)
                delay(1000)
                bluetoothSppConnet()


            }
        }
        stateJob?.start()
    }
    fun wlanListState(){
        if(wifiConnectStatus?.value == "connected"){
            var commandModel = CommandModel(WlanNetworkList)
            bt?.send(gson.toJson(commandModel))
        }

    }
    fun wifiState(){
        var commandModel = CommandModel(WlanState)
        bt?.send(gson.toJson(commandModel))
    }
    fun spearkInfoState(){
        var commandModel = CommandModel(SystemInfo)
        bt?.send(gson.toJson(commandModel))
    }

    fun bluetoothState() {
        var state = false
        var devices = bt?.pairedDeviceName
        for (item in devices ?: arrayOf()) {
            if(item == "FRIENDS"){
                state = true
            }
        }

        if (state) {
            runOnUiThread {
                bluetoothConnectStatus?.value = "Connected"
            }

        } else {
            runOnUiThread {
                bluetoothConnectStatus?.value = "Connecting"
            }
        }

    }

    override fun onDestroy() {

        bt?.stopService()
        musicBox.releaseMediaPlayer()
        bluetoothManager.disConnectUsingBluetoothA2dp(selectDeivice)
        stateJob?.cancel()
        stateThreadWhile = false
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }
}
