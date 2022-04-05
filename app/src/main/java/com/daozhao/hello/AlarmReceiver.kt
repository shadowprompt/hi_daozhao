package com.daozhao.hello

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(mContent: Context?, intent: Intent?) {
        if (CONST.ALARM_ACTION.equals(intent!!.getAction())) {
            // 第1步中设置的闹铃时间到，这里可以弹出闹铃提示并播放响铃
            Toast.makeText(mContent, "hello alarm", Toast.LENGTH_LONG).show();
            System.out.println("hello alarm");
            // 可以继续设置下一次闹铃时间;
            return;
        }
    }
}