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

class WifiListAdapter() : RecyclerView.Adapter<WifiListAdapter.WifiItemViewHolder>() {

    private var scanResults: ArrayList<ScanResult> = ArrayList()
    lateinit var selectDeviceItemBinding : SelectWifiItemBinding
    inner class WifiItemViewHolder(val binding: SelectWifiItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiItemViewHolder {

        selectDeviceItemBinding =  DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.select_wifi_item, parent, false)
        return WifiItemViewHolder(selectDeviceItemBinding)
    }

    override fun getItemCount(): Int {
        return scanResults.size
    }

    override fun onBindViewHolder(holder: WifiItemViewHolder, position: Int) {
        val item = scanResults[position]

        if (ActivityCompat.checkSelfPermission(
                holder.itemView.context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        holder.binding.deviceName.text = item.SSID
    }

    fun addItems(list: MutableList<ScanResult>) {
        scanResults.clear()
        scanResults.addAll(list)
        notifyDataSetChanged()
    }

    fun addItem(scanResult: ScanResult) {
        scanResults.add(scanResult)
        notifyDataSetChanged()
    }
}