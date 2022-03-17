package com.daozhao.hello

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.daozhao.hello.databinding.ActivityDaozhaoBinding
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException

class DaozhaoActivity : AppCompatActivity(), View.OnClickListener {

    private var receiver: MyReceiver? = null

    private lateinit var binding: ActivityDaozhaoBinding

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

        findViewById<Button>(R.id.getTokenBtn).setOnClickListener(this)
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent?.extras
            if (bundle?.getString("msg") != null) {
                val content = bundle.getString("msg")
                showLog(content);
            }
        }
    }

    private fun getToken() {
        showLog("getToken:begin")
        object : Thread() {
            override fun run() {
                try {
                    // read from agconnect-services.json
//                    val appId = "102930575"
                    val appId = "Please enter your App_Id from agconnect-services.json "

                    val token = HmsInstanceId.getInstance(this@DaozhaoActivity).getToken(appId, "HCM")
                    Log.i(TAG, "get token:$token")
                    if (!TextUtils.isEmpty(token)) {
                        sendRegTokenToServer(token)
                    }
                    showLog("get token:$token")
                } catch (e: ApiException) {
                    Log.e(TAG, "get token failed, $e")
                    showLog("get token failed, $e")
                }
            }
        }.start()
    }


    private fun sendRegTokenToServer(token: String?) {
        Log.i(TAG, "sending token to server. token:$token")
    }


    fun showLog(text: String?) {
        Toast.makeText(this, "aaa", Toast.LENGTH_LONG).show();
    }

    companion object {
        private const val TAG: String = "PushDemoLog"
        private const val GET_AAID = 1
        private const val DELETE_AAID = 2
        private const val CODELABS_ACTION: String = "com.daozhao.hello.action"
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.getTokenBtn -> getToken()
        }
    }
}