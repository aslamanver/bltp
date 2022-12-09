package com.aslam.bltprinter

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslam.bltprinter.adapters.DeviceAdapter
import com.aslam.bltprinter.databinding.ActivityMainBinding
import com.aslam.bltprinter.utils.toLogcat
import com.aslam.bltprinter.utils.toPrinterByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.*


class MainActivityV1(override val layoutId: Int = R.layout.activity_main) : BaseActivity<ActivityMainBinding>() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var deviceAdapter: DeviceAdapter
    private var input: InputStream? = null
    private var out: OutputStream? = null
    private var connectThread: ConnectThread? = null
    private val buffer: ByteArray = ByteArray(1024)

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == intent.action) {
                binding.txtStatus.text = "Rescanning..."
                mBluetoothAdapter?.startDiscovery()
                return
            }

            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

            if (device != null && device.name != null) {
                addDevice(device)
                "${device.name} bond is ${device.bondState == BluetoothDevice.BOND_BONDED}".toLogcat()
            }
        }
    }

    @SuppressLint("MissingPermission")
    inner class ConnectThread(device: BluetoothDevice) : Thread() {

        lateinit var bluetoothSocket: BluetoothSocket
        private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        init {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun run() {
            unregisterReceiver(mReceiver)
            mBluetoothAdapter?.cancelDiscovery()
            try {
                bluetoothSocket.connect()
                out = bluetoothSocket.outputStream
                input = bluetoothSocket.inputStream
                ReceiveThread().start()
                "onDeviceConnected".toLogcat(applicationContext)
            } catch (e: IOException) {
                bluetoothSocket.close()
                e.printStackTrace()
            }
        }
    }

    inner class ReceiveThread : Thread(), Runnable {
        override fun run() {
            var numBytes: Int
            while (true) {
                numBytes = try {
                    input?.read(buffer)!!
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
                "onReceive".toLogcat()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        deviceAdapter = DeviceAdapter(object : DeviceAdapter.OnClickListener {
            @SuppressLint("MissingPermission")
            override fun onClick(device: BluetoothDevice) {
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    device.createBond()
                } else if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    // device.removeBond()
                    if (connectThread == null) {
                        connectThread = ConnectThread(device)
                        connectThread?.start()
                    } else {
                        printIt()
                    }
                }
            }
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = deviceAdapter

        val bluetoothManager: BluetoothManager? = ContextCompat.getSystemService(this, BluetoothManager::class.java)
        mBluetoothAdapter = bluetoothManager?.adapter
    }

    override fun onResume() {

        super.onResume()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 600
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                        ), 600
                    )
                }
            }
            return
        }

        if (mBluetoothAdapter?.isEnabled == false) {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 200)
            return
        }

        startScan()
    }

    override fun onPause() {
        super.onPause()
        if (mReceiver.isOrderedBroadcast) {
            unregisterReceiver(mReceiver)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {

        binding.txtStatus.text = "Scanning..."

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        // filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        // filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        registerReceiver(mReceiver, filter)

        mBluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    private fun addDevice(device: BluetoothDevice) {

        binding.txtStatus.text = "Device added: " + device.name

        val index = deviceAdapter.deviceList.indexOf(device)

        if (index == -1) {
            deviceAdapter.deviceList.add(device)
            deviceAdapter.notifyDataSetChanged()
        } else {
            deviceAdapter.deviceList[index] = device
            deviceAdapter.notifyDataSetChanged()
        }

        /*
        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {

            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
            val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

            if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                //
            } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                //
            }
        }
        */
    }

    fun sendMessage(msg: ByteArray?) {
        try {
            out!!.write(msg)
        } catch (e: IOException) {
            e.printStackTrace()
            "sendMessage: DeviceConnected".toLogcat()
        }
    }

    fun printIt() {

        CoroutineScope(Dispatchers.IO).launch {

            sendMessage(getBitmap("printBitmap", 1000)?.toPrinterByteArray())

            sendMessage("SOME\n".toPrinterByteArray())
            sendMessage("SOME\n".toPrinterByteArray())
            sendMessage("SOME\n".toPrinterByteArray())
            sendMessage("\n\n\n".toPrinterByteArray())
        }
    }

    private fun getBitmap(name: String, height: Int): Bitmap? {
        var bitmap = Bitmap.createBitmap(400, height, Bitmap.Config.ARGB_8888)
        var bitmapConfig = bitmap.config
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888
        }
        bitmap = bitmap.copy(bitmapConfig, true)
        bitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        paint.textSize = 30f
        var yposition = 30
        val space = 40
        var x = 0
        while (yposition < height + space) {
            canvas.drawText(name + " සුබ දවසක් " + (x + 1), 5f, yposition.toFloat(), paint)
            yposition += space
            x++
        }
        return bitmap
    }
}
