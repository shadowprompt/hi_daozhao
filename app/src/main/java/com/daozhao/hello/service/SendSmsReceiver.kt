package com.daozhao.hello.service

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class SendSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (ACTION == action) {
            val resultCode = resultCode
            if (resultCode == Activity.RESULT_OK) {
                // 发送成功
                println("发送成功！")
            } else {
                // 发送失败
                println("发送失败！")
            }
        }
    }

    companion object {
        const val ACTION = "action.send.sms"
    }
}