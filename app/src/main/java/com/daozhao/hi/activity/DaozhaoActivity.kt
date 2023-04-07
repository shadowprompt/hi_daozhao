package com.daozhao.hi.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.daozhao.hi.R
import com.daozhao.hi.Utils
import com.daozhao.hi.databinding.ActivityDaozhaoBinding
import com.daozhao.hi.model.UrlViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class DaozhaoActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDaozhaoBinding

    private val viewModel: UrlViewModel by viewModels()

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Utils.showMsgViaStatusBar(context, intent?.extras, "BROADCAST")
        }
    }

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
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        onUrlChange()

//        getDataFromIntent()

        registerlisteners()

//        test()
    }



    private fun registerlisteners() {
        listenPushActionReceiver()

    }

    private fun listenPushActionReceiver() {
        val receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction("com.daozhao.push.action")
        registerReceiver(receiver, filter)
    }

    private fun onUrlChange() {
        viewModel.activeUrl.observe(this, Observer<String>{ it ->
            Log.i(TAG, it);
        })
    }

    companion object {
        private const val TAG: String = "DaozhaoActivity"
    }
}