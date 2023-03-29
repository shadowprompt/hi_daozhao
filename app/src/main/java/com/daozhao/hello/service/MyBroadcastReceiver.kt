package com.daozhao.hello.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.v("dimos", "MyBroadcastReceiver")
        // 启动时注册服务
        if (intent.action == ACTION) {
            val service = Intent(context, MyService::class.java)
            context.startService(service)
        }

        StringBuilder().apply {
            append("Action: ${intent.action}\n")
            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            toString().also { log ->
                Log.d(TAG, log)
                Toast.makeText(context, log, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        val TAG = "MyBroadcastReceiver"
        const val ACTION = "android.intent.action.BOOT_COMPLETED"
    }
}