package com.daozhao.hi.ui.dashboard

import android.app.*
import android.content.*
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.daozhao.hi.*
import com.daozhao.hi.databinding.FragmentDashboardBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.HmsMessaging
import com.huawei.hms.push.HmsProfile
import okhttp3.*
import java.io.IOException
import java.util.*


class DashboardFragment : Fragment(), View.OnClickListener {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var root: View? = null;

    private var status: Boolean = false;

    private val client = OkHttpClient()

    private var mContext: Context?= null

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

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

        (root as ConstraintLayout).findViewById<Button>(R.id.getTokenBtn2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.toggle2).setOnClickListener(this)


        FirebaseApp.initializeApp(mContext!!)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getToken() {
        textLog("getToken:begin")
        object : Thread() {
            override fun run() {
                try {
                    // read from agconnect-services.json
                    val appId = "102930575"

                    val token = HmsInstanceId.getInstance(mContext).getToken(appId, "HCM")
                    storeTokenProfile(token)
                } catch (e: ApiException) {
                    textLog("get token failed, $e", true)
                }
            }
        }.start()
    }

    private fun storeTokenProfile(token: String?) {
        textLog("get token:$token")
        if (!TextUtils.isEmpty(token)) {
            sendRegTokenToServer(token);
            // 添加当前设备上的用户与应用的关系。
            val hmsProfileInstance: HmsProfile = HmsProfile.getInstance(mContext);
            if (hmsProfileInstance.isSupportProfile) {
                hmsProfileInstance.addProfile(HmsProfile.CUSTOM_PROFILE, "9105385871708200535")
                    .addOnCompleteListener { task ->
                        // 获取结果
                        if (task.isSuccessful){
                            textLog("add profile success. token: $token")
                        } else{
                            textLog("add profile failed: " + task.exception.message, true)
                        }
                    }
            }
        }
    }

    private fun setReceiveNotifyMsg(enable: Boolean) {
        textLog("Control the display of notification messages:begin")
        if (enable) {
            HmsMessaging.getInstance(mContext).turnOnPush().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    textLog("已开启消息推送")
                    status = false;
                } else {
                    textLog("开启消息推送失败\n原因：" + task.exception.message)
                }
            }
        } else {
            HmsMessaging.getInstance(mContext).turnOffPush().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    textLog("已关闭消息推送")
                    status = true;
                } else {
                    textLog("关闭消息推送失败\n原因：" + task.exception.message)
                }
            }
        }
    }

    private fun sendRegTokenToServer(token: String?) {
        Log.i(TAG, "sending token to server. token:$token")
        FirebaseInstallations.getInstance().id.addOnCompleteListener {
            run {
                if (it.isComplete) {
                    var uuid = it.result.toString();
                    textLog("成功获取uuid: " + uuid);
                    // 用hutool发送请求
//                    val paramMap: HashMap<String, Any> = HashMap()
//                    paramMap["id"] = uuid
//                    paramMap["pushToken"] = token.toString()
//                    val result = HttpUtil.post("https://gateway.daozhao.com.cn/HMS/storePushToken", paramMap)
//                    textLog("post result " + result)

                    var formBody: FormBody.Builder = FormBody.Builder();
                    formBody.add("id", uuid);
                    formBody.add("pushToken",token.toString());
                    val request: Request = Request.Builder()
                        .url("https://gateway.daozhao.com.cn/HMS/storePushToken")
                        .post(formBody.build())
                        .build()
                    val call: Call = client.newCall(request)
                    call.enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            textLog("存储至服务器失败\nuuid: $uuid\ntoken: $token", true)
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call?, response: Response) {
                            val result: String = response.body().string()
                            textLog("已存储至服务器\nuuid: $uuid\ntoken: $token")
                        }
                    })
                }
            }
        }

    }


    // 记log 并宰文本框内显示出来
    fun textLog(log: String?, isError: Boolean = false) {
        if (isError == true) {
            Log.e(TAG, log.toString())
        } else {
            Log.i(TAG, log.toString())
        }
        activity?.runOnUiThread {
            val textView = root!!.findViewById<TextView?>(R.id.text_dashboard)
            textView.text = log
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT


            val notificationManager: NotificationManager =  mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannelGroup(NotificationChannelGroup(CONST.NOTIFICATION_GROUP_ID, "Hi"))

            val channel = NotificationChannel(CONST.NOTIFICATION_CHANNEL_ID, "AndroidNotification", importance).apply {
                description = "a android builtin notification"
            }
            channel.group = CONST.NOTIFICATION_GROUP_ID
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.getTokenBtn2 -> getToken()
            R.id.toggle2 -> setReceiveNotifyMsg(status)
        }
    }
}