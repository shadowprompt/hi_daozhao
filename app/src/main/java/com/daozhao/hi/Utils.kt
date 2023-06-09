package com.daozhao.hi

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daozhao.hi.model.Msg
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
            .setSmallIcon(R.drawable.daozhaolite)
            .setContentTitle(title)
            .setColor(Color.parseColor("#df3473")) // 背景色
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText(bigText))
            .setAutoCancel(true)
    }
    fun showMsgViaStatusBar(context: Context?, bundle: Bundle?, source : String = "") {
        Log.i("MsgViaStatusBar", context.toString() + '_' + bundle.toString() + '_' + source)
        if (context == null) {
            return;
        }
        if (bundle?.getString("msgData") == null) {
            return;
        }
        val msgData = bundle.getString("msgData");
        val msg = Gson().fromJson(msgData, Msg::class.java);
        val builder = this.noticeBuilder(context, msg.title, msg.body, msg.body)
        // 采用不同的notifyId，避免覆盖
        val notifyId = System.currentTimeMillis().toInt()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(context).notify(notifyId, builder.build())
    }
}
