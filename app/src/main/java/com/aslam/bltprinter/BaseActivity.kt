package com.aslam.bltprinter

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import com.aslam.bltprinter.utils.resultLaunchers
import com.aslam.bltprinter.utils.viewBindings

abstract class BaseActivity<VB : ViewDataBinding?> : AppCompatActivity() {

    abstract val layoutId: Int
    protected val binding: VB by viewBindings()
    internal val resultLauncher: ActivityResultLauncher<Intent> by resultLaunchers()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding?.apply { this.lifecycleOwner = this@BaseActivity }
        resultLauncher.apply { "resultLauncher is registered ${binding!!::class.simpleName}" }
    }

    open fun onActivityResultData(activityRequestCode: Int, activityResult: ActivityResult) {
        activityResult.data?.let { "onActivityResult: ${activityResult.data}" }
    }
}