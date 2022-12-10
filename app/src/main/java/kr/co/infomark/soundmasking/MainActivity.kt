package kr.co.infomark.soundmasking

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.databinding.ActivityMainBinding
import kr.co.infomark.soundmasking.main.HomeFragment
import kr.co.infomark.soundmasking.main.ListFragment
import kr.co.infomark.soundmasking.main.MyPageFragment
import kr.co.infomark.soundmasking.main.PlayFragment
import kr.co.infomark.soundmasking.model.RemoveCommandModel
import kr.co.infomark.soundmasking.model.WlanRemoveNetwork
import kr.co.infomark.soundmasking.util.MusicBox
import kr.co.infomark.soundmasking.util.Util
import org.apache.tika.Tika
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
                    val file = listFile[i]
                    val mimeType = tika.detect(file)
                    if(mimeType == "audio/mpeg"){
                        storageFiles.add(File(listFile[i].absolutePath))
                    }
                }
            }
        }
        musicBox.setPlayList(storageFiles)
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
    fun removeBond(device: BluetoothDevice) {
        try {
            device::class.java.getMethod("removeBond").invoke(device)
        } catch (e: Exception) {

        }
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
