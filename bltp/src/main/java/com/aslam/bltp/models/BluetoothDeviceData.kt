package com.aslam.bltp.models

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.aslam.bltp.bluetooth.BluetoothPrinter
import com.aslam.bltp.utils.bondState

@SuppressLint("MissingPermission")
class BluetoothDeviceData(var device: BluetoothDevice) {

    enum class Status {
        CONNECTED, DISCONNECTED, CONNECTING, PAIRING, PAIRED, UNPAIRED, NONE, SOCKET_ERROR, RFCOMM_ERROR
    }

    val name: String = device.name
    var address: String = device.address
    var status: Status = device.bondState()

    var connection: BluetoothPrinter.ConnectThread? = null
}