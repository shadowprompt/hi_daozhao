package com.daozhao.hello.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.daozhao.hello.model.Crime
import java.util.UUID

@Dao
interface CrimeDao {
    @Query("Select * from crime")
//    fun getCrimes(): List<Crime>
    fun getCrimes(): LiveData<List<Crime>>

    @Query("Select * from crime where id=(:id)")
//    fun getCrime(id: UUID): Crime?
    fun getCrime(id: UUID): LiveData<Crime?>

    // Room不会自动在后台线程上执行数据库插入和更新操作，只能自己手动在后台线程上执行这些DAO调用，具体方法：使用executor
    @Update
    fun updateCrime(crime: Crime)
    // 新增的注解不需要任何参数 Room会使用他们产生的核实的sql操作命令
    @Insert
    fun addCrime(crime: Crime)
}