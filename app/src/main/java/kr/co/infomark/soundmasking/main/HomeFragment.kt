package kr.co.infomark.soundmasking.main

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.infomark.soundmasking.LogActivity
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.FragmentHomeBinding
import kr.co.infomark.soundmasking.model.*
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    lateinit var binding : FragmentHomeBinding
    lateinit var gson : Gson
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        gson = Gson()
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)
        initView()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.ssidTextview.text = Util.getSharedPreferenceString(activity,Util.MAC)
        binding.wifiNameTextview.text = Util.getSharedPreferenceString(activity,Util.WIFI_NAME)
        var bt = (activity as MainActivity).bt
        if (!bt.isBluetoothEnabled) {
            bt.enable()
        } else {
            if (!bt.isServiceAvailable) {
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
                //Auto Connect in exceoption
                if(bt.connectedDeviceAddress == null){
                    val mac = Util.MAC
                    bt.connect(Util.getSharedPreferenceString(activity,mac))
                }
            }
            deviceCallback()
        }
        checkWifiState()
        setBTListener()

    }


    fun initView(){
        binding.playMusic.setOnClickListener {
//            bluetoothManager.playMusic()
        }
        binding.logBtn.setOnClickListener {
            startActivity(Intent(activity, LogActivity::class.java))
        }
        binding.startSpeakerSettingBtn.setOnClickListener {
            var mainActivity = activity as MainActivity
            mainActivity.resetDevice()
        }
    }
    fun setBTListener(){
        var bt = (activity as MainActivity).bt
        bt.setOnDataReceivedListener { data, message ->
            println(message)
            var isLog = JSONObject(message).isNull("log")
            if(!isLog){
                return@setOnDataReceivedListener
            }
            var cmd = JSONObject(message).getString("cmd")
            if(cmd == WlanState){
                var model = gson.fromJson(message, DefaultModel::class.java)
                if(model.result == "ok"){
                    binding.wifiStatusTextview.text = model.state
                }
            }
        }
    }
    fun checkWifiState(){
        var bt = (activity as MainActivity).bt
        GlobalScope.launch {
            delay(2000)
            var commandModel = CommandModel(WlanState)
            bt.send(gson.toJson(commandModel))
        }.start()
    }

    fun deviceCallback(){

        //Bluetooth Status
        var  mainActivity = (activity as MainActivity)
        val mac = Util.getSharedPreferenceString(activity,Util.MAC)
        mainActivity.selectDeivice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac)
        mainActivity.bluetoothManager.enableBluetooth()
        mainActivity.bluetoothManager.connectUsingBluetoothA2dpCallback(mainActivity.selectDeivice) {
            if(it){
                binding.speakerStatusTextview.text = "Connected"
            }
        }
        //Wifi Status
        var bt = (activity as MainActivity).bt
        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                checkWifiState()
            }

            override fun onDeviceDisconnected() {

            }

            override fun onDeviceConnectionFailed() {

            }
        })
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}