package kr.co.infomark.soundmasking.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.ActivitySpeakerConnectCompleteBinding
import kr.co.infomark.soundmasking.model.DeviceModel
import kr.co.infomark.soundmasking.util.Util

class SpeakerConnectCompleteActivity : AppCompatActivity() {
    lateinit var binding : ActivitySpeakerConnectCompleteBinding
    lateinit var gson : Gson
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_speaker_connect_complete)
        gson = Gson()
        binding.wifiConnectCompleteButton.setOnClickListener {

            val fromMainActivity = intent.getBooleanExtra("fromMain",false);
            if(!fromMainActivity){
                startActivity(Intent(this,MainActivity::class.java))
            }
            finish()


        }

    }
}