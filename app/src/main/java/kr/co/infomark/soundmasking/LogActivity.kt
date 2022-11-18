package kr.co.infomark.soundmasking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.infomark.soundmasking.bluetooth.BluetoothManager
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.databinding.ActivityLogBinding
import kr.co.infomark.soundmasking.model.LogModel
import org.json.JSONObject

class LogActivity : AppCompatActivity() {
    lateinit var binding : ActivityLogBinding
    lateinit var gson: Gson
    lateinit var bt: BluetoothSPP
    var logListAdapter = LogListAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gson = Gson()
        bt =  BluetoothSPP.getInstance(this)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_log)
        binding.toolbarBackLeft.setOnClickListener { finish() }
        binding.logRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.logRecyclerview.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        binding.logRecyclerview.apply { adapter = logListAdapter }

    }

    override fun onResume() {
        super.onResume()
        setBTListener()
    }
    fun setBTListener(){

        bt.setOnDataReceivedListener { data, message ->

            var isLog = JSONObject(message).isNull("log")
            if (!isLog) {
                println(message)
                val item = gson.fromJson(message,LogModel::class.java)
                logListAdapter.addItem(item)
                return@setOnDataReceivedListener
            }
        }
    }

}