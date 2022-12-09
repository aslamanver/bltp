package com.aslam.bltprinter

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import androidx.core.graphics.scale
import com.aslam.bltp.bluetooth.BluetoothPrinter
import com.aslam.bltp.bluetooth.BluetoothPrinterListener
import com.aslam.bltp.models.BluetoothDeviceData
import com.aslam.bltp.services.BLTPService
import com.aslam.bltp.services.BaseForegroundService
import com.aslam.bltp.utils.*
import com.aslam.bltprinter.adapters.DeviceAdapter
import com.aslam.bltprinter.databinding.ActivityMainBinding
import com.aslam.bltprinter.utils.setTitleWithIcon
import com.aslam.bltprinter.utils.toLogcat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


@SuppressLint("MissingPermission")
class MainActivity(override val layoutId: Int = R.layout.activity_main) : BaseActivity<ActivityMainBinding>() {

    private lateinit var deviceAdapter: DeviceAdapter

    private val bluetoothPrinterListener = object : BluetoothPrinterListener {

        override fun onBluetoothUnavailable() {
            "onBluetoothUnavailable".toTextView()
        }

        override fun onBluetoothOff() {
            BluetoothPrinter.onBluetoothOffHandler(this@MainActivity)
        }

        override fun onPermissionError() {
            BluetoothPrinter.onPermissionErrorHandler(this@MainActivity)
        }

        override fun onScanStarted() {
            "onScanStarted".toTextView()
        }

        override fun onRescanStarted() {
            "onRescanStarted".toTextView()
        }

        override fun onDeviceAdded(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onDeviceAdded".toTextView()
            deviceAdapter.notifyDeviceMapChanged()
        }

        override fun onDeviceUpdated(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onDeviceUpdated".toTextView()
            deviceAdapter.notifyDeviceMapChanged()
        }

        override fun onScanStopped() {
            "onScanStopped".toTextView()
        }

        override fun onPaired(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onPaired".toTextView()
            deviceAdapter.notifyDeviceMapChanged()
        }

        override fun onUnpaired(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onUnpaired".toTextView()
            deviceAdapter.notifyDeviceMapChanged()
        }

        override fun onDeviceConnected(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onDeviceConnected".toTextView()
            deviceAdapter.notifyDeviceMapChanged()
            // BluetoothPrinter.of(this@MainActivity).sendMessage(bluetoothDeviceData, "onDeviceConnected\n\n\n".toPrinterByteArray())
        }

        override fun onDeviceDisconnected(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onDeviceDisconnected".toTextView()
            deviceAdapter.notifyDeviceMapChanged()
        }

        override fun onReceive(bluetoothDeviceData: BluetoothDeviceData, numBytes: Int, buffer: ByteArray) {
            "${bluetoothDeviceData.name}: onReceive".toTextView()
        }

        override fun onConnectError(bluetoothDeviceData: BluetoothDeviceData) {
            "${bluetoothDeviceData.name}: onConnectError".toTextView()
            deviceAdapter.notifyDeviceMapChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setTitleWithIcon(icon = com.aslam.bltp.R.drawable.ic_notification)

        BaseForegroundService.start(this, BLTPService::class.java)

        deviceAdapter = DeviceAdapter(this)

        defaultDeviceAddress.observe(this) { deviceAdapter.notifyDeviceMapChanged() }

        binding.recyclerView.setBluetoothDeviceAdapter(this, deviceAdapter)

        binding.btnPrint.setOnClickListener {
            BluetoothPrinter.getDefaultDevice(this)?.sampleReceiptV2(this)
        }
    }

    override fun onResume() {
        super.onResume()
        BluetoothPrinter.of(this).startScan(bluetoothPrinterListener)
    }

    override fun onPause() {
        super.onPause()
        BluetoothPrinter.of(this).stopScan(bluetoothPrinterListener)
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
