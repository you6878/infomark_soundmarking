/*
 * Copyright 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kr.co.infomark.soundmasking

import android.app.Activity
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import android.os.Bundle
import android.widget.Toast
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP.BluetoothConnectionListener
import kr.co.infomark.soundmasking.bluetooth.BluetoothState
import android.content.Intent
import android.view.View
import android.widget.Button
import com.google.gson.Gson
import kr.co.infomark.soundmasking.bluetooth.DeviceList
import kr.co.infomark.soundmasking.model.CommandModel
import kr.co.infomark.soundmasking.model.DefaultModel
import kr.co.infomark.soundmasking.model.LogModel
import kr.co.infomark.soundmasking.model.WlanState
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject

class DeviceListActivity : Activity() {
    var bt: BluetoothSPP? = null
    lateinit var gson : Gson
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devicelist)
        bt = BluetoothSPP.getInstance(this)
        gson = Gson()
        if (bt?.isBluetoothAvailable == false) {
//            Toast.makeText(
//                applicationContext, "Bluetooth is not available", Toast.LENGTH_SHORT
//            ).show()
            finish()
        }
        bt?.setOnDataReceivedListener { data, message ->

            var state = JSONObject(message).isNull("state")
            if(!state){

                return@setOnDataReceivedListener
            }

            var isLog = JSONObject(message).isNull("log")
            if(!isLog){
                val item = gson.fromJson(message, LogModel::class.java)
                saveLog(item)
                return@setOnDataReceivedListener
            }
            var isCmd = JSONObject(message).isNull("cmd")
            if(!isCmd){
                var cmd = JSONObject(message).getString("cmd")
                if(cmd == "wlan_state"){
                    var model = gson.fromJson(message,DefaultModel::class.java)
                    println(model)
                }
            }

        }

        bt?.setBluetoothConnectionListener(object : BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                Toast.makeText(
                    applicationContext, "Pairing $name\n$address", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDeviceDisconnected() {
//                Toast.makeText(
//                    applicationContext, "Connection lost", Toast.LENGTH_SHORT
//                ).show()
            }

            override fun onDeviceConnectionFailed() {
//                Toast.makeText(
//                    applicationContext, "Unable to connect", Toast.LENGTH_SHORT
//                ).show()
            }
        })
        val btnConnect = findViewById<View>(R.id.btnConnect) as Button
        btnConnect.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (bt?.serviceState == BluetoothState.STATE_CONNECTED) {
                    bt?.disconnect()
                } else {
                    val intent = Intent(this@DeviceListActivity, DeviceList::class.java)
                    intent.putExtra("bluetooth_devices", "Bluetooth devices")
                    intent.putExtra("no_devices_found", "No device")
                    intent.putExtra("scanning", "Scanning")
                    intent.putExtra("scan_for_devices", "Search")
                    intent.putExtra("select_device", "Select")
                    intent.putExtra("layout_list", R.layout.device_layout_list)
                    intent.putExtra("layout_text", R.layout.device_layout_text)
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
                }
            }
        })
    }
    fun saveLog(item : LogModel){
        var logs = Util.getSharedPreferenceString(this, Util.LOGS)
        var list : MutableList<LogModel> = mutableListOf()
        if(!logs.isEmpty()){
            list = gson.fromJson(logs,Array<LogModel>::class.java).asList().toMutableList()
        }
        list.add(item)
        Util.putSharedPreferenceString(this, Util.LOGS,gson.toJson(list))
    }
    public override fun onDestroy() {
        super.onDestroy()
        bt?.stopService()
    }

    public override fun onStart() {
        super.onStart()
        if (bt?.isBluetoothEnabled == false) {
            bt?.enable()
        } else {
            if (bt?.isServiceAvailable == false) {
                bt?.setupService()
                bt?.startService(BluetoothState.DEVICE_OTHER)
                setup()
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == RESULT_OK) bt?.connect(data)
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                bt?.setupService()
            } else {
//                Toast.makeText(
//                    applicationContext, "Bluetooth was not enabled.", Toast.LENGTH_SHORT
//                ).show()
                finish()
            }
        }
    }

    fun setup() {
        val btnSend = findViewById<View>(R.id.btnSend) as Button
        btnSend.setOnClickListener {
            var commandModel = CommandModel(WlanState)
            bt?.send(gson.toJson(commandModel));
        }
    }
}