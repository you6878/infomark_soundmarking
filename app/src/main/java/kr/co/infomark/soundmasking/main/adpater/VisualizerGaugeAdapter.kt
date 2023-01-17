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
    var maxDegree = 6
    var volumeGague  = 3f
    var totalVolumeCount = 11
    inner class VisualizerGaugeViewHolder(val binding: VisualizerGaugeItemBinding) : RecyclerView.ViewHolder(binding.root)
    lateinit var visualizerGaugeItemBinding : VisualizerGaugeItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisualizerGaugeViewHolder {
        visualizerGaugeItemBinding =  DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.visualizer_gauge_item, parent, false)

        return VisualizerGaugeViewHolder(visualizerGaugeItemBinding)
    }

    override fun onBindViewHolder(holder: VisualizerGaugeViewHolder, position: Int) {

        if(volumeGague > 0){
            val matchPostion = totalVolumeCount - (volumeGague / maxDegree * totalVolumeCount)

            if(position >= matchPostion){

                holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_red)
            }
            if(position > 5){
                holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_gray)
            }


        }else{
            val matchPostion = totalVolumeCount - (volumeGague / maxDegree * totalVolumeCount)

            if(position <= matchPostion){
                //보다 작을때
                holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_red)
            }
            if(position < 5){
                holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_gray)
            }
        }




        if(position == 5){
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