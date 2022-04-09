package com.daozhao.hello.ui.dashboard

import android.Manifest
import android.app.*
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.UserDictionary
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.daozhao.hello.*
import com.daozhao.hello.databinding.FragmentDashboardBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.HmsMessaging
import com.huawei.hms.push.HmsProfile
import com.permissionx.guolindev.PermissionX
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

    private val viewModel: UrlViewModel by activityViewModels()

    private var status: Boolean = false;

    private val client = OkHttpClient()

    private var receiver: MyReceiver? = null

    private var mContext: Context?= null

    // A "projection" defines the columns that will be returned for each row
    private val mProjection: Array<String> = arrayOf(
        UserDictionary.Words._ID,    // Contract class constant for the _ID column name
        UserDictionary.Words.WORD,   // Contract class constant for the word column name
        UserDictionary.Words.LOCALE  // Contract class constant for the locale column name
    )

    // Defines a string to contain the selection clause
    private var selectionClause: String? = UserDictionary.Words.LOCALE + " LIKE ?"

    // Declares an array to contain selection arguments
    private var selectionArgs: Array<String> = arrayOf("")

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

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })



        (root as ConstraintLayout).findViewById<Button>(R.id.getTokenBtn2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.toggle2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.btn_generate_intent2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.btn_action2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.btn_web2).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.btn_getContact).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.jd).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.contact).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.btn_showNotice).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(R.id.btn_showAlarm).setOnClickListener(this)


        FirebaseApp.initializeApp(mContext!!)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUrl(url: String) {
        Log.i("updateUrl", url)

//        val mainFragment: HomeFragment? = activity?.supportFragmentManager?.findFragmentById(R.id.navigation_home) as HomeFragment?
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
        val intent = Intent(CONST.PUSH_ACTION)
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
            val textView = root!!.findViewById<TextView?>(R.id.text_dashboard)
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

    private fun showNotice() {
        val intent = Intent(mContext, DeeplinkActivity::class.java).apply {
            data = Uri.parse("pushscheme://com.daozhao.push/deeplink?name=notice&age=180")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val snoozeIntent = Intent(mContext, MyBroadcastReceiver::class.java).apply {
            action = CONST.PUSH_ACTION
            putExtra("_push_msgId", "myNewId")
        }
        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(mContext, 0, snoozeIntent, PendingIntent.FLAG_IMMUTABLE)

        var builder = NotificationCompat.Builder(mContext!!, CONST.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home_black_24dp)
            .setContentTitle("textTitle")
            .setContentText("textContent")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("firstLine dfadfadfadfadf \n secondline adjkjdkfadjkfadfadf \n 打发打发打发打发的"))
            .setAutoCancel(true)
            .addAction(R.drawable.ic_launcher_background, "snooze", snoozePendingIntent)

        NotificationManagerCompat.from(requireContext()).notify(0, builder.build())
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT


            val notificationManager: NotificationManager =  mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannelGroup(NotificationChannelGroup(CONST.NOTIFICATION_GROUP_ID, "Hello"))

            val channel = NotificationChannel(CONST.NOTIFICATION_CHANNEL_ID, "AndroidNotification", importance).apply {
                description = "a android builtin notification"
            }
            channel.group = CONST.NOTIFICATION_GROUP_ID
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showAlarm() {
        val amMgr = mContext!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //闹铃间隔， 这里设为1分钟闹一次，在第2步我们将每隔1分钟收到一次广播
        val triggerAtMillis = SystemClock.elapsedRealtime() + 10 * 1000;

        val repeatAlarmIntent = Intent(CONST.ALARM_SCHEDULE_ACTION).let { intent ->
            PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        amMgr?.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis,
            60000,
            repeatAlarmIntent
        )

        // 指定时间进行闹钟提醒
        // Set the alarm to start at 15:30 a.m.
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 30)
        }

        val calendarAlarmIntent = Intent(mContext, AlarmReceiver::class.java).let { intent ->
            intent.action = CONST.ALARM_ACTION
            PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        amMgr?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            1000 * 60 * 1,
            calendarAlarmIntent
        )
    }

    fun getContact() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.e(TAG, Build.VERSION.SDK_INT.toString())
            Toast.makeText(requireContext(), "Your android version is lower than " + Build.VERSION_CODES.LOLLIPOP_MR1, Toast.LENGTH_LONG).show()
            return
        }

        var mCursor = mContext!!.contentResolver.query(
            UserDictionary.Words.CONTENT_URI,   // The content URI of the words table
            mProjection,                        // The columns to return for each row
            selectionClause,                   // Selection criteria
            selectionArgs,      // Selection criteria
            null         // The sort order for the returned rows
        )
        val values = ContentValues(3)
        val locale = Locale.getDefault()
        val localString = locale.toString()
        selectionArgs[0] = "%$localString%"
        var resultUri : Uri? = null
        when (mCursor?.count) {
            null -> {
                /*
                 * Insert code here to handle the error. Be sure not to use the cursor!
                 * You may want to call android.util.Log.e() to log this error.
                 *
                 */
            }
            0 -> {
                // 如果没有的话先插入数据
                values.put(UserDictionary.Words.APP_ID, "com.daozhao.hello")
                values.put(UserDictionary.Words.WORD, "Tuasimodo")
                values.put(UserDictionary.Words.FREQUENCY, 250)
                values.put(UserDictionary.Words.LOCALE, localString)
                resultUri = requireActivity().contentResolver.insert(UserDictionary.Words.CONTENT_URI, values)

                UserDictionary.Words.addWord(requireContext(), "Dext", 100, "dt",  locale)
            }
            else -> {
                // Insert code here to do something with the results
            }
        }

        Log.e(TAG, resultUri.toString())

        mCursor = requireActivity().contentResolver.query(
            UserDictionary.Words.CONTENT_URI,   // The content URI of the words table
            mProjection,                        // The columns to return for each row
            selectionClause,                   // Selection criteria
            selectionArgs,      // Selection criteria
            null         // The sort order for the returned rows
        )

        // Defines a list of View IDs that will receive the Cursor columns for each row
        val wordListItems = intArrayOf(R.id.msgTitle, R.id.msgBody)

        // Creates a new SimpleCursorAdapter
        val cursorAdapter = SimpleCursorAdapter(
            mContext,             // The application's Context object
            R.layout.msg_item,           // A layout in XML for one row in the ListView
            mCursor,                        // The result from the query
            mProjection,               // A string array of column names in the cursor
            wordListItems,                 // An integer array of view IDs in the row layout
            0                               // Flags (usually none are needed)
        )

        // Sets the adapter for the ListView
        (root as ConstraintLayout).findViewById<ListView>(R.id.list_view2).setAdapter(cursorAdapter)

        mCursor?.apply {
            // Determine the column index of the column named "word"
            val index: Int = getColumnIndex(UserDictionary.Words.WORD)

            /*
             * Moves to the next row in the cursor. Before the first movement in the cursor, the
             * "row pointer" is -1, and if you try to retrieve data at that position you will get an
             * exception.
             */
            while (moveToNext()) {
                // Gets the value from the column.
                val newWord = getString(index)

                // Insert code here to process the retrieved word.
                Log.e(TAG, newWord);

                // end of while loop
            }
        }
    }

    private fun getContactPermission(){
        PermissionX.init(this)

            .permissions(Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION)

            .onExplainRequestReason { scope, deniedList ->

                val message = "需要您同意通讯录和定位权限"

                val ok = "确定"

                scope.showRequestReasonDialog(deniedList, message, ok)

            }

            .onForwardToSettings { scope, deniedList ->

                val message = "您需要去设置当中同意通讯录和定位权限"

                val ok = "确定"

                scope.showForwardToSettingsDialog(deniedList, message, ok)

            }

            .request { _, _, _ ->

                openWeb()

            }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.getTokenBtn2 -> getToken()
            R.id.toggle2 -> setReceiveNotifyMsg(status)
            R.id.btn_generate_intent2 -> generateIntentUri()
            R.id.btn_action2 -> openActivityByAction()
            R.id.btn_web2 -> openWeb()
            R.id.btn_showNotice -> showNotice()
            R.id.btn_showAlarm -> showAlarm()
            R.id.btn_getContact -> getContact()
            R.id.jd -> updateUrl("https://www.jd.com")
            R.id.contact -> getContactPermission()
        }
    }
}