package kr.co.infomark.soundmasking.intro.adapter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.SelectDeviceItemBinding

class PairedDevicesAdapter(private val connect: (deviceToConnect: BluetoothDevice?) -> Unit) : RecyclerView.Adapter<PairedDevicesAdapter.DeviceViewHolder>() {

    private var devicesList: ArrayList<BluetoothDevice> = ArrayList()
    lateinit var selectDeviceItemBinding : SelectDeviceItemBinding
    inner class DeviceViewHolder(val binding: SelectDeviceItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {

        selectDeviceItemBinding =  DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.select_device_item, parent, false)
        return DeviceViewHolder(selectDeviceItemBinding)
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devicesList[position]


        holder.binding.deviceName.text = device.name
        holder.binding.deviceAddress.text = device.address
        holder.itemView.setOnClickListener {
            connect(device)
        }
    }

    fun addItems(list: MutableSet<BluetoothDevice>) {
        devicesList.clear()
        devicesList.addAll(list)
        notifyDataSetChanged()
    }

    fun addItem(device: BluetoothDevice) {
        var duplicate = false
        devicesList.forEach { it
               if(it.address == device.address){
                   duplicate = true
               }
        }
        if(!duplicate){
            devicesList.add(device)
            notifyDataSetChanged()
        }

    }
}