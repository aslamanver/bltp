package com.aslam.bltp.bluetooth

import com.aslam.bltp.models.BluetoothDeviceData

interface BluetoothPrinterListener {
    fun onBluetoothUnavailable()
    fun onBluetoothOff()
    fun onPermissionError()
    fun onScanStarted()
    fun onRescanStarted()
    fun onDeviceAdded(bluetoothDeviceData: BluetoothDeviceData)
    fun onDeviceUpdated(bluetoothDeviceData: BluetoothDeviceData)
    fun onScanStopped()
    fun onPaired(bluetoothDeviceData: BluetoothDeviceData)
    fun onUnpaired(bluetoothDeviceData: BluetoothDeviceData)
    fun onDeviceConnected(bluetoothDeviceData: BluetoothDeviceData)
    fun onDeviceDisconnected(bluetoothDeviceData: BluetoothDeviceData)
    fun onReceive(bluetoothDeviceData: BluetoothDeviceData, numBytes: Int, buffer: ByteArray)
    fun onConnectError(bluetoothDeviceData: BluetoothDeviceData)
}