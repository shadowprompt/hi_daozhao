package com.daozhao.hello

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.daozhao.hello.databinding.ActivityDaozhaoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class DaozhaoActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDaozhaoBinding

    private val viewModel: UrlViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDaozhaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_daozhao)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_discovery
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        onUrlChange()

        getIntentData()
    }

    fun getIntentData() {
        val uri = intent.data
        if (uri != null) {
            Log.i("ABC--", uri.toString())
            val target = uri.getQueryParameter("target")
            if (target == "pdgzf") {
                viewModel.selectItem("https://www.daozhao.com");
            }
        }
    }

    fun onUrlChange() {
        viewModel.activeUrl.observe(this, Observer<String>{ it ->
            Log.i("DATA", it);
        })
    }

}