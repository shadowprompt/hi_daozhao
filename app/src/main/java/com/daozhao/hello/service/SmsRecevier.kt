package com.daozhao.hello.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.daozhao.hello.SmsHelper


class SmsRecevier : BroadcastReceiver() {
    init {
        Log.v("dimos", "SmsRecevier create")
    }

    override fun onReceive(context: Context, intent: Intent) {
        val dString: String = SmsHelper.getSmsBody(intent)
        val address: String = SmsHelper.getSmsAddress(intent)
        Log.i("dimos", "$dString,$address")
        //阻止广播继续传递，如果该receiver比系统的级别高，
        //那么系统就不会收到短信通知了
        abortBroadcast()
    }
}