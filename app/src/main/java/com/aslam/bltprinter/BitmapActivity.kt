package com.aslam.bltprinter

import android.os.Bundle
import com.aslam.bltprinter.databinding.ActivityBitmapBinding

class BitmapActivity(override val layoutId: Int = R.layout.activity_bitmap) : BaseActivity<ActivityBitmapBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.imageView.setImageBitmap(BitmapReceipt.generateReceipt(this))
    }
}
