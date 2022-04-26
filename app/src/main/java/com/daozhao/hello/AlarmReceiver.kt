package com.daozhao.hello

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(mContent: Context, intent: Intent?) {
        if (CONST.ALARM_ACTION.equals(intent!!.getAction())) {
            when(intent.getStringExtra("type")) {
                CONST.BIRTHDAY -> { // 生日提醒
                    val name = intent.getStringExtra("name")
                    val birthday = intent.getStringExtra("birthday")
                    val text = "hello alarm $name"
                    Log.i("ALARM_SHOW", text)
                    val builder = Utils.noticeBuilder(mContent, "Happy birthday", "date: $birthday", "Have a nice day!")

                    NotificationManagerCompat.from(mContent).notify(0, builder.build())
                }
            }

            // 可以继续设置下一次闹铃时间;
            return;
        }
    }
}