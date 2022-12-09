package com.aslam.bltp.bluetooth

object BluetoothPrinterCommands {

    private val ALIGNMENT_COMMAND = byteArrayOf(27, 97)
    val ALIGNMENT_RIGHT = ALIGNMENT_COMMAND.plus(2)
    val ALIGNMENT_LEFT = ALIGNMENT_COMMAND.plus(0)
    val ALIGNMENT_CENTER = ALIGNMENT_COMMAND.plus(1)

    private val FONT_WEIGHT_COMMAND = byteArrayOf(27, 69)
    val FONT_WEIGHT_BOLD = FONT_WEIGHT_COMMAND.plus(1)
    val FONT_WEIGHT_NORMAL = FONT_WEIGHT_COMMAND.plus(0)

    private val FONT_SIZE_COMMAND = byteArrayOf(29, 33)
    val FONT_SIZE_NORMAL = FONT_SIZE_COMMAND.plus(0x00)
    val FONT_SIZE_LARGE = FONT_SIZE_COMMAND.plus(0x10)

    private val UNDERLINE_MODE_COMMAND = byteArrayOf(27, 45)
    val UNDERLINED_MODE_ON = UNDERLINE_MODE_COMMAND.plus(1)
    val UNDERLINED_MODE_OFF = UNDERLINE_MODE_COMMAND.plus(0)

    private val LINE_SPACING_COMMAND = byteArrayOf(0x1B, 0x33)
    val LINE_SPACING_60 = LINE_SPACING_COMMAND.plus(60)
    val LINE_SPACING_30 = LINE_SPACING_COMMAND.plus(30)
    val LINE_SPACING_0 = LINE_SPACING_COMMAND.plus(0)
}