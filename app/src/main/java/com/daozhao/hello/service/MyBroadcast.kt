package com.daozhao.hello.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class MyBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.v("dimos", "MyBroadcast")
        if (intent.action == ACTION) {
            val service = Intent(context, MyService::class.java)
            context.startService(service)
        }
    }

    companion object {
        const val ACTION = "android.intent.action.BOOT_COMPLETED"
    }
}