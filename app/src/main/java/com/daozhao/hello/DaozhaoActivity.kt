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
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

class DaozhaoActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDaozhaoBinding

    private val viewModel: UrlViewModel by viewModels()

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
                    Log.e(TAG, res!!)
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

    fun getLocalListStr(context: Context): String {
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

    fun getLocalList2(str: String): ArrayList<Msg> {
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

    fun updateLog(context: Context, content: String) {
        val list = getLocalList2(getLocalListStr(context));
        val newMsgBean: Msg = Gson().fromJson(content, Msg::class.java)
        var isExisted = false;
        for (item in list) {
            if (item.time === newMsgBean.time) {
                isExisted = true;
                break;
            }
        }
        if (!isExisted) {
            list.add(newMsgBean)
        }
        val listStr = Gson().toJson(list);
        Log.e(TAG, listStr)
        Utils.saveData(context!!, "test", "msgList", listStr)
    }

    fun listen() {
        val receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction("com.daozhao.push.action")
        registerReceiver(receiver, filter)
    }

    fun getIntentData() {
        val uri = intent.data
        if (uri != null) {
            Log.i(TAG, uri.toString())
            val target = uri.getQueryParameter("target")
            if (target == "pdgzf") {
                viewModel.selectItem("https://www.daozhao.com");
            }
        }
    }

    fun onUrlChange() {
        viewModel.activeUrl.observe(this, Observer<String>{ it ->
            Log.i(TAG, it);
        })
    }
    companion object {
        private const val TAG: String = "DaozhaoActivity"
    }
}