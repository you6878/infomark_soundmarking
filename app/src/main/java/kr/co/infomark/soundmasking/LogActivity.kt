package kr.co.infomark.soundmasking

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.databinding.ActivityLogBinding
import kr.co.infomark.soundmasking.model.LogModel
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class LogActivity : AppCompatActivity() {
    lateinit var binding : ActivityLogBinding
    lateinit var gson: Gson
    lateinit var bt: BluetoothSPP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gson = Gson()
        bt =  BluetoothSPP.getInstance(this)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_log)
        binding.toolbarBackLeft.setOnClickListener { finish() }
        binding.logRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.logRecyclerview.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        binding.logRecyclerview.apply {
            var logs = Util.getSharedPreferenceString(this@LogActivity,Util.LOGS)
            if(logs.isEmpty()){
                adapter = LogListAdapter(mutableListOf())
            }else{
                var list : MutableList<LogModel> = gson.fromJson(logs,Array<LogModel>::class.java).asList().toMutableList()
                adapter = LogListAdapter(list)
            }

        }

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
                var adapter : LogListAdapter = binding.logRecyclerview.adapter as LogListAdapter
                adapter.addItem(item)
                saveLog(item)
                return@setOnDataReceivedListener
            }
        }
    }
    fun saveLog(item : LogModel){
        var logs = Util.getSharedPreferenceString(this,Util.LOGS)
        var list : MutableList<LogModel> = mutableListOf()
        if(!logs.isEmpty()){
            list = gson.fromJson(logs,Array<LogModel>::class.java).asList().toMutableList()
        }
        list.add(item)
        Util.putSharedPreferenceString(this,Util.LOGS,gson.toJson(list))
    }

}