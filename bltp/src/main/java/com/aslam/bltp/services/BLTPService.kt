package com.aslam.bltp.services

import androidx.annotation.CallSuper
import com.aslam.bltp.R
import com.aslam.bltp.bluetooth.BluetoothPrinter

class BLTPService : BaseForegroundService() {

    companion object {
        const val REQUEST_CODE = 3005
    }

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        BluetoothPrinter.of(this).startScan()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        BluetoothPrinter.of(this).destroy()
    }

    override fun serviceBuilder(): ServiceBuilder {
        val serviceBuilder = ServiceBuilder(REQUEST_CODE, "${this::class.java.name}_Service_ID", "${this::class.java.name} Service Channel")
        val notification = createNotification(serviceBuilder, this::class.java.name, this::class.java.name, R.drawable.ic_notification, R.drawable.ic_notification)
        return serviceBuilder.build(notification)
    }
}