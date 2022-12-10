package com.aslam.bltp.ui

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.view.View
import com.aslam.bltp.R
import com.aslam.bltp.bluetooth.BluetoothPrinter
import com.aslam.bltp.bluetooth.BluetoothPrinterListener
import com.aslam.bltp.models.BluetoothDeviceData
import com.aslam.bltp.utils.*
import com.aslam.bltp.databinding.ActivityBltpBinding

@SuppressLint("MissingPermission")
class BluetoothPrinterActivity(override val layoutId: Int = R.layout.activity_bltp) : BaseActivity<ActivityBltpBinding>() {

    private val bluetoothPrinter: BluetoothPrinter by bluetoothPrinter()
    private lateinit var uiDeviceAdapter: UIDeviceAdapter

    private val bluetoothPrinterListener = object : BluetoothPrinterListener {

        override fun onBluetoothUnavailable() {
            "onBluetoothUnavailable".toTextView()
        }

        override fun onBluetoothOff() {
            BluetoothPrinter.onBluetoothOffHandler(this@BluetoothPrinterActivity)
        }

        override fun onPermissionError() {
            BluetoothPrinter.onPermissionErrorHandler(this@BluetoothPrinterActivity)
        }

        override fun onScanStarted() {
            "onScanStarted".toTextView()
        }

        override fun onRescanStarted() {
            "onRescanStarted".toTextView()
        }

        override fun onDeviceAdded(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onDeviceAdded".toTextView()
            uiDeviceAdapter.notifyDeviceMapChanged()
        }

        override fun onDeviceUpdated(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onDeviceUpdated".toTextView()
            uiDeviceAdapter.notifyDeviceMapChanged()
        }

        override fun onScanStopped() {
            "onScanStopped".toTextView()
        }

        override fun onPaired(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onPaired".toTextView()
            uiDeviceAdapter.notifyDeviceMapChanged()
        }

        override fun onUnpaired(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onUnpaired".toTextView()
            uiDeviceAdapter.notifyDeviceMapChanged()
        }

        override fun onDeviceConnected(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onDeviceConnected".toTextView()
            uiDeviceAdapter.notifyDeviceMapChanged()
            // BluetoothPrinter.of(this@MainActivity).sendMessage(bluetoothDeviceData, "onDeviceConnected\n\n\n".toPrinterByteArray())
        }

        override fun onDeviceDisconnected(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onDeviceDisconnected".toTextView()
            uiDeviceAdapter.notifyDeviceMapChanged()
        }

        override fun onReceive(bluetoothDeviceData: BluetoothDeviceData, numBytes: Int, buffer: ByteArray) {
            "${bluetoothDeviceData.name}: onReceive".toTextView()
        }

        override fun onConnectError(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onConnectError".toTextView()
            uiDeviceAdapter.notifyDeviceMapChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setTitleWithIcon(icon = com.aslam.bltp.R.drawable.ic_notification)

        // BaseForegroundService.start(this, BLTPService::class.java)

        uiDeviceAdapter = UIDeviceAdapter(this)

        defaultDeviceAddress.observe(this) { uiDeviceAdapter.notifyDeviceMapChanged() }

        binding.recyclerView.setBluetoothDeviceAdapter(this, uiDeviceAdapter)

        binding.btnPrint.setOnClickListener {
            bluetoothPrinter.getDefaultDevice()?.sampleReceiptV2(this)
        }

        if (!bluetoothPrinter.debugMode) {
            binding.debugLayout.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        bluetoothPrinter.startScan(bluetoothPrinterListener)
    }

    override fun onPause() {
        super.onPause()
        bluetoothPrinter.stopScan(bluetoothPrinterListener)
    }

    // override fun onDestroy() {
    //     super.onDestroy()
    //     BluetoothPrinter.of(this).destroy()
    // }

    fun String.toTextView() {
        runOnUiThread { binding.txtStatus.text = this }
        this.toLogcat()
    }
}
