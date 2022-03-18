package com.daozhao.hello

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.daozhao.hello.databinding.ActivityDaozhaoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.HmsMessaging
import okhttp3.*
import java.io.IOException


class DaozhaoActivity : AppCompatActivity(), View.OnClickListener {

    private var receiver: MyReceiver? = null

    private lateinit var binding: ActivityDaozhaoBinding

    private var status: Boolean = false;

    private val client = OkHttpClient()

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


        receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction(CODELABS_ACTION)
        registerReceiver(receiver, filter)


        findViewById<Button>(R.id.getTokenBtn).setOnClickListener(this)
        findViewById<Button>(R.id.toggle).setOnClickListener(this)
        findViewById<Button>(R.id.btn_action).setOnClickListener(this)
        findViewById<Button>(R.id.btn_generate_intent).setOnClickListener(this)

        FirebaseApp.initializeApp(this);
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
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

    private fun setReceiveNotifyMsg(enable: Boolean) {
        showLog("Control the display of notification messages:begin")
        if (enable) {
            HmsMessaging.getInstance(this).turnOnPush().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showLog("turnOnPush Complete")
                    status = false;
                } else {
                    showLog("turnOnPush failed: cause=" + task.exception.message)
                }
            }
        } else {
            HmsMessaging.getInstance(this).turnOffPush().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showLog("turnOffPush Complete")
                    status = true;
                } else {
                    showLog("turnOffPush  failed: cause =" + task.exception.message)
                }
            }
        }
    }

    /**
     * In Opening a Specified Page of an App, how to Generate Intent parameters.
     */
    private fun generateIntentUri() {
        val intent = Intent(Intent.ACTION_VIEW)

        // You can add parameters in either of the following ways:
        // Define a scheme protocol, for example, pushscheme://com.huawei.codelabpush/deeplink?.
        // way 1 start: Use ampersands (&) to separate key-value pairs. The following is an example:
        intent.data = Uri.parse("pushscheme://com.huawei.codelabpush/deeplink?name=abc&age=180")
        // way 1 end. In this example, name=abc and age=180 are two key-value pairs separated by an ampersand (&).

        // way 2 start: Directly add parameters to the Intent.
        // intent.setData(Uri.parse("pushscheme://com.huawei.codelabpush/deeplink?"));
        // intent.putExtra("name", "abc");
        // intent.putExtra("age", 180);
        // way 2 end.

        // The following flag is mandatory. If it is not added, duplicate messages may be displayed.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val intentUri = intent.toUri(Intent.URI_INTENT_SCHEME)
        // The value of intentUri will be assigned to the intent parameter in the message to be sent.
        Log.d("intentUri", intentUri)
        showLog(intentUri)

        // You can start the deep link activity with the following code.
        intent.setClass(this, DeeplinkActivity::class.java);
        startActivity(intent);
    }

    /**
     * Simulate pulling up the application custom page by action.
     */
    private fun openActivityByAction() {
        val intent = Intent("com.huawei.codelabpush.intent.action.test")

        // You can start the deep link activity with the following code.
        intent.setClass(this, Deeplink2Activity::class.java)
        startActivity(intent)
    }


    private fun sendRegTokenToServer(token: String?) {
        Log.i(TAG, "sending token to server. token:$token")
        FirebaseInstallations.getInstance().id.addOnCompleteListener { it ->
            run {
                if (it.isComplete) {
                    var uuid = it.result.toString();
                    showLog("uuid complete " + uuid);

                    var formBody: FormBody.Builder = FormBody.Builder();
                    formBody.add("id", uuid);
                    formBody.add("pushToken",token);
                    val request: Request = Request.Builder()
                        .url("https://gateway.daozhao.com.cn/HMS/storePushToken")
                        .post(formBody.build())
                        .build()
                    val call: Call = client.newCall(request)
                    call.enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            showLog("fetch failed" + uuid)
                            Log.e(TAG, "fetch failed" + uuid);
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call?, response: Response) {
                            val result: String = response.body().string()
                            showLog("fetch success" + uuid)
                            Log.i(TAG, "fetch success" + uuid);
                        }
                    })
                }
            }
        }

    }


    fun showLog(log: String?) {
        runOnUiThread {
            val textView = findViewById<View?>(R.id.msgText)
            if (textView is TextView) {
                textView.text = log
            }
        }
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
            R.id.toggle -> setReceiveNotifyMsg(status)
            R.id.btn_action -> openActivityByAction()
            R.id.btn_generate_intent -> generateIntentUri()
        }
    }

}