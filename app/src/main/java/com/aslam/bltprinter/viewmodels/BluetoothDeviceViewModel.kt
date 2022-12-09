package com.aslam.bltprinter.viewmodels

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothDeviceViewModel : ViewModel() {

    private val _list: MutableLiveData<List<BluetoothDevice>> by lazy {
        MutableLiveData<List<BluetoothDevice>>()
    }

    val list: MutableLiveData<List<BluetoothDevice>>
        get() = _list
}