package kr.co.infomark.soundmasking.intro.adapter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.SelectWifiItemBinding
import kr.co.infomark.soundmasking.model.WlanNetworkListModel

class WifiListAdapter(var nextPage: (String,String) -> Unit) : RecyclerView.Adapter<WifiListAdapter.WifiItemViewHolder>() {

    var scanResult = WlanNetworkListModel()
    lateinit var selectDeviceItemBinding : SelectWifiItemBinding
    inner class WifiItemViewHolder(val binding: SelectWifiItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiItemViewHolder {

        selectDeviceItemBinding =  DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.select_wifi_item, parent, false)
        return WifiItemViewHolder(selectDeviceItemBinding)
    }

    override fun getItemCount(): Int {
        return scanResult.data.size
    }

    override fun onBindViewHolder(holder: WifiItemViewHolder, position: Int) {
        val item = scanResult.data.get(position)
        holder.itemView.setOnClickListener {
            nextPage(item.ssid,item.bssid)
        }
        holder.binding.deviceName.text = item?.ssid
    }

    fun addItems(result: WlanNetworkListModel) {
        scanResult.data.clear()
        scanResult = result
        notifyDataSetChanged()
    }

}