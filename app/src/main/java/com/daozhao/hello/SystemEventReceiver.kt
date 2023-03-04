package com.daozhao.hello


import android.os.Handler;
import android.os.Message;
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.gsm.SmsManager


class SystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
//            context.startService(Intent(Globals.IMICHAT_SERVICE))
//        } else if (intent.action == Globals.ACTION_SEND_SMS) {
//            val mItem = intent.getSerializableExtra(Globals.EXTRA_SMS_DATA) as MessageItem?
//            if (mItem != null && mItem.phone != null && mItem.body != null) {
//                SmsManager.getDefault()
//                    .sendTextMessage(
//                        mItem.phone, null,
//                        mItem.body, null, null
//                    )
//                //            new Thread(mTasks).start();
//            }
//        }
    }
}