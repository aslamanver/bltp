package com.aslam.bltp.ui

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

const val REQUEST_CODE = 2010

fun BaseActivity<*>.resultLaunchers(): Lazy<ActivityResultLauncher<Intent>> = lazy {
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { onActivityResultData(REQUEST_CODE, it) }
}

fun <B> BaseActivity<*>.viewBindings(): Lazy<B> = lazy { DataBindingUtil.setContentView(this, layoutId) }

fun String.toLogcat() = Log.e("MY-TAG", this)

fun String.toLogcat(context: Context, length: Int = Toast.LENGTH_SHORT) {
    this.toLogcat()
    Handler(context.mainLooper).post {
        Toast.makeText(context, this, length).show()
    }
}

fun AppCompatActivity.setTitleWithIcon(titleText: String = "Bluetooth Printer", icon: Int) {
    supportActionBar?.apply {
        title = "\t${titleText}"
        setIcon(icon)
        setDisplayUseLogoEnabled(true)
        setDisplayShowHomeEnabled(true)
    }
}