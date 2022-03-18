package com.daozhao.hello.ui.notifications

import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.daozhao.hello.databinding.FragmentNotificationsBinding
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException


class NotificationsFragment : Fragment() {


    private lateinit var notificationsViewModel: NotificationsViewModel
    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

//    private var receiver: MyReceiver? = null

    private var btn: Button? = null;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        btn = binding.getTokenBtn;
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        btn!!.setOnClickListener(object : View.OnClickListener() {
//            override fun onClick(v: View?) {
//                // TODO Auto-generated method stub
//                Toast.makeText(activity, "success2", Toast.LENGTH_LONG).show()
//            }
//        })
//    }

    fun clickFun(v: View) {
        Toast.makeText(activity, "success2", Toast.LENGTH_LONG).show()
    }

//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        receiver = MyReceiver()
//        val filter = IntentFilter()
//        filter.addAction(CODELABS_ACTION)
////        registerReceiver(receiver, filter)
//    }
//

//
//    private fun getToken() {
//        showLog("getToken:begin")
//        object : Thread() {
//            override fun run() {
//                try {
//                    // read from agconnect-services.json
////                    val appId = "102930575"
//                    val appId = "Please enter your App_Id from agconnect-services.json "
//
//                    val token = HmsInstanceId.getInstance(this@DaozhaoActivity).getToken(appId, "HCM")
//                    Log.i(TAG, "get token:$token")
//                    if (!TextUtils.isEmpty(token)) {
//                        sendRegTokenToServer(token)
//                    }
//                    showLog("get token:$token")
//                } catch (e: ApiException) {
//                    Log.e(TAG, "get token failed, $e")
//                    showLog("get token failed, $e")
//                }
//            }
//        }.start()
//    }
//
//
//    private fun sendRegTokenToServer(token: String?) {
//        Log.i(TAG, "sending token to server. token:$token")
//    }
//
//
//    fun showLog(text: String?) {
////        Toast.makeText(this, "aaa", Toast.LENGTH_LONG).show();
//    }
//
//    inner class MyReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val bundle = intent?.extras
//            if (bundle?.getString("msg") != null) {
//                val content = bundle.getString("msg")
//                showLog(content);
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG: String = "PushDemoLog"
//        private const val GET_AAID = 1
//        private const val DELETE_AAID = 2
//        private const val CODELABS_ACTION: String = "com.daozhao.hello.action"
//    }
}