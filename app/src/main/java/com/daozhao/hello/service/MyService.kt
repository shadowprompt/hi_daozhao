package com.daozhao.hello.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log


class MyService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val localIntentFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        localIntentFilter.priority = 2147483647
        val localMessageReceiver = SmsReceiver()
        Log.v("dimos", "MyService")
//        registerReceiver(localMessageReceiver, localIntentFilter)
    }
}