package com.daozhao.hello.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.daozhao.hello.CONST.DAOZHAO_GATEWAY_SERVER
import com.daozhao.hello.SmsHelper
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
        Log.i("dimos", "$address,$body")
//        forwardSms(address, body)
        //阻止广播继续传递，如果该receiver比系统的级别高，
        //那么系统就不会收到短信通知了
        abortBroadcast()
    }

    // 将收到的短信用http转发出去
    private fun forwardSms(address: String, body: String) {

        val smsMsg = SmsMsg("【短信来源】 $address", body)
        val smsMsgParams = SmsMsgParams("bark_sms", "sms", smsMsg)
        val json = Gson().toJson(smsMsgParams)

        val requestBody: RequestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json)

        val request: Request = Request.Builder()
            .url("$DAOZHAO_GATEWAY_SERVER/sms/hh")
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
}