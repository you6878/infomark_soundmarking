package kr.co.infomark.soundmasking.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.FragmentMyPageBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyPageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var binding: FragmentMyPageBinding? = null

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_page, container, false)
        binding?.logMenu?.setOnClickListener {
            startActivity(Intent(activity, LogActivity::class.java))
        }
        binding?.devMenu?.setOnClickListener {
            var mainActivity = requireActivity() as? MainActivity
            if(mainActivity?.bt?.isConnected == true){
                startActivity(Intent(activity, DevelopActivity::class.java))
            }else{
                Toast.makeText(requireActivity(),"현재 SPP 블루투스 연결 중 입니다. 잠시 후 다시 시도해주세요.",Toast.LENGTH_LONG).show()
                val main = activity as? MainActivity
                main?.buletoothInit()
            }
        }
        var mainActivity = requireActivity() as? MainActivity
        mainActivity?.bluetoothConnectStatus?.observe(viewLifecycleOwner) {
            binding?.statusConnectTextview?.text = it
        }
        binding?.updateAppStatusTextview?.text = getVersionName()
        return binding?.root
    }
    fun getVersionName() : String{
        val pInfo = context?.packageName?.let { context?.packageManager?.getPackageInfo(it, 0) }
        return pInfo?.versionName ?: ""
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyPageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}