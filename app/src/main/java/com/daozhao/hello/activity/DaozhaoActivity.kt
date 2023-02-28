package com.daozhao.hello.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.daozhao.hello.R
import com.daozhao.hello.Utils
import com.daozhao.hello.databinding.ActivityDaozhaoBinding
import com.daozhao.hello.model.Crime
import com.daozhao.hello.model.CrimeListViewModel
import com.daozhao.hello.model.Msg
import com.daozhao.hello.model.UrlViewModel
import com.daozhao.hello.service.MyService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.text.ParsePosition
import java.util.*

class DaozhaoActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDaozhaoBinding

    private val viewModel: UrlViewModel by viewModels()

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent?.extras
            if (bundle?.getString("msg") != null) {
                val msgData = bundle.getString("msgData")
                if (msgData != null) {
                    Log.e(TAG, msgData)
                    updateLog(context!!, msgData)
//                    Utils.saveData(context!!, "test", "msgList", msgData)
                    val res = Utils.getData(context, "test", "msgList");
                    val msg = Gson().fromJson(msgData, Msg::class.java);
                    var crime = Crime(UUID.randomUUID(), msg.title)
                    crimeListViewModel.addCrime(crime)
                    Log.e(TAG, res!!)
                    // HMS的透传消息
                    if (bundle?.getString("msgType") == "HMS") {
                        showMsgViaStatusBar(context!!, bundle)
                    }
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
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_discovery
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        onUrlChange()

//        getDataFromIntent()

        registerlisteners()

//        test()
    }

    fun test() {
        var m = Msg(title = "t", body = "b", time = "abc")
        var json = Gson().toJson(m);
        val msg = Gson().fromJson(json, Msg::class.java);
        Log.i(TAG, msg.toString())

        val str = "{\"title\": \"abc\", \"body\": \"124\", \"time\": \"124\"}"
        var strJson = Gson().fromJson(str, Msg::class.java)
        Log.i(TAG, strJson.toString())
    }

    private fun getLocalListStr(context: Context): String {
        var strByJson = Utils.getData(context, "test", "msgList");
        if (strByJson == "") {
            strByJson = "[]"
        }
       return strByJson!!
    }


    fun getLocalList(str: String): ArrayList<Msg> {
        val type = object : TypeToken<ArrayList<Msg>>() {}.type
        return Gson().fromJson(str, type)
    }

    private fun getLocalList2(str: String): ArrayList<Msg> {
        val jsonArray: JsonArray = JsonParser.parseString(str).asJsonArray
        val msgBeanList: ArrayList<Msg> = ArrayList()
        //加强for循环遍历JsonArray
        for (item in jsonArray) {
            //使用GSON，直接转成Bean对象
            val msgBean: Msg =  Gson().fromJson(item, Msg::class.java)
            //加强for循环遍历JsonArray
            msgBeanList.add(msgBean)
        }
        return msgBeanList
    }

    private fun removeDuplicateMsgList(list: ArrayList<Msg>) {
        val result: ArrayList<Msg> = ArrayList(list.size)
        val set: HashSet<String> = HashSet<String>(list.size)
        for (item in list) {
            if (!set.contains(item.time)) {
                result.add(item)
            }
        }
        list.clear()
        list.addAll(result)
    }

    private fun sortMsgList(list: ArrayList<Msg>) {
        list.sortWith { a, b ->
            run {
                var timeA =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(a.time, ParsePosition(0)).time
                var timeB =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(b.time, ParsePosition(0)).time
                timeB.compareTo(timeA)
            }
        }
    }

    fun updateLog(context: Context, content: String) {
        var list = getLocalList2(getLocalListStr(context));
        val newMsgBean: Msg = Gson().fromJson(content, Msg::class.java)

        list.add(newMsgBean)

//        removeDuplicateMsgList(list)

        list = list.distinctBy { it.time }  as ArrayList<Msg>

        sortMsgList(list)


        val listStr = Gson().toJson(list);
        Log.e(TAG, listStr)
        Utils.saveData(context!!, "test", "msgList", listStr)
    }

    private fun registerlisteners() {
        listenPushActionReceiver()

        listenSmsReceiver()
    }

    private fun listenPushActionReceiver() {
        val receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction("com.daozhao.push.action")
        registerReceiver(receiver, filter)
    }

    private fun listenSmsReceiver() {
        val service = Intent(this, MyService::class.java)
        startService(service)
    }

    fun getDataFromIntent() {
        val uri = intent.data
        if (uri != null) {
            Log.i(TAG, uri.toString())
            val target = uri.getQueryParameter("target")
            if (target == "pdgzf") {
                viewModel.selectItem("https://www.daozhao.com");
            }
        }
    }

    private fun onUrlChange() {
        viewModel.activeUrl.observe(this, Observer<String>{ it ->
            Log.i(TAG, it);
        })
    }
    // 将消息通过通知栏显示
    fun showMsgViaStatusBar(context: Context, bundle: Bundle) {
        val msgData = bundle.getString("msgData");
        val msg = Gson().fromJson(msgData, Msg::class.java);
        var builder = Utils.noticeBuilder(context, msg.title, msg.body, msg.body)
        // 采用不同的notifyId，避免覆盖
        val notifyId = System.currentTimeMillis().toInt()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(context).notify(notifyId, builder.build())
    }
    companion object {
        private const val TAG: String = "DaozhaoActivity"
    }
}