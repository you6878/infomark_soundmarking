package kr.co.infomark.soundmasking.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.bluetooth.BluetoothSPP
import kr.co.infomark.soundmasking.databinding.ActivityDevelopBinding
import kr.co.infomark.soundmasking.model.*
import org.json.JSONObject

class DevelopActivity : AppCompatActivity() {
    lateinit var gson: Gson
    lateinit var bt: BluetoothSPP
    lateinit var binding: ActivityDevelopBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_develop)
        binding.soundMaskingEnable.setOnClickListener {
            var commandModel = CommandModel(MaskingEnable)
            bt.send(gson.toJson(commandModel))
        }
        binding.soundMaskingDisable.setOnClickListener {
            var commandModel = CommandModel(MaskingDisable)
            bt.send(gson.toJson(commandModel))
        }

        binding.noiseReportAbleBtn.setOnClickListener {
            var commandModel = CommandModel(MaskingGetParameter)
            bt.send(gson.toJson(commandModel))
        }
//        binding.noiseReportDisableBtnn.setOnClickListener {
//            var commandModel = CommandModel(MaskingGetParameter)
//            bt.send(gson.toJson(commandModel))
//        }
        binding.soundGgPstepBtn.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "gg_pstep"
            commandModel.value = binding.soundGgPsetpTextview.text.toString()
            bt.send(gson.toJson(commandModel))
        }
        binding.soundGgNstepBtn.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "gg_nstep"
            commandModel.value = binding.soundGgNstepTextview.text.toString()
            bt.send(gson.toJson(commandModel))
        }
        binding.soundEqPsetpBtn.setOnClickListener {
            var commandModel = MaskingSetParameterDTO()
            commandModel.cmd = "masking_set_parameter"
            commandModel.key = "eq_step"
            commandModel.value = binding.soundGgPsetpTextview.text.toString()
            bt.send(gson.toJson(commandModel))
        }

        binding.toolbarBackLeft.setOnClickListener {
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
            bt.send(gson.toJson(maskingState))
            var maskingGetParameter = CommandModel(MaskingGetParameter)
            bt.send(gson.toJson(maskingGetParameter))
        }.start()

    }

    fun setBTListener() {
        bt.setOnDataReceivedListener { data, message ->
            var isLog = JSONObject(message).isNull("log")
            if (!isLog) {

                return@setOnDataReceivedListener
            }
            println(message)
            var cmd = JSONObject(message).getString("cmd")
            if (cmd == MaskingGetParameter) {
//                    MaskingGetParameterDTO();
                var model = gson.fromJson(message, MaskingGetParameterDTO::class.java)
                if (model.result == "ok") {
                    binding.soundGgPsetpTextview.setText(model.gg_pstep)
                    binding.soundGgNstepTextview.setText(model.gg_nstep)
                    binding.soundEqPsetpTextview.setText(model.eq_step)
                }
            }
            if (cmd == MaskingSetParameter) {
                var model = gson.fromJson(message, DefaultModel::class.java)
                if (model.result == "ok") {
                    Toast.makeText(this@DevelopActivity,"적용 되었습니다",Toast.LENGTH_LONG).show()
                }
            }
            if (cmd == MaskingDisable) {
                var model = gson.fromJson(message, DefaultModel::class.java)
                if (model.result == "ok") {
                    binding.soundMaskingTextview.text = "disable"
                }
            }
            if (cmd == MaskingEnable) {
                var model = gson.fromJson(message, DefaultModel::class.java)
                if (model.result == "ok") {
                    binding.soundMaskingTextview.text = "enable"
                }
            }
            if (cmd == MaskingState) {
                var model = gson.fromJson(message, DefaultModel::class.java)
                if (model.result == "ok") {
                    binding.soundMaskingTextview.text = model.state
                }
            }
        }
    }
}