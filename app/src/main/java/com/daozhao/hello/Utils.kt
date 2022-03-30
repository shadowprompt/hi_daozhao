package com.daozhao.hello

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences


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

}