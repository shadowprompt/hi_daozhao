package com.daozhao.hello.ui.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.daozhao.hello.Deeplink2Activity
import com.daozhao.hello.DeeplinkActivity
import com.daozhao.hello.UrlViewModel
import com.daozhao.hello.databinding.FragmentDashboardBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.HmsMessaging
import com.huawei.hms.push.HmsProfile
import okhttp3.*
import java.io.IOException

class DashboardFragment : Fragment(), View.OnClickListener {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var root: View? = null;

    private val viewModel: UrlViewModel by activityViewModels()

    private var status: Boolean = false;

    private val client = OkHttpClient()

    private var receiver: MyReceiver? = null

    private var mContext: Context?= null

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent?.extras
            if (bundle?.getString("msg") != null) {
                val content = bundle.getString("msg")
                showLog(content);
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction(CODELABS_ACTION)
        mContext!!.registerReceiver(receiver, filter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        root = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        (root as ConstraintLayout).findViewById<Button>(com.daozhao.hello.R.id.jd).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(com.daozhao.hello.R.id.taobao).setOnClickListener(this)

        (root as ConstraintLayout).findViewById<Button>(com.daozhao.hello.R.id.getTokenBtn2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(com.daozhao.hello.R.id.toggle2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(com.daozhao.hello.R.id.btn_action2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(com.daozhao.hello.R.id.btn_generate_intent2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(com.daozhao.hello.R.id.btn_web2).setOnClickListener(this)

        FirebaseApp.initializeApp(mContext!!)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUrl(url: String) {
        Log.i("updateUrl", url)

//        val mainFragment: HomeFragment? = activity?.supportFragmentManager?.findFragmentById(com.daozhao.hello.R.id.navigation_home) as HomeFragment?
//        mainFragment?.loadUrl(url)
        viewModel.selectItem(url);
    }

    private fun getToken() {
        showLog("getToken:begin")
        object : Thread() {
            override fun run() {
                try {
                    // read from agconnect-services.json
                    val appId = "102930575"

                    val token = HmsInstanceId.getInstance(mContext).getToken(appId, "HCM")
                    storeTokenProfile(token)
                } catch (e: ApiException) {
                    Log.e(TAG, "get token failed, $e")
                    showLog("get token failed, $e")
                }
            }
        }.start()
    }

    private fun storeTokenProfile(token: String?) {
        Log.i(TAG, "get token:$token")
        showLog("get token:$token")
        if (!TextUtils.isEmpty(token)) {
            sendRegTokenToServer(token);
            // 添加当前设备上的用户与应用的关系。
            val hmsProfileInstance: HmsProfile = HmsProfile.getInstance(mContext);
            if (hmsProfileInstance.isSupportProfile) {
                hmsProfileInstance.addProfile(HmsProfile.CUSTOM_PROFILE, "9105385871708200535")
                    .addOnCompleteListener { task ->
                        // 获取结果
                        if (task.isSuccessful){
                            Log.i(TAG, "add profile success.")
                        } else{
                            Log.e(TAG, "add profile failed: " + task.exception.message)
                        }
                    }
            }
        }
    }

    private fun setReceiveNotifyMsg(enable: Boolean) {
        showLog("Control the display of notification messages:begin")
        if (enable) {
            HmsMessaging.getInstance(mContext).turnOnPush().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showLog("turnOnPush Complete")
                    status = false;
                } else {
                    showLog("turnOnPush failed: cause=" + task.exception.message)
                }
            }
        } else {
            HmsMessaging.getInstance(mContext).turnOffPush().addOnCompleteListener { task ->
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
        // Define a scheme protocol, for example, pushscheme://com.daozhao.push/deeplink?.
        // way 1 start: Use ampersands (&) to separate key-value pairs. The following is an example:
        intent.data = Uri.parse("pushscheme://com.daozhao.push/deeplink?name=abc&age=180")
        // way 1 end. In this example, name=abc and age=180 are two key-value pairs separated by an ampersand (&).

        // way 2 start: Directly add parameters to the Intent.
        // intent.setData(Uri.parse("pushscheme://com.daozhao.push/deeplink?"));
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
        intent.setClass(mContext!!, DeeplinkActivity::class.java);
        startActivity(intent);
    }

    /**
     * Simulate pulling up the application custom page by action.
     */
    private fun openActivityByAction() {
        val intent = Intent("com.daozhao.push.intent.action.test2")
        intent.putExtra("_push_msgId", "myId");
        intent.putExtra("_push_cmd_type", "myType")

        // You can start the deep link activity with the following code.
        intent.setClass(mContext!!, Deeplink2Activity::class.java)
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
                            showLog("fetch failed " + uuid)
                            Log.e(TAG, "fetch failed " + uuid);
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call?, response: Response) {
                            val result: String = response.body().string()
                            showLog("fetch success " + uuid)
                            Log.i(TAG, "fetch success " + uuid);
                        }
                    })
                }
            }
        }

    }


    fun showLog(log: String?) {
        activity?.runOnUiThread {
            val textView = root!!.findViewById<TextView?>(com.daozhao.hello.R.id.text_dashboard)
            textView.text = log
        }
    }

    private fun openWeb() {
        val webpage: Uri = Uri.parse("https://www.daozhao.com")
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        // android 11之后需要在清单文件里面配置queries之后intent.resolveActivity(mContext!!.packageManager)才不会返回null
        if (intent.resolveActivity(mContext!!.packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            com.daozhao.hello.R.id.jd -> updateUrl("https://www.jd.com")
            com.daozhao.hello.R.id.taobao -> updateUrl("https://www.taobao.com")
            com.daozhao.hello.R.id.getTokenBtn2 -> getToken()
            com.daozhao.hello.R.id.toggle2 -> setReceiveNotifyMsg(status)
            com.daozhao.hello.R.id.btn_action2 -> openActivityByAction()
            com.daozhao.hello.R.id.btn_generate_intent2 -> generateIntentUri()
            com.daozhao.hello.R.id.btn_web2 -> openWeb()
        }
    }

    companion object {
        private const val TAG: String = "DashboardFragment"
        private const val GET_AAID = 1
        private const val DELETE_AAID = 2
        private const val CODELABS_ACTION: String = "com.daozhao.push.action"
        private val dataMap = object {
            val jd = "https://www.jd.com"
            val taobao = "https://www.taobao.com"
        }
    }
}