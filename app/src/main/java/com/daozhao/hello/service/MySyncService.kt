package com.daozhao.hello.service

import android.accounts.Account
import android.app.Service
import android.content.*
import android.os.Bundle
import android.os.IBinder


class MySyncService : Service() {
    companion object {
        // Storage for an instance of the sync adapter
        private var mSyncAdapter: MySyncAdapter? = null
        // Object to use as a thread-safe lock
        private val sSyncAdapterLock = Any()
    }

    override fun onCreate() {
        synchronized(sSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = MySyncAdapter(applicationContext, true)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mSyncAdapter?.getSyncAdapterBinder()
    }

    internal class MySyncAdapter(context: Context?, autoInitialize: Boolean) :
        AbstractThreadedSyncAdapter(context, autoInitialize) {
        override fun onPerformSync(
            account: Account,
            extras: Bundle,
            authority: String,
            provider: ContentProviderClient,
            syncResult: SyncResult
        ) {            // 具体的同步操作，这里主要是为了提高进程优先级
        }
    }
}