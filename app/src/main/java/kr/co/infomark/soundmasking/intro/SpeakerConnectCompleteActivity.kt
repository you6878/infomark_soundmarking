package kr.co.infomark.soundmasking.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.ActivitySpeakerConnectCompleteBinding

class SpeakerConnectCompleteActivity : AppCompatActivity() {
    lateinit var binding : ActivitySpeakerConnectCompleteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_speaker_connect_complete)
        binding.wifiConnectCompleteButton.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

    }
}