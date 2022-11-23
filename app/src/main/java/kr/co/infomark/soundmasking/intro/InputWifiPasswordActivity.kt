package kr.co.infomark.soundmasking.intro

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import kr.co.infomark.soundmasking.databinding.ActivityInputWifiPasswordBinding
import kr.co.infomark.soundmasking.model.*
import kr.co.infomark.soundmasking.popup.CanUseSpeakerDialogFragment
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject


class InputWifiPasswordActivity : AppCompatActivity() {
    private lateinit var bt: BluetoothSPP
    lateinit var binding : ActivityInputWifiPasswordBinding
    lateinit var gson : Gson
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_input_wifi_password)

        gson = Gson()
        bt =  BluetoothSPP.getInstance(this); //Initializing

        initView()

        bt.setOnDataReceivedListener { data, message ->
            var isLog = JSONObject(message).isNull("log")
            if(!isLog){
                return@setOnDataReceivedListener
            }
            println(message)

            var cmd = JSONObject(message).getString("cmd")
            if(cmd == WlanAddNetwork){
                var model = gson.fromJson(message, DefaultModel::class.java)
                if(model.result == "ok"){
                    setResult(RESULT_OK)
                    finish()
                }else{
                    Toast.makeText(this,"와이파이 정보가 정확하지 않습니다.",Toast.LENGTH_LONG).show()
                }
            }

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

                //Auto Connect in exceoption
                if(bt.connectedDeviceAddress == null){
                    val mac = Util.MAC
                    bt.connect(Util.getSharedPreferenceString(this,mac))
                }
            }
        }
        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                var commandModel = RemoveCommandModel(WlanRemoveNetwork,binding.wifiIdTextview.text.toString())
                println(commandModel)
                bt.send(gson.toJson(commandModel));
            }

            override fun onDeviceDisconnected() {

            }

            override fun onDeviceConnectionFailed() {

            }
        })
    }
    fun initView(){
        val id =  intent.getStringExtra("SSID")
        binding.wifiIdTextview.text = id
        binding.eyePassowrdImageview.setOnClickListener {
            val psEditText = binding.passwordWifiEdittext
            if (psEditText.inputType === InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                psEditText.inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                psEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
        }
        binding.speakerStartPopupCancel.setOnClickListener { finish() }
        binding.toolbarBackLeft.setOnClickListener { finish() }
        binding.speakerStartPopupApply.setOnClickListener {

            var commandModel = CommandModel(WlanAddNetwork,binding.wifiIdTextview.text.toString(),binding.passwordWifiEdittext.text.toString())
            bt.send(gson.toJson(commandModel));
        }
    }
}