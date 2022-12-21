package kr.co.infomark.soundmasking.intro

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.ActivitySpeakerWifiCofirmBinding


class SpeakerWifiCofirmActivity : AppCompatActivity() {
    var binding : ActivitySpeakerWifiCofirmBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_speaker_wifi_cofirm)
        binding?.toolbarBackLeft?.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
    }



}