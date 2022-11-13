package kr.co.infomark.soundmasking.intro

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.ActivitySelectSpeakerWifiBinding
import kr.co.infomark.soundmasking.intro.adapter.WifiListAdapter
import kr.co.infomark.soundmasking.util.Util

class SelectSpeakerWifiActivity : AppCompatActivity() {
    lateinit var binding : ActivitySelectSpeakerWifiBinding
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiListAdapter: WifiListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_select_speaker_wifi)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "와이파이를 켜고 있습니다.", Toast.LENGTH_LONG).show();
            wifiManager.isWifiEnabled = true;
        }
        setRecyclerview()

    }
    private fun setRecyclerview() {
        wifiListAdapter = WifiListAdapter(::nextPage)
        binding.wifiRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.wifiRecyclerview.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        binding.wifiRecyclerview.apply {
            adapter = wifiListAdapter
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val i = Intent(this, SpeakerConnectCompleteActivity::class.java)
            i.putExtra("fromMain",intent.getBooleanExtra("fromMain",false))
            intent.getStringExtra("SSID")?.let {
                Util.putSharedPreferenceString(this@SelectSpeakerWifiActivity,Util.WIFI_NAME, it)
            }
            startActivity(i)
            finish()
        }
    }
    fun nextPage(id : String){
        val i = Intent(this,InputWifiPasswordActivity::class.java)
        i.putExtra("SSID",id)
        intent.putExtra("SSID",id)
        resultLauncher.launch(i)



    }
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
        wifiManager.startScan()
        if(wifiListAdapter.scanResults.size == 0){
            binding.progressCir.visibility = View.VISIBLE
        }

    }

    val wifiScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                binding.progressCir.visibility = View.GONE
                val results = wifiManager.scanResults
                wifiListAdapter.addItems(results)

            }
        }
    }
}