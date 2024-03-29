package kr.co.infomark.soundmasking.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.databinding.ActivityDevelopBinding
import kr.co.infomark.soundmasking.model.*
import kr.co.infomark.soundmasking.util.Util
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat

class DevelopActivity : AppCompatActivity() {
    lateinit var gson: Gson
    var bt: BluetoothSPP? = null
    var binding: ActivityDevelopBinding? = null
    var formatter = DecimalFormat("###.#############")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_develop)
        binding?.noiseReportTextview?.setOnClickListener {
            if(binding?.noiseReportTextview?.text == "enabled"){
                var commandModel = CommandModel(ReportingDisable)
                bt?.send(gson.toJson(commandModel))
            }else{
                var commandModel = CommandModel(ReportingEnable)
                bt?.send(gson.toJson(commandModel))
            }
        }
        binding?.soundMaskingTextview?.setOnClickListener {
            if(binding?.soundMaskingTextview?.text == "enabled"){
                var commandModel = CommandModel(MaskingDisable)
                bt?.send(gson.toJson(commandModel))
            }else{
                var commandModel = CommandModel(MaskingEnable)
                bt?.send(gson.toJson(commandModel))
            }

        }

        binding?.soundEnOffsetBtn?.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "gg_momentum"
            commandModel.value = binding?.soundEnOffsetTextview?.text.toString()
            bt?.send(gson.toJson(commandModel))
        }
        binding?.soundGgPstepBtn?.isEnabled = false
        binding?.soundGgPstepBtn?.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "gg_pstep"
            commandModel.value = binding?.soundGgPstepTextview?.text.toString()
            bt?.send(gson.toJson(commandModel))
        }
        binding?.soundGgNstepBtn?.isEnabled = false
        binding?.soundGgNstepBtn?.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "gg_nstep"
            commandModel.value = binding?.soundGgNstepTextview?.text.toString()
            println(gson.toJson(commandModel))
            bt?.send(gson.toJson(commandModel))
        }
        binding?.soundEqPsetpBtn?.isEnabled = false
        binding?.soundEqPsetpBtn?.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "eq_step"
            commandModel.value = binding?.soundEqPsetpTextview?.text.toString()

            bt?.send(gson.toJson(commandModel))
        }
        binding?.soundGgPclipBtn?.isEnabled = false
        binding?.soundGgPclipBtn?.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "gg_pclip"
            commandModel.value = binding?.soundGgPclipTextview?.text.toString()

            bt?.send(gson.toJson(commandModel))
        }
        binding?.soundGgNclipBtn?.isEnabled = false
        binding?.soundGgNclipBtn?.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "gg_nclip"
            commandModel.value = binding?.soundGgNclipTextview?.text.toString()

            bt?.send(gson.toJson(commandModel))
        }
        binding?.soundEqClipBtn?.isEnabled = false
        binding?.soundEqClipBtn?.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "eq_clip"
            commandModel.value = binding?.soundEqClipTextview?.text.toString()

            bt?.send(gson.toJson(commandModel))
        }

        binding?.toolbarBackLeft?.setOnClickListener {
            finish()
        }
        bt = BluetoothSPP.getInstance(this)
        gson = Gson()
    }

    override fun onResume() {
        super.onResume()
        setBTListener()
        getSoundMaskingState()
    }

    fun getSoundMaskingState() {
        GlobalScope.launch {
            var maskingState = CommandModel(MaskingState)
            bt?.send(gson.toJson(maskingState))
            var reportingState = CommandModel(ReportingState)
            bt?.send(gson.toJson(reportingState))
            var maskingGetParameter = CommandModel(MaskingGetParameter)
            bt?.send(gson.toJson(maskingGetParameter))
        }.start()

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
    fun setBTListener() {
        bt?.setOnDataReceivedListener { data, message ->
            var isLog = JSONObject(message).isNull("log")
            if (!isLog) {
                val item = gson.fromJson(message,LogModel::class.java)
                saveLog(item)
                return@setOnDataReceivedListener
            }
            println(message)




            var isCmd = JSONObject(message).isNull("cmd")
            if(!isCmd){
                var cmd = JSONObject(message).getString("cmd")
                if(cmd == ReportingState){
                    var model = gson.fromJson(message, DefaultModel::class.java)
                    if (model.result == "ok") {
                        binding?.noiseReportTextview?.text = model.state
                    }
                }
                if(cmd == ReportingEnable){
                    var model = gson.fromJson(message, DefaultModel::class.java)
                    if (model.result == "ok") {
                        binding?.noiseReportTextview?.text = "enabled"
                        Toast.makeText(this@DevelopActivity,"정상적으로 적용 되었습니다",Toast.LENGTH_LONG).show()
                    }
                }

                if(cmd == ReportingDisable){
                    var model = gson.fromJson(message, DefaultModel::class.java)
                    if (model.result == "ok") {
                        binding?.noiseReportTextview?.text = "disabled"
                        Toast.makeText(this@DevelopActivity,"정상적으로 적용 되었습니다",Toast.LENGTH_LONG).show()
                    }
                }

                if (cmd == MaskingGetParameter) {
//                    MaskingGetParameterDTO();
                    var model = gson.fromJson(message, MaskingGetParameterDTO::class.java)
                    if (model.result == "ok") {
                        binding?.soundEnOffsetTextview?.setText(formatter.format(model.gg_momentum.toDouble()))

                        binding?.soundGgPstepTextview?.setText(formatter.format(model.gg_pstep.toDouble()))

                        binding?.soundGgNstepTextview?.setText(formatter.format(model.gg_nstep.toDouble()))

                        binding?.soundEqPsetpTextview?.setText(formatter.format(model.eq_step.toDouble()))

                        //gg_pclip
                        binding?.soundGgPclipTextview?.setText(formatter.format(model.gg_pclip.toDouble()))
                        //gg_nclip
                        binding?.soundGgNclipTextview?.setText(formatter.format(model.gg_nclip.toDouble()))
                        //eq_clip
                        binding?.soundEqClipTextview?.setText(formatter.format(model.eq_clip.toDouble()))
                    }
                }
                if (cmd == MaskingSetParameter) {
                    var model = gson.fromJson(message, DefaultModel::class.java)
                    if (model.result == "ok") {
                        Toast.makeText(this@DevelopActivity,"정상적으로 적용 되었습니다",Toast.LENGTH_LONG).show()
                    }
                }
                if (cmd == MaskingDisable) {
                    var model = gson.fromJson(message, DefaultModel::class.java)
                    if (model.result == "ok") {
                        binding?.soundMaskingTextview?.text = "disabled"
                        Toast.makeText(this@DevelopActivity,"정상적으로 적용 되었습니다",Toast.LENGTH_LONG).show()
                    }
                }
                if (cmd == MaskingEnable) {
                    var model = gson.fromJson(message, DefaultModel::class.java)
                    if (model.result == "ok") {
                        binding?.soundMaskingTextview?.text = "enabled"
                        Toast.makeText(this@DevelopActivity,"정상적으로 적용 되었습니다",Toast.LENGTH_LONG).show()
                    }
                }
                if (cmd == MaskingState) {
                    var model = gson.fromJson(message, DefaultModel::class.java)
                    if (model.result == "ok") {
                        binding?.soundMaskingTextview?.text = model.state
                    }
                }
            }
        }
    }
}