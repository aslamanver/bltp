package com.aslam.bltp.adapters

import com.aslam.bltp.models.BluetoothDeviceData

interface AdapterListener {
    fun onClick(device: BluetoothDeviceData)
}