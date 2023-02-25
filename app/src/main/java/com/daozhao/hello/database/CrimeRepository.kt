package com.daozhao.hello.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.daozhao.hello.model.Crime
import java.util.*
import java.util.concurrent.Executors

class CrimeRepository private constructor(context: Context){

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2).build()

    private val crimeDao = database.crimeDao()
    // 会返回一个执行新线程的executor实例
    private val executor = Executors.newSingleThreadExecutor()

//    fun getCrimes(): List<Crime> = crimeDao.getCrimes()
    fun getCrimes(): LiveData <List<Crime>> = crimeDao.getCrimes()

//    fun getCrime(id: UUID): Crime? = crimeDao.getCrime(id)
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    companion object {
        private var INSTANCE: CrimeRepository ?= null

        val DATABASE_NAME = "crime-base"

        fun initialize(context: Context) {
            if (INSTANCE === null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?:
            throw java.lang.IllegalStateException("CrimeRepository must be initialized")
        }
    }
}