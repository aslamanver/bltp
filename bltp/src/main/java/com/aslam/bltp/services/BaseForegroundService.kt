package com.aslam.bltp.services;

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

abstract class BaseForegroundService : Service() {

    // protected int REQUEST_CODE, NOTIFICATION_ID = 3000;
    // protected String CHANNEL_ID = "ForegroundService_ID";
    // protected String CHANNEL_NAME = "ForegroundService Channel";

    private lateinit var serviceBuilder: ServiceBuilder

    protected abstract fun serviceBuilder(): ServiceBuilder

    protected inner class ServiceBuilder(val requestCode: Int, val channelId: String, val channelName: String) {

        var notification: Notification? = null

        fun build(notification: Notification?): ServiceBuilder {
            this.notification = notification
            return this
        }
    }

    protected val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: BaseForegroundService
            get() = this@BaseForegroundService
    }

    protected var mNotificationManager: NotificationManager? = null
    protected var mNotificationBuilder: NotificationCompat.Builder? = null

    override fun onCreate() {
        super.onCreate()
        serviceBuilder = serviceBuilder()
        startForeground(serviceBuilder.requestCode, serviceBuilder.notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    protected fun createNotification(serviceBuilder: ServiceBuilder, title: String, message: String, smallIcon: Int?, bigIcon: Int?, intentClass: Class<*>? = null): Notification {

        if (mNotificationManager == null) {
            mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }

        if (mNotificationBuilder == null) {
            mNotificationBuilder = NotificationCompat.Builder(this, serviceBuilder.channelId)
        }

        mNotificationBuilder!!.setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)

        if (smallIcon != null) {
            mNotificationBuilder!!.setSmallIcon(smallIcon)
        }

        if (bigIcon != null) {
            mNotificationBuilder!!.setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, bigIcon), 128, 128, true))
        }

        if (intentClass != null) {
            val notificationIntent = Intent(this, intentClass)
            val pendingIntent = PendingIntent.getActivity(this, serviceBuilder.requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            mNotificationBuilder!!.setContentIntent(pendingIntent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(serviceBuilder.channelId, serviceBuilder.channelName, NotificationManager.IMPORTANCE_LOW)
            channel.setShowBadge(false)
            channel.importance = NotificationManager.IMPORTANCE_LOW
            mNotificationManager!!.createNotificationChannel(channel)
        }

        return mNotificationBuilder!!.build()
    }

    companion object {

        fun start(context: Context, serviceClass: Class<out BaseForegroundService>) {
            ContextCompat.startForegroundService(context, Intent(context, serviceClass))
        }

        fun stop(context: Context, serviceClass: Class<out BaseForegroundService>) {
            context.stopService(Intent(context, serviceClass))
        }

        fun bindService(context: Context, serviceClass: Class<out BaseForegroundService>, connection: ServiceConnection) {
            context.bindService(Intent(context, serviceClass), connection, BIND_AUTO_CREATE)
        }

        fun unbindService(context: Context, connection: ServiceConnection) {
            context.unbindService(connection)
        }

        fun from(service: IBinder): BaseForegroundService {
            val binder = service as LocalBinder
            return binder.service
        }
    }
}