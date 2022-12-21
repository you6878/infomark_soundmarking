package kr.co.infomark.soundmasking.main

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.FragmentHomeBinding
import kr.co.infomark.soundmasking.util.Costant
import kr.co.infomark.soundmasking.util.Util


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    var binding : FragmentHomeBinding? = null
    lateinit var gson : Gson
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        gson = Gson()
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)
        var mainActivity = requireActivity() as? MainActivity
        mainActivity?.bluetoothConnectStatus?.observe(viewLifecycleOwner) {
            binding?.speakerStatusTextview?.text = it
        }
        mainActivity?.wifiConnectStatus?.observe(viewLifecycleOwner){
            binding?.wifiStatusTextview?.text = it
        }
        mainActivity?.bluetoothNameStatus?.observe(viewLifecycleOwner){
            binding?.ssidTextview?.text = it
        }
        mainActivity?.bluetoothFirmwareVersion?.observe(viewLifecycleOwner){
            binding?.fwVersionTextview?.text = it
        }
//        mainActivity?.wifiName?.observe(viewLifecycleOwner){
//            binding.wifiNameTextview.text = it
//        }
//        mainActivity?.wifiBssid?.observe(viewLifecycleOwner){
//            binding.bssidTextview.text = it
//        }

        binding?.wifiLinear?.setOnLongClickListener {

            mainActivity?.resetWifi()
            true
        }
        binding?.speakerLinear?.setOnLongClickListener {
            if(binding?.speakerStatusTextview?.text == "Connected"){
                AlertDialog.Builder(requireActivity())
                    .setTitle("경고")
                    .setMessage("블루투스 스피커 재설정을 진행하시겠습니까?")
                    .setPositiveButton("확인", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            var mainActivity = requireActivity() as? MainActivity
                            mainActivity?.resetBluetoothDevice()
                        }
                    })
                    .setNegativeButton("취소", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {

                        }
                    })
                    .create()
                    .show()

            }else{
                var mainActivity = requireActivity() as? MainActivity
                mainActivity?.resetBluetoothDevice()
            }

            true
        }
        initView()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()

        binding?.wifiNameTextview?.text = Util.getSharedPreferenceString(activity,Util.WIFI_NAME)
        binding?.bssidTextview?.text = Util.getSharedPreferenceString(activity,Util.BSSID_NAME)
        binding?.appversionTextview?.text = getVersionName()
        binding?.updateDateTextview?.text = Costant.updateDate
    }
    fun getVersionName() : String{
        val pInfo = context?.packageName?.let { context?.packageManager?.getPackageInfo(it, 0) }
        return pInfo?.versionName ?: ""
    }



    fun initView(){
        binding?.playMusic?.setOnClickListener {
//            bluetoothManager.playMusic()
        }
        binding?.logBtn?.setOnClickListener {
//            startActivity(Intent(activity, LogActivity::class.java))
        }
        binding?.startSpeakerSettingBtn?.setOnClickListener {
            var mainActivity = activity as? MainActivity
            mainActivity?.resetDevice()
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}