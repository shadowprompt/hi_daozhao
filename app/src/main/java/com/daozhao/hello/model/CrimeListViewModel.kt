package com.daozhao.hello.model

import androidx.lifecycle.ViewModel
import com.daozhao.hello.database.CrimeRepository

class CrimeListViewModel: ViewModel() {
//    val crimes = mutableListOf<Crime>()
//
//    init {
//        for(i in 1 until 100) {
//            var crime = Crime()
//            crime.title = "Crime #$i"
//            crime.isSolved = i % 2 == 0
//            crimes += crime
//        }
//    }

    private val crimeRepository = CrimeRepository.get()
//    val crimes = crimeRepository.getCrimes()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }
}