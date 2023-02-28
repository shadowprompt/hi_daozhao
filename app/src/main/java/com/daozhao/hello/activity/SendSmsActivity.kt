package com.daozhao.hello.activity


import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsManager
import com.daozhao.hello.R
import com.daozhao.hello.service.SendSmsReceiver


class SendMessageActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_sms)
        val receiver = SendSmsReceiver()
        val filter = IntentFilter()
        filter.addAction(SendSmsReceiver.ACTION)
        registerReceiver(receiver, filter)
        //必须先注册广播接收器,否则接收不到发送结果
        val smsManager = SmsManager.getDefault()
        val intent = Intent()
        intent.action = SendSmsReceiver.ACTION
        val divideMessage = smsManager.divideMessage("ye")
        val sentIntent = PendingIntent.getBroadcast(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val sentIntents = ArrayList<PendingIntent>()
        sentIntents.add(sentIntent)
        try {
            smsManager.sendMultipartTextMessage(
                "10086", null,
                divideMessage, sentIntents, null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}