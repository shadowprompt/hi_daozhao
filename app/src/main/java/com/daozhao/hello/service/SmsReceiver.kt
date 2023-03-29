package com.daozhao.hello.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.daozhao.hello.CONST.DAOZHAO_GATEWAY_SERVER
import com.daozhao.hello.SmsHelper
import com.daozhao.hello.Utils
import com.daozhao.hello.model.SmsMsg
import com.daozhao.hello.model.SmsMsgParams
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException


class SmsReceiver : BroadcastReceiver() {
    private val client = OkHttpClient()

    init {
        Log.v("dimos", "SmsReceiver create")
    }

    override fun onReceive(context: Context, intent: Intent) {
        val body: String = SmsHelper.getSmsBody(intent)
        val address: String = SmsHelper.getSmsAddress(intent)
        Log.v("dimos received", "$address,$body")
        showSms(context, "短信源自$address", body)
//        forwardSms(address, body)
        //阻止广播继续传递，如果该receiver比系统的级别高，
        //那么系统就不会收到短信通知了
        abortBroadcast()
    }

    // 将收到的短信用http转发出去
    private fun forwardSms(address: String, body: String) {

        val smsMsg = SmsMsg("【短信来源】 $address", body)
        val smsMsgParams = SmsMsgParams("bark_sms", "sms", arrayOf("LB") ,smsMsg)
        val json = Gson().toJson(smsMsgParams)

        val requestBody: RequestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json)

        val request: Request = Request.Builder()
            .url("$DAOZHAO_GATEWAY_SERVER/sms/push")
            .post(requestBody)
            .build()
        val call: Call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.i("smsPush_error", "fetch failed: $address body: $body",)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                val result: String = response.body().string()
                Log.i("smsPush_response", "fetch success: $address body: $body")
            }
        })
    }

    private fun showSms(context: Context, title: String, body: String) {
        val builder = Utils.noticeBuilder(context, title, body, body)
        // 采用不同的notifyId，避免覆盖
        val notifyId = System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(context).notify(notifyId, builder.build())
    }
}