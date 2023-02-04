package com.daozhao.hello.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.daozhao.hello.R

class Deeplink2Activity: AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deeplink2)

        getDataFromIntent()
    }

    fun getDataFromIntent() { // 比如点击通知栏消息后可以从Intent获取消息内容
        // 循环取值
        val bundle = intent.extras
        bundle?.keySet()?.forEach { key ->
            val content = bundle.getString(key)
            Log.i(TAG, "receive data from push, key = $key, content = $content")
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        getDataFromIntent()
    }

    companion object {
        private const val TAG: String = "Deeplink2Activity"
    }
}