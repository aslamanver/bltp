package com.aslam.bltp.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.aslam.bltp.bluetooth.BluetoothPrinter
import com.aslam.bltp.models.BluetoothDeviceData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BluetoothDeviceAdapter<VH : ViewHolder>(private val context: Context) : RecyclerView.Adapter<VH>() {

    abstract fun onDeviceBindViewHolder(holder: VH, position: Int, device: BluetoothDeviceData)

    override fun onBindViewHolder(holder: VH, position: Int) {
        val device = BluetoothPrinter.of(context).devices.entries.toList()[position].value
        onDeviceBindViewHolder(holder, position, device)
    }

    override fun getItemCount() = BluetoothPrinter.of(context).devices.entries.toList().size

    fun notifyDeviceMapChanged() {
        CoroutineScope(Dispatchers.Main).launch {
            notifyDataSetChanged()
        }
    }
}