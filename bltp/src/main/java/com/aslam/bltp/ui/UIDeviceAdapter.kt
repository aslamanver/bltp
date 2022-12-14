package com.aslam.bltp.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aslam.bltp.adapters.AdapterListener
import com.aslam.bltp.adapters.BluetoothDeviceAdapter
import com.aslam.bltp.adapters.DialogAdapterListener
import com.aslam.bltp.models.BluetoothDeviceData
import com.aslam.bltp.utils.defaultDeviceAddress
import com.aslam.bltp.R
import com.aslam.bltp.databinding.DeviceLayoutRowBltpBinding

class UIDeviceAdapter(private val context: Context, private val adapterListener: AdapterListener = DialogAdapterListener(context)) : BluetoothDeviceAdapter<UIDeviceAdapter.ViewHolder>(context) {

    class ViewHolder(val binding: DeviceLayoutRowBltpBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DeviceLayoutRowBltpBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onDeviceBindViewHolder(holder: ViewHolder, position: Int, device: BluetoothDeviceData) {

        holder.binding.txtName.text = device.name
        holder.binding.txtAddress.text = "${device.address} ${device.status}"

        holder.binding.mainLayout.setOnClickListener {
            adapterListener.onClick(device)
        }

        holder.binding.mainLayout.background = ContextCompat.getDrawable(
            context,
            if (context.defaultDeviceAddress.value == device.address) R.drawable.ripple_selected else R.drawable.ripple
        )
    }
}