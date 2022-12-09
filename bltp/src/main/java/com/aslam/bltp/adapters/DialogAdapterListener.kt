package com.aslam.bltp.adapters

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import com.aslam.bltp.R
import com.aslam.bltp.bluetooth.BluetoothPrinter
import com.aslam.bltp.models.BluetoothDeviceData
import com.aslam.bltp.utils.defaultDeviceAddress
import com.aslam.bltp.utils.sendMessage
import com.aslam.bltp.utils.toPrinterByteArray

class DialogAdapterListener(private val context: Context) : AdapterListener {

    override fun onClick(device: BluetoothDeviceData) {

        val items: MutableSet<String> = mutableSetOf()

        if (context.defaultDeviceAddress.value == device.address) {
            items.add(context.getString(R.string.clear_default))
        } else {
            items.add(context.getString(R.string.set_as_default))
        }

        if (device.status == BluetoothDeviceData.Status.CONNECTED) {
            items.add(context.getString(R.string.print_test))
            items.add(context.getString(R.string.unpair))
        } else {
            items.add(context.getString(R.string.pair))
        }

        AlertDialog.Builder(context)
            .setTitle(device.name)
            // .setCancelable(false)
            .setItems(items.toTypedArray()) { _, which ->
                when (items.elementAt(which)) {
                    context.getString(R.string.set_as_default) -> context.defaultDeviceAddress = MutableLiveData(device.address)
                    context.getString(R.string.clear_default) -> {
                        context.defaultDeviceAddress = MutableLiveData("")
                    }
                    context.getString(R.string.pair), context.getString(R.string.unpair) -> {
                        BluetoothPrinter.switchBond(context, device)
                    }
                    context.getString(R.string.print_test) -> device.sendMessage("TEST\n".toPrinterByteArray())
                }
            }
            .create().show()
    }
}