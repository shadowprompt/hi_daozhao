package com.daozhao.hello


import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.IBinder
import android.os.Process
import android.util.Log


class BootService : Service() {
    private var mObserver: ContentObserver? = null
    private val mHandler = Handler()
    override fun onCreate() {
        Log.i(TAG, "onCreate().")
        super.onCreate()
        addSMSObserver()
    }

    fun addSMSObserver() {
        Log.i(TAG, "add a SMS observer. ")
        val resolver = contentResolver
        val handler: Handler = SMSHandler(this)
        mObserver = SMSObserver(resolver, handler)
        resolver.registerContentObserver(SMS.CONTENT_URI, true, mObserver as SMSObserver)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy().")
        this.contentResolver.unregisterContentObserver(mObserver!!)
        super.onDestroy()
        Process.killProcess(Process.myPid())
        System.exit(0)
    }

    companion object {
        const val TAG = "BootService"
    }
}