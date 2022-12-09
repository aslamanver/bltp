package com.aslam.bltp.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aslam.bltp.R
import com.aslam.bltp.models.BluetoothDeviceData
import com.aslam.bltp.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@SuppressLint("MissingPermission")
class BluetoothPrinter private constructor(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter = ContextCompat.getSystemService(context, BluetoothManager::class.java)!!.adapter

    private val deviceMap: MutableMap<String, BluetoothDeviceData> = mutableMapOf()
    val devices: MutableMap<String, BluetoothDeviceData> = deviceMap

    private var listeners: MutableSet<BluetoothPrinterListener> = mutableSetOf()
    private val defaultListener = DefaultBluetoothPrinterListener(listeners)
    private var isRegistered = false

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            // intent.action?.toLogcat()

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == intent.action) {
                defaultListener.onRescanStarted()
                bluetoothAdapter.startDiscovery()
                return
            }

            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

            if (device != null && device.name != null) {

                var bluetoothDeviceData = deviceMap[device.address]

                if (bluetoothDeviceData == null) {

                    bluetoothDeviceData = BluetoothDeviceData(device)
                    deviceMap[device.address] = bluetoothDeviceData
                    defaultListener.onDeviceAdded(bluetoothDeviceData)

                } else {

                    deviceMap[device.address]?.device = device
                    defaultListener.onDeviceUpdated(bluetoothDeviceData)
                }

                // TODO: bluetoothDeviceData.status

                if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    connectOnPair(bluetoothDeviceData)
                }

                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {

                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {

                        connectOnPair(bluetoothDeviceData)

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {

                        disconnectOnUnpair(bluetoothDeviceData)
                    }

                    // "ACTION_BOND_STATE_CHANGED state:$state prevState:$prevState".toLogcat(context)
                }
            }
        }
    }

    private fun isPermissionsGranted(): Boolean {

        if (Companion.isPermissionsGranted(context)) {
            if (bluetoothAdapter.isEnabled) {
                return true
            } else {
                defaultListener.onBluetoothOff()
            }
        } else {
            defaultListener.onPermissionError()
        }

        return false
    }

    private fun connectOnPair(device: BluetoothDeviceData) {

        if (arrayListOf(
                BluetoothDeviceData.Status.CONNECTING,
                BluetoothDeviceData.Status.CONNECTED
            ).contains(device.status)
        ) {
            return
        }

        device.status = device.device.bondState()
        defaultListener.onPaired(device)

        if (context.defaultDeviceAddress.value == device.address) {
            device.connection = ConnectThread(device)
            device.connection?.start()
        }
    }

    private fun disconnectOnUnpair(device: BluetoothDeviceData) {
        defaultListener.onUnpaired(device)
        device.connection?.close()
        device.connection = null
        device.status = BluetoothDeviceData.Status.DISCONNECTED
    }

    private fun registerReceiver() {

        if (!isRegistered) {

            val filter = IntentFilter()
            filter.addAction(BluetoothDevice.ACTION_FOUND)
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            // filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            // filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            context.registerReceiver(mReceiver, filter)

            isRegistered = true
        }
    }

    private fun unregisterReceiver() {
        try {
            isRegistered = false
            context.unregisterReceiver(mReceiver)
        } catch (ex: java.lang.IllegalArgumentException) {
            // ex.printStackTrace()
        }
    }

    private fun addPairedDevices() {

        if (bluetoothAdapter.bondedDevices.isNotEmpty()) {

            for (device in bluetoothAdapter.bondedDevices) {

                if (deviceMap[device.address] == null) {

                    deviceMap[device.address] = BluetoothDeviceData(device)
                    defaultListener.onDeviceAdded(deviceMap[device.address]!!)

                    // Add fake devices
                    // for (i in 1..10){
                    //     val fake = device.address + (100..200).random()
                    //     deviceMap[fake] = BluetoothDeviceData(device)
                    //     defaultListener.onDeviceAdded(deviceMap[fake]!!)
                    // }
                }

                // if (device.isPrinter()) {
                //     connectOnPair(deviceMap[device.address]!!)
                // }

                // if (context.defaultDeviceAddress.value == device.address) {
                //     connectOnPair(deviceMap[device.address]!!)
                // }

                connectOnPair(deviceMap[device.address]!!)
            }
        }
    }

    fun startScan(bluetoothPrinterListener: BluetoothPrinterListener? = null) {

        bluetoothPrinterListener?.let { addListener(it) }

        if (!isPermissionsGranted()) return

        addPairedDevices()
        registerReceiver()

        bluetoothAdapter.startDiscovery()
        defaultListener.onScanStarted()
    }

    fun stopScan(bluetoothPrinterListener: BluetoothPrinterListener? = null) {

        if (!isPermissionsGranted()) return

        unregisterReceiver()

        bluetoothAdapter.cancelDiscovery()
        defaultListener.onScanStopped()

        bluetoothPrinterListener?.let { removeListener(it) }
    }

    fun destroy() {
        closeConnections()
        stopScan()
        removeListeners()
    }

    fun addListener(bluetoothPrinterListener: BluetoothPrinterListener) {
        listeners.add(bluetoothPrinterListener)
    }

    fun removeListener(bluetoothPrinterListener: BluetoothPrinterListener) {
        listeners.remove(bluetoothPrinterListener)
    }

    private fun removeListeners() {
        listeners.clear()
    }

    private fun closeConnections() {
        deviceMap.forEach { (_, device) ->
            device.connection?.close()
        }
    }

    inner class ConnectThread(val bluetoothDeviceData: BluetoothDeviceData) : Thread() {

        private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        private lateinit var bluetoothSocket: BluetoothSocket
        lateinit var inputStream: InputStream
        lateinit var outputStream: OutputStream
        private lateinit var receiveThread: ReceiveThread

        init {
            try {
                bluetoothDeviceData.status = BluetoothDeviceData.Status.CONNECTING
                bluetoothSocket = bluetoothDeviceData.device.createRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                e.printStackTrace()
                bluetoothDeviceData.status = BluetoothDeviceData.Status.RFCOMM_ERROR
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

                bluetoothDeviceData.status = BluetoothDeviceData.Status.CONNECTED
                defaultListener.onDeviceConnected(bluetoothDeviceData)

                // Thread {
                //     while (true) {
                //         sendMessage(bluetoothDevice, "onDeviceConnected".toPrinterByteArray())
                //         sleep(500)
                //     }
                // }.start()

                // var text = ""
                // repeat(2) {
                //     text = "$text $it \n"
                // }

                // sendMessage(bluetoothDevice, text.toPrinterByteArray())

            } catch (e: IOException) {
                e.printStackTrace()
                bluetoothSocket.close()
                bluetoothDeviceData.status = BluetoothDeviceData.Status.SOCKET_ERROR
                defaultListener.onConnectError(bluetoothDeviceData)
            }
        }

        fun close() {

            if (bluetoothDeviceData.status == BluetoothDeviceData.Status.CONNECTED) {
                receiveThread.close()
                bluetoothSocket.close()
            }

            defaultListener.onDeviceDisconnected(bluetoothDeviceData)
            interrupt()
        }
    }

    private inner class ReceiveThread(private val connectThread: ConnectThread) : Thread(), Runnable {

        private val buffer: ByteArray = ByteArray(1024)

        override fun run() {

            var numBytes: Int

            while (true) {

                numBytes = try {
                    connectThread.inputStream.read(buffer)
                } catch (e: IOException) {
                    e.printStackTrace()
                    disconnectOnUnpair(connectThread.bluetoothDeviceData)
                    break
                }

                defaultListener.onReceive(connectThread.bluetoothDeviceData, numBytes, buffer)
            }
        }

        fun close() {
            interrupt()
        }
    }

    companion object {

        private var bluetoothPrinter: BluetoothPrinter? = null

        fun of(context: Context): BluetoothPrinter {

            if (bluetoothPrinter == null) {
                bluetoothPrinter = BluetoothPrinter(context)
            }

            return bluetoothPrinter!!
        }

        private fun isPermissionsGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
        }

        fun onPermissionErrorHandler(activity: Activity) {

            if (!isPermissionsGranted(activity)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    val permissions = mutableSetOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
                    }

                    ActivityCompat.requestPermissions(
                        activity, permissions.toTypedArray(), 600
                    )
                }
            }
        }

        fun onBluetoothOffHandler(activity: Activity) {
            activity.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 200)
        }

        fun switchBond(context: Context, bluetoothDeviceData: BluetoothDeviceData) {
            if (bluetoothDeviceData.device.bondState != BluetoothDevice.BOND_BONDED) {
                bluetoothDeviceData.device.createBond()
                // bluetoothDeviceData.status = BluetoothDeviceData.Status.PAIRING
            } else if (bluetoothDeviceData.device.bondState == BluetoothDevice.BOND_BONDED) {
                bluetoothDeviceData.device.removeBond()
                // of(context).deviceMap[bluetoothDeviceData.device.address]?.connection?.close()
            }
        }

        fun getDefaultDevice(context: Context) = of(context).deviceMap[context.defaultDeviceAddress.value]
    }
}