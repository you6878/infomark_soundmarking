package kr.co.infomark.soundmasking.intro

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.databinding.ActivityInputWifiPasswordBinding


class InputWifiPasswordActivity : AppCompatActivity() {
    private lateinit var bt: BluetoothSPP
    lateinit var binding : ActivityInputWifiPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_input_wifi_password)
        bt =  BluetoothSPP(this); //Initializing
        initView()

    }
    fun initView(){
        val id =  intent.getStringExtra("id")
        val password = intent.getStringExtra("password")
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
        binding.speakerStartPopupApply.setOnClickListener {
            setResult(RESULT_OK)
            finish()

        }
    }
}