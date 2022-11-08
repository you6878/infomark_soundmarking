package kr.co.infomark.soundmasking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import kr.co.infomark.soundmasking.databinding.ActivityStartSpeakerSettingBinding

class StartSpeakerSettingActivity : AppCompatActivity() {
    lateinit var binding : ActivityStartSpeakerSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_start_speaker_setting)

        binding.speakerStartPopupCancel.setOnClickListener {
            binding.speakerStartPopupRela.visibility = View.GONE
        }
    }
}