package com.aslam.bltp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

object BluetoothPrinterPrefs {

    private val defaultDeviceAddress: MutableLiveData<String> by lazy { MutableLiveData("") }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(this::class.java.name, Context.MODE_PRIVATE)
    }

    fun getDefaultDeviceAddress(context: Context): MutableLiveData<String> {

        if (defaultDeviceAddress.value == "") {
            val defaultValue = "DEFAULT"
            val storageValue = getPrefs(context).getString("DEFAULT_DEVICE_ADDRESS", defaultValue)
            defaultDeviceAddress.value = storageValue?.ifEmpty { defaultValue }
        }

        return defaultDeviceAddress
    }

    fun setDefaultDeviceAddress(context: Context, _deviceAddress: String) {
        defaultDeviceAddress.value = _deviceAddress
        getPrefs(context).edit().putString("DEFAULT_DEVICE_ADDRESS", defaultDeviceAddress.value).apply()
    }
}