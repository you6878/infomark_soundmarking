package kr.co.infomark.soundmasking.main.adpater

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.VisualizerGaugeItemBinding
import kr.co.infomark.soundmasking.databinding.VolumeGaugeItemBinding

class VisualizerGaugeAdapter(var context : Context?) : RecyclerView.Adapter<VisualizerGaugeAdapter.VisualizerGaugeViewHolder>() {
    var volumeGague  = 20f
    var totalVolumeCount = 10
    inner class VisualizerGaugeViewHolder(val binding: VisualizerGaugeItemBinding) : RecyclerView.ViewHolder(binding.root)
    lateinit var visualizerGaugeItemBinding : VisualizerGaugeItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisualizerGaugeViewHolder {
        visualizerGaugeItemBinding =  DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.visualizer_gauge_item, parent, false)

        return VisualizerGaugeViewHolder(visualizerGaugeItemBinding)
    }

    override fun onBindViewHolder(holder: VisualizerGaugeViewHolder, position: Int) {
        val matchPostion = (totalVolumeCount - 1) - (volumeGague / 100 * totalVolumeCount)
        if(matchPostion >= position){
            holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_gray)
        } else{
            holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_red)

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