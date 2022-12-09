package com.aslam.bltprinter.bluetooth

import android.bluetooth.BluetoothDevice

interface BluetoothPrinterListener {
    fun onBluetoothUnavailable()
    fun onBluetoothOff()
    fun onPermissionError()
    fun onScanStarted()
    fun onRescanStarted()
    fun onDeviceAdded(bluetoothDevice: BluetoothDevice)
    fun onDeviceUpdated(bluetoothDevice: BluetoothDevice)
    fun onScanStopped()
    fun onPaired(bluetoothDevice: BluetoothDevice)
    fun onUnpaired(bluetoothDevice: BluetoothDevice)
    fun onDeviceConnected(bluetoothDevice: BluetoothDevice)
    fun onDeviceDisconnected(bluetoothDevice: BluetoothDevice)
    fun onReceive(numBytes: Int, buffer: ByteArray)
}