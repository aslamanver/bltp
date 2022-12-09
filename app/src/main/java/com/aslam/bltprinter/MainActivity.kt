package com.aslam.bltprinter

import android.content.Intent
import android.os.Bundle
import com.aslam.bltp.ui.BaseActivity
import com.aslam.bltp.ui.BluetoothPrinterActivity
import com.aslam.bltprinter.databinding.ActivityMainBinding

class MainActivity(override val layoutId: Int = R.layout.activity_main) : BaseActivity<ActivityMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, BluetoothPrinterActivity::class.java))
    }
}
