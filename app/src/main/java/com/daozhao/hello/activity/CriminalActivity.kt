package com.daozhao.hello.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.daozhao.hello.R
import com.daozhao.hello.ui.crime.CrimeFragment
import com.daozhao.hello.ui.crime.CrimeListFragment
import java.util.*

class CriminalActivity : AppCompatActivity(), CrimeFragment.Callback, CrimeListFragment.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crime)

        replaceToCrimeListFrag()
    }

    override fun onOpenList() {
        replaceToCrimeListFrag(true)
    }

    override fun onCrimeSelected(crimeId: UUID) {
        Log.d("MMMM", "CriminalActivity.onCrimeSelected: $crimeId")
        replaceToCrimeFrag(crimeId)
    }

    fun replaceToCrimeFrag(crimeId: UUID) {
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, CRIME_TAG)
            .addToBackStack("crimeList")
            .commit()
    }

    fun replaceToCrimeListFrag(forceReplace: Boolean = true) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (forceReplace || currentFragment == null) {
            val fragment = CrimeListFragment.newInstance();
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, CRIME_LIST_TAG)
                .commit()
        }
    }

    companion object {
        var CRIME_TAG = "CRIME_TAG"
        var CRIME_LIST_TAG = "CRIME_LIST_TAG"
    }
}