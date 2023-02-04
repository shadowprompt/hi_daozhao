package com.daozhao.hello.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.daozhao.hello.CONST
import com.daozhao.hello.Utils

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(mContent: Context, intent: Intent?) {
        Log.i("ALARM_onReceive", intent!!.getAction() + "_" + intent.getStringExtra("_type"))
        if (CONST.ALARM_ACTION.equals(intent!!.getAction())) {
            when(intent.getStringExtra("_type")) {
                CONST.BIRTHDAY -> { // 生日提醒
                    val name = intent.getStringExtra("name")
                    val birthday = intent.getStringExtra("birthday")
                    val text = "hello alarm $name"
                    Log.i("ALARM_SHOW", text)
                    val builder = Utils.noticeBuilder(
                        mContent,
                        "Happy birthday",
                        "date: $birthday",
                        "Have a nice day!"
                    )

                    NotificationManagerCompat.from(mContent).notify(0, builder.build())
                }
            }

            // 可以继续设置下一次闹铃时间;
            return;
        }
    }
}