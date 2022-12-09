package com.aslam.bltp.bluetooth

import com.aslam.bltp.models.BluetoothDeviceData

class DefaultBluetoothPrinterListener(private val listeners: Set<BluetoothPrinterListener>) : BluetoothPrinterListener {

    override fun onBluetoothUnavailable() {
        listeners.forEach { it.onBluetoothUnavailable() }
    }

    override fun onBluetoothOff() {
        listeners.forEach { it.onBluetoothOff() }
    }

    override fun onPermissionError() {
        listeners.forEach { it.onPermissionError() }
    }

    override fun onScanStarted() {
        listeners.forEach { it.onScanStarted() }
    }

    override fun onRescanStarted() {
        listeners.forEach { it.onRescanStarted() }
    }

    override fun onDeviceAdded(bluetoothDeviceData: BluetoothDeviceData) {
        listeners.forEach { it.onDeviceAdded(bluetoothDeviceData) }
    }

    override fun onDeviceUpdated(bluetoothDeviceData: BluetoothDeviceData) {
        listeners.forEach { it.onDeviceUpdated(bluetoothDeviceData) }
    }

    override fun onScanStopped() {
        listeners.forEach { it.onScanStopped() }
    }

    override fun onPaired(bluetoothDeviceData: BluetoothDeviceData) {
        listeners.forEach { it.onPaired(bluetoothDeviceData) }
    }

    override fun onUnpaired(bluetoothDeviceData: BluetoothDeviceData) {
        listeners.forEach { it.onUnpaired(bluetoothDeviceData) }
    }

    override fun onDeviceConnected(bluetoothDeviceData: BluetoothDeviceData) {
        listeners.forEach { it.onDeviceConnected(bluetoothDeviceData) }
    }

    override fun onDeviceDisconnected(bluetoothDeviceData: BluetoothDeviceData) {
        listeners.forEach { it.onDeviceDisconnected(bluetoothDeviceData) }
    }

    override fun onReceive(bluetoothDeviceData: BluetoothDeviceData, numBytes: Int, buffer: ByteArray) {
        listeners.forEach { it.onReceive(bluetoothDeviceData, numBytes, buffer) }
    }

    override fun onConnectError(bluetoothDeviceData: BluetoothDeviceData) {
        listeners.forEach { it.onConnectError(bluetoothDeviceData) }
    }
}