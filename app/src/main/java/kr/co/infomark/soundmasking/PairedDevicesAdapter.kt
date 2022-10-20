package kr.co.infomark.soundmasking

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kr.co.infomark.soundmasking.databinding.RowPairedDeviceItemBinding

class PairedDevicesAdapter(private val connect: (deviceToConnect: BluetoothDevice?) -> Unit) : RecyclerView.Adapter<PairedDevicesAdapter.DeviceViewHolder>() {

    private var devicesList: ArrayList<BluetoothDevice> = ArrayList()
    lateinit var rowPairedDeviceItemBinding : RowPairedDeviceItemBinding
    inner class DeviceViewHolder(val binding: RowPairedDeviceItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {

        rowPairedDeviceItemBinding =  DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.row_paired_device_item, parent, false)
        return DeviceViewHolder(rowPairedDeviceItemBinding)
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devicesList[position]

        if (ActivityCompat.checkSelfPermission(
                holder.itemView.context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        holder.binding.tvDeviceName.text = device.name
        holder.binding.tvDeviceAddress.text = device.address
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
        devicesList.add(device)
        notifyDataSetChanged()
    }
}