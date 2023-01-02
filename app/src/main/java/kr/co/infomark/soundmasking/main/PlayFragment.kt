package kr.co.infomark.soundmasking.main

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.FragmentPlayBinding
import java.util.*
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var binding : FragmentPlayBinding? = null
    lateinit var timer : Timer
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
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_play, container, false)
        // Inflate the layout for this fragment
        binding?.progressBar?.progressDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

        setMusicPlay()
        setCheckTime()

        return binding?.root
    }
    fun setMusicPlay(){
        val musicBox = (requireActivity() as? MainActivity)?.musicBox
        musicBox?.currentPlayMusicName?.observe(viewLifecycleOwner) {
            binding?.currentPlayTitle?.text = it
        }
        binding?.playBtn?.setOnClickListener {
            if(musicBox?.isPlay?.value == true){
                musicBox.stopMusic()
            }else{
                musicBox?.playMusic()
            }
        }
        binding?.randomBtn?.setOnClickListener {
            musicBox?.eventRandomBtn()
        }
        binding?.repeatBtn?.setOnClickListener {
            musicBox?.eventRepeatBtn()
        }
        binding?.previewBtn?.setOnClickListener {
            musicBox?.previewMusic()
        }
        binding?.nextBtn?.setOnClickListener {
            musicBox?.nextMusic()
        }


        musicBox?.isPlay?.observe(viewLifecycleOwner){
            if(it){
                binding?.playBtn?.setImageResource(R.drawable.ico_playbutton_pause)
            }else{
                binding?.playBtn?.setImageResource(R.drawable.ico_playbutton_play)

            }
        }


        musicBox?.randomPlay?.observe(viewLifecycleOwner) {
            if(it){
                binding?.randomBtn?.setImageResource(R.drawable.ico_light_shuffle_activated)
            }else{
                binding?.randomBtn?.setImageResource(R.drawable.ico_light_shuffle_deactivated)
            }
        }

        musicBox?.repeatPlay?.observe(viewLifecycleOwner) {

            //0 반복해제, 1:한곡반복, 2:여러곡반복
            if(it == 1){
                binding?.repeatBtn?.setImageResource(R.drawable.ico_light_repeat_activated)
            }else if(it == 2){
                binding?.repeatBtn?.setImageResource(R.drawable.ico_light_all_repeat_activated)
            } else{
                binding?.repeatBtn?.setImageResource(R.drawable.ico_light_repeat_deactivated)
            }
        }
    }


    fun setCheckTime(){
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                var mainActivity = activity as? MainActivity
                var isPlaying = mainActivity?.musicBox?.isPlay?.value
                if (isPlaying == true) {
                    getCurrentTime()
                }
            }
        }, 0, 1000)
    }

    fun getCurrentTime(){
        try {
            val mainActivity = requireActivity() as? MainActivity
            val currentTime = mainActivity?.musicBox?.getMPlayerCurrentTime() ?: 0
            val durationTime = mainActivity?.musicBox?.getMPlayerDurationTime() ?: 0
            val seekbarGuage = (currentTime.toFloat() / durationTime.toFloat()) * 100
            println(seekbarGuage)
            mainActivity?.runOnUiThread {
                binding?.currentTimeTextview?.text = setTime(currentTime.toLong());
                binding?.remainTimeTextview?.text = setTime(durationTime.toLong());
                binding?.progressBar?.progress = seekbarGuage.toInt()
            }

        }catch (e : Exception){

        }
    }

    fun setTime(millis : Long) : String{
        var res = ""
        var minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        var secounds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))


        if(minutes.toString().count() == 2){
            res += minutes.toString()
        }else{
            res += "0$minutes"
        }
        res += ":"

        if(secounds.toString().count() == 2){
            res += secounds.toString()
        }else{
            res += "0$secounds"
        }
        return res

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}