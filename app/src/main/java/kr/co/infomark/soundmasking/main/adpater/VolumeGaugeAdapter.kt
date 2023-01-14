package kr.co.infomark.soundmasking.main.adpater

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.VolumeGaugeItemBinding

class VolumeGaugeAdapter(var context : Context?) : RecyclerView.Adapter<VolumeGaugeAdapter.VolumeGaugeViewHolder>() {
    var volumeGague  = 50f
    var totalVolumeCount = 44
    inner class VolumeGaugeViewHolder(val binding: VolumeGaugeItemBinding) : RecyclerView.ViewHolder(binding.root)
    lateinit var volumeGaugeItemBinding : VolumeGaugeItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolumeGaugeViewHolder {
        volumeGaugeItemBinding =  DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.volume_gauge_item, parent, false)

        return VolumeGaugeViewHolder(volumeGaugeItemBinding)
    }

    override fun onBindViewHolder(holder: VolumeGaugeViewHolder, position: Int) {
        val matchPostion = volumeGague / 100 * totalVolumeCount
        if(matchPostion >= position){
            holder.binding.gaugeBar.setBackgroundResource(R.color.black)
        } else{
            holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_gray)
        }
    }

    override fun getItemCount(): Int {
        return totalVolumeCount
    }
    fun setVolume(volume : Float){
        this.volumeGague = volume
        notifyDataSetChanged()

    }
}