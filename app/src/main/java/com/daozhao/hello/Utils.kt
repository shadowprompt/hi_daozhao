package com.daozhao.hello

import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object Utils {

    fun saveData(context: Context, database: String, key: String, value: String) {
        //获得SharedPreferences的实例 sp_name是文件名
        val sp: SharedPreferences =  context.getSharedPreferences(database, Context.MODE_PRIVATE);
        //获得Editor 实例
        val editor : SharedPreferences.Editor = sp.edit();
        //以key-value形式保存数据
        editor.putString(key, value);
        //apply()是异步写入数据
        editor.apply();
        //commit()是同步写入数据
        editor.commit();
    }

    fun getData(context: Context, database: String, key: String): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(database, MODE_PRIVATE)
        val value: String? = sharedPreferences.getString(key, "")
        return value;
    }

    fun getLogList(context: Context, database: String, key: String) : ArrayList<Msg> {
        var str = this.getData(context!!, database, key);
        if (str == "") {
            str = "[]"
        }
        val type = object : TypeToken<ArrayList<Msg>>() {}.type
        return Gson().fromJson(str, type)
    }

    fun noticeBuilder(context: Context, title: String, text: String, bigText: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context!!, CONST.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home_black_24dp)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText(bigText))
            .setAutoCancel(true)
    }
}