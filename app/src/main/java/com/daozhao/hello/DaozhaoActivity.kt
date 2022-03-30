package com.daozhao.hello

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
import com.daozhao.hello.databinding.ActivityDaozhaoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class DaozhaoActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDaozhaoBinding

    private val viewModel: UrlViewModel by viewModels()

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent?.extras
            if (bundle?.getString("msg") != null) {
                val content = bundle.getString("msg")
                if (content != null) {
                    Log.e("MUCH", content)
                    Utils.saveData(context!!, "test", "msg", content + "_9999")
                    val res = Utils.getData(context, "test", "msg");
                    Log.e("MUCH_Aa", res!!)
                };
            }
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
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_discovery
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        onUrlChange()

        getIntentData()

        listen()
    }

    fun listen() {
        val receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction("com.daozhao.hello.action")
        registerReceiver(receiver, filter)
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