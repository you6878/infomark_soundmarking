package kr.co.infomark.soundmasking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kr.co.infomark.soundmasking.databinding.LogItemBinding
import kr.co.infomark.soundmasking.model.LogModel
import java.text.SimpleDateFormat

class LogListAdapter(var logs: MutableList<LogModel>) : RecyclerView.Adapter<LogListAdapter.LogItemViewHolder>() {

    lateinit var selectDeviceItemBinding : LogItemBinding
    inner class LogItemViewHolder(val binding: LogItemBinding) : RecyclerView.ViewHolder(binding.root)

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    init {
        logs.sortByDescending { it.timestamp }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogListAdapter.LogItemViewHolder {

        selectDeviceItemBinding =  DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.log_item, parent, false)
        return LogItemViewHolder(selectDeviceItemBinding)
    }

    override fun getItemCount(): Int {
        return logs.size
    }

    override fun onBindViewHolder(holder: LogItemViewHolder, position: Int) {
        val item = logs.get(position)
        val date = java.util.Date(item.timestamp)
        holder.binding.timestampTextview.text = sdf.format(date)
        holder.binding.contentTextview.text = item.text
//        holder.binding.timestampTextview =
//        holder.itemView.setOnClickListener {
//            nextPage(item.ssid)
//        }
//        holder.binding.deviceName.text = item?.ssid
    }

    fun addItem(item: LogModel) {
        logs.add(item)
        logs.sortByDescending { it.timestamp }
        notifyDataSetChanged()
    }




}