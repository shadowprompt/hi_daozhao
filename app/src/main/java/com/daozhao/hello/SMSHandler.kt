package com.daozhao.hello
import android.content.ContentUris
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log


class SMSHandler(private val mContext: Context) : Handler() {
    override fun handleMessage(message: Message) {
        Log.i(TAG, "handleMessage: $message")
        val item: MessageItem = message.obj as MessageItem
        //delete the sms
        val uri = ContentUris.withAppendedId(SMS.CONTENT_URI, item.id.toLong())
        mContext.contentResolver.delete(uri, null, null)
        Log.i(TAG, "delete sms item: $item")
    }

    companion object {
        const val TAG = "SMSHandler"
    }
}