package com.aslam.bltprinter.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aslam.bltprinter.utils.removeBond
import com.aslam.bltprinter.utils.toLogcat
import com.aslam.bltprinter.utils.toPrinterByteArray
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@SuppressLint("MissingPermission")
class BluetoothPrinter private constructor(private val context: Context) {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()
    var listener: BluetoothPrinterListener? = null
    private val connectThreads: MutableMap<String, ConnectThread> = mutableMapOf()
    private var isRegistered = false

    init {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            val bluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java)

            if (bluetoothManager != null) {

                bluetoothAdapter = bluetoothManager.adapter

            } else if (!bluetoothAdapter.isEnabled) {
                listener?.onBluetoothOff()
            } else {
                listener?.onBluetoothUnavailable()
            }

        } else {
            listener?.onPermissionError()
        }
    }

    companion object {

        private var bluetoothPrinter: BluetoothPrinter? = null

        fun of(context: Context): BluetoothPrinter {

            if (bluetoothPrinter == null) {

                bluetoothPrinter = BluetoothPrinter(context)

            } else if (context != bluetoothPrinter?.context) {

                bluetoothPrinter?.stopScan()
                bluetoothPrinter = BluetoothPrinter(context)
            }

            return bluetoothPrinter!!
        }

        fun onPermissionErrorHandler(activity: Activity, requestPermissions: Boolean = true): Boolean {

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (requestPermissions) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        ActivityCompat.requestPermissions(
                            activity, arrayOf(
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ), 600
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(
                                activity, arrayOf(
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.BLUETOOTH_SCAN,
                                ), 600
                            )
                        }
                    }
                }

                return false
            }
            return true
        }

        fun onBluetoothOffHandler(activity: Activity) {
            activity.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 200)
        }

        fun switch(bluetoothDevice: BluetoothDevice) {
            if (bluetoothDevice.bondState != BluetoothDevice.BOND_BONDED) {
                bluetoothDevice.createBond()
            } else if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
                bluetoothDevice.removeBond()
            }
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            // intent.action?.toLogcat()

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == intent.action) {
                listener?.onRescanStarted()
                bluetoothAdapter.startDiscovery()
                return
            }

            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

            if (device != null && device.name != null) {

                val index = deviceList.indexOf(device)

                if (index == -1) {
                    deviceList.add(device)
                    listener?.onDeviceAdded(device)
                } else {
                    deviceList[index] = device
                    listener?.onDeviceUpdated(device)
                }

                if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    if (connectThreads[device.address] == null) {
                        connectOnPair(device)
                    }
                }

                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {

                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {

                        connectOnPair(device)

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {

                        disconnectOnUnpair(device)
                    }

                    // "ACTION_BOND_STATE_CHANGED state:$state prevState:$prevState".toLogcat(context)
                }
            }
        }
    }

    private fun connectOnPair(device: BluetoothDevice) {
        listener?.onPaired(device)
        connectThreads[device.address] = ConnectThread(device)
        connectThreads[device.address]?.start()
    }

    private fun disconnectOnUnpair(device: BluetoothDevice) {
        listener?.onUnpaired(device)
        connectThreads[device.address]?.close()
        connectThreads.remove(device.address)
    }

    private fun registerReceiver() {

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        // filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        // filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        context.registerReceiver(mReceiver, filter)

        isRegistered = true
    }

    fun startScan() {

        if (!isRegistered) {
            registerReceiver()
        }

        bluetoothAdapter.startDiscovery()
        listener?.onScanStarted()
    }

    fun stopScan() {

        try {
            isRegistered = false
            context.unregisterReceiver(mReceiver)
        } catch (ex: java.lang.IllegalArgumentException) {
            // ex.printStackTrace()
        }

        bluetoothAdapter.cancelDiscovery()
        listener?.onScanStopped()
    }

    fun destroy() {

        connectThreads.forEach {
            it.value.close()
        }

        stopScan()
    }

    fun sendMessage(bluetoothDevice: BluetoothDevice, msg: ByteArray?) {
        try {
            val connectThread = connectThreads[bluetoothDevice.address]
            connectThread?.outputStream?.write(msg)
        } catch (e: IOException) {
            e.printStackTrace()
            "sendMessage:".toLogcat()
        }
    }

    inner class ConnectThread(val bluetoothDevice: BluetoothDevice) : Thread() {

        private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        private lateinit var bluetoothSocket: BluetoothSocket
        lateinit var inputStream: InputStream
        lateinit var outputStream: OutputStream
        lateinit var receiveThread: ReceiveThread

        init {
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun run() {

            // unregisterReceiver(mReceiver)
            // mBluetoothAdapter?.cancelDiscovery()

            try {

                bluetoothSocket.connect()

                outputStream = bluetoothSocket.outputStream
                inputStream = bluetoothSocket.inputStream

                receiveThread = ReceiveThread(this@ConnectThread)
                receiveThread.start()

                listener?.onDeviceConnected(bluetoothDevice)

                // Thread {
                //     while (true) {
                //         sendMessage(bluetoothDevice, "onDeviceConnected".toPrinterByteArray())
                //         sleep(500)
                //     }
                // }.start()

                var text = ""
                repeat(2) {
                    text = "$text $it \n"
                }

                sendMessage(bluetoothDevice, text.toPrinterByteArray())

            } catch (e: IOException) {
                bluetoothSocket.close()
                e.printStackTrace()
            }
        }

        fun close() {
            receiveThread.close()
            bluetoothSocket.close()
            listener?.onDeviceDisconnected(bluetoothDevice)
            interrupt()
        }
    }

    inner class ReceiveThread(private val connectThread: ConnectThread) : Thread(), Runnable {

        private val buffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int
            while (true) {
                numBytes = try {
                    connectThread.inputStream.read(buffer)
                } catch (e: IOException) {
                    e.printStackTrace()
                    disconnectOnUnpair(connectThread.bluetoothDevice)
                    break
                }
                listener?.onReceive(numBytes, buffer)
            }
        }

        fun close() {
            interrupt()
        }
    }
}
