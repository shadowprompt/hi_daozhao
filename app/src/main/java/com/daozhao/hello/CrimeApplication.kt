package com.daozhao.hello

import android.app.Application
import com.daozhao.hello.database.CrimeRepository

class CrimeApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // 完成一次性的初始化工作
        CrimeRepository.initialize(this)
    }
}