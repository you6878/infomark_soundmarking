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
    var maxDegree = 3
    var volumeGague  = 0f
    var totalVolumeCount = 11f
    var volumeSideCount = 5
    inner class VisualizerGaugeViewHolder(val binding: VisualizerGaugeItemBinding) : RecyclerView.ViewHolder(binding.root)
    lateinit var visualizerGaugeItemBinding : VisualizerGaugeItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisualizerGaugeViewHolder {
        visualizerGaugeItemBinding =  DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.visualizer_gauge_item, parent, false)

        return VisualizerGaugeViewHolder(visualizerGaugeItemBinding)
    }

    override fun onBindViewHolder(holder: VisualizerGaugeViewHolder, position: Int) {

        //초기화
        if(position == volumeSideCount){
            holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_red)
        }else{
            holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_gray)
        }


        val matchPostion = volumeSideCount - (volumeGague / maxDegree * volumeSideCount)
        if(matchPostion.toInt() < volumeSideCount){
            val matchPostion = volumeSideCount - (volumeGague / maxDegree * volumeSideCount)

            if(position >= matchPostion.toInt()){
                holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_red)
            }
            if(position > 5){
                holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_gray)
            }
        }else if(matchPostion.toInt() > volumeSideCount){
            if(position <= matchPostion.toInt()){
                //보다 작을때
                holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_red)
            }
            if(position < 5){
                holder.binding.gaugeBar.setBackgroundResource(R.color.gauge_gray)
            }
        }





    }

    override fun getItemCount(): Int {
        return totalVolumeCount.toInt()
    }
    fun setVolume(volume : Float){
        this.volumeGague = volume
        notifyDataSetChanged()

    }
}