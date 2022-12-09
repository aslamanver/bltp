@file:JvmName("BluetoothPrinterUtils")

package com.aslam.bltp.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslam.bltp.R
import com.aslam.bltp.adapters.BluetoothDeviceAdapter
import com.aslam.bltp.bluetooth.BluetoothPrinter
import com.aslam.bltp.bluetooth.BluetoothPrinterCommands
import com.aslam.bltp.models.BluetoothDeviceData
import java.io.IOException

fun BluetoothDevice.removeBond() {
    try {
        javaClass.getMethod("removeBond").invoke(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun String.toPrinterByteArray() = InputUtils.toPrinterByteArray(this)

fun Bitmap.toPrinterByteArray() = InputUtils.decodeBitmap(this)

fun Bitmap.replaceTransparentBackground(color: Int): Bitmap {
    val newBitmap = Bitmap.createBitmap(width, height, config)
    val canvas = Canvas(newBitmap)
    canvas.drawColor(color)
    canvas.drawBitmap(this, 0f, 0f, null)
    return newBitmap
}

@SuppressLint("MissingPermission")
fun BluetoothDevice.bondState(): BluetoothDeviceData.Status = when (this.bondState) {
    BluetoothDevice.BOND_BONDED -> BluetoothDeviceData.Status.PAIRED
    BluetoothDevice.BOND_BONDING -> BluetoothDeviceData.Status.PAIRING
    BluetoothDevice.BOND_NONE -> BluetoothDeviceData.Status.UNPAIRED
    else -> BluetoothDeviceData.Status.NONE
}

@SuppressLint("MissingPermission")
fun BluetoothDevice.isPrinter(): Boolean {
    val printerMask = 263808
    val fullCode = bluetoothClass.hashCode()
    return fullCode and printerMask == printerMask
}

fun Context.bluetoothPrinter(): Lazy<BluetoothPrinter> = lazy { BluetoothPrinter.of(this) }

var Context.defaultDeviceAddress: LiveData<String>
    get() = BluetoothPrinterPrefs.getDefaultDeviceAddress(this)
    set(value) {
        BluetoothPrinterPrefs.setDefaultDeviceAddress(this, value.value!!)
    }

fun RecyclerView.setBluetoothDeviceAdapter(context: Context, bluetoothDeviceAdapter: BluetoothDeviceAdapter<*>) {
    layoutManager = LinearLayoutManager(context)
    setHasFixedSize(true)
    adapter = bluetoothDeviceAdapter
}

fun BluetoothDeviceData.sendMessage(message: ByteArray): Boolean {
    if (status == BluetoothDeviceData.Status.CONNECTED) {
        try {
            connection?.outputStream?.write(message)
            return true
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
    return false
}

fun BluetoothDeviceData.sendMessage(vararg messages: ByteArray): Boolean {
    resetPrinterCommands()
    messages.forEach { message -> if (!sendMessage(message)) return false }
    resetPrinterCommands()
    return true
}

fun BluetoothDeviceData.resetPrinterCommands() {
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_LEFT)
    sendMessage(BluetoothPrinterCommands.FONT_SIZE_NORMAL)
    sendMessage(BluetoothPrinterCommands.FONT_WEIGHT_NORMAL)
    sendMessage(BluetoothPrinterCommands.LINE_SPACING_0)
    sendMessage(BluetoothPrinterCommands.UNDERLINED_MODE_OFF)
}

fun BluetoothDeviceData.sampleReceipt(context: Context) {

    val bitmapLogo = InputUtils.generateBitmapFromResource(context, R.drawable.print_logo)
    sendMessage(bitmapLogo.toPrinterByteArray()!!)

    var receiptText = "\n\n"
    receiptText += "Tillion Bites \n"
    receiptText += "Galle Road, Dehiwala \n\n"
    // receiptText += "          WELCOMES YOU \n"
    receiptText += "077 06 600 06 \n"
    receiptText += "info@tillion.lk \n"
    receiptText += "bites.tillion.lk \n\n"
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_CENTER, receiptText.toPrinterByteArray())

    receiptText = "Invoice No: 123456 \n"
    receiptText += "Date Time: 2021-01-01 12:00:00 \n"
    receiptText += "Customer Name: Aslam \n"
    receiptText += "Customer Mobile: 075XXXX081 \n"
    receiptText += "................................ \n"

    receiptText += "Nutella Milkshake \n"
    receiptText += "1 x 720.00 \n\n"
    receiptText += "Nutella Mocha \n"
    receiptText += "1 x 720.00 \n"

    receiptText += "................................ \n"
    receiptText += "Discount:   200.00 \n"
    receiptText += "Total:      1,200.00 \n"
    receiptText += "Card Payment \n"
    receiptText += "................................ \n"
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_LEFT, receiptText.toPrinterByteArray())

    receiptText = "Thank you and come again! \n\n\n"
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_CENTER, receiptText.toPrinterByteArray())
}

fun BluetoothDeviceData.sampleReceiptV2(context: Context) {

    val headerLogo = InputUtils.generateBitmapFromResource(context, R.drawable.cmb_logo_print_header)
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_CENTER, headerLogo.replaceTransparentBackground(Color.WHITE).toPrinterByteArray()!!)

    var receiptText = "\n"
    receiptText += "ABC Group Pvt Ltd \n"
    receiptText += "Galle Road, Dehiwala \n"
    receiptText += "DUPLICATE \n"
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_CENTER, receiptText.toPrinterByteArray())

    receiptText = "DATE/TIME: 2021-01-01 12:00:00 \n"
    receiptText += "MERCHANT ID: 14558856565556566 \n"
    receiptText += "TERMINAL ID: 1452625655 \n"
    receiptText += "BATCH NUM: 000003 \n"
    receiptText += "TXN METHOD: NFC \n"
    sendMessage(receiptText.toPrinterByteArray())

    receiptText = "SALE \n"
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_CENTER, BluetoothPrinterCommands.FONT_WEIGHT_BOLD, receiptText.toPrinterByteArray())

    receiptText = "CARD NO: 4424 13XX XXXX 4356 \n"
    receiptText += "EXP DATE: XX/XX \n"
    receiptText += "CARD TYPE ID: VISA \n"
    receiptText += "APP: VISA DEBIT \n"
    receiptText += "AID: A000000000000000031010 \n"
    receiptText += "APP CODE: 052558 \n"
    receiptText += "RRN NO: 0000004 \n"
    receiptText += "TRACK NUM: 000006 \n"
    sendMessage(receiptText.toPrinterByteArray())

    receiptText = "Total: LKR 1,200.00 \n"
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_CENTER, BluetoothPrinterCommands.FONT_WEIGHT_BOLD, receiptText.toPrinterByteArray())

    receiptText = "*** Signature not required *** \n"
    receiptText += "I agree to pay the above total amount according to the card issuer agreement \n"
    receiptText += "*** Customer Copy *** \n"
    receiptText += "Thank You \n"
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_CENTER, receiptText.toPrinterByteArray())

    val footerLogo = InputUtils.generateBitmapFromResource(context, R.drawable.cmb_logo_powered_by, height = 80)
    sendMessage(BluetoothPrinterCommands.ALIGNMENT_CENTER, footerLogo.replaceTransparentBackground(Color.WHITE).toPrinterByteArray()!!, "\n\n\n".toPrinterByteArray())
}