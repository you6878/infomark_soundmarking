package kr.co.infomark.soundmasking.popup

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.intro.StartSpeakerSettingActivity
import kr.co.infomark.soundmasking.databinding.FragmentCanUseSpeakerDialogBinding


class CanUseSpeakerDialogFragment : DialogFragment() {

    lateinit var binding : FragmentCanUseSpeakerDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_can_use_speaker_dialog, container, false)
        // Inflate the layout for this fragment
        binding.applyBtn.setOnClickListener {
            if (activity is StartSpeakerSettingActivity) (activity as StartSpeakerSettingActivity?)?.handleDialogClose(this)
        }
        return binding.root
    }

}