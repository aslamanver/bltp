package com.aslam.bltp.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        BaseForegroundService.start(context, BLTPService::class.java)
    }
}