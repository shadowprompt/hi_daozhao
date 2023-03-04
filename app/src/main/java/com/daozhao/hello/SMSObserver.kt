package com.daozhao.hello

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Message
import android.util.Log


class SMSObserver(private val mResolver: ContentResolver, private val mHandler: Handler) :
    ContentObserver(mHandler) {
    override fun onChange(selfChange: Boolean) {
        Log.i(TAG, "onChange : " + selfChange + "; ")
//        super.onChange(selfChange)
//        val cursor = mResolver.query(
//            permission_group.SMS.CONTENT_URI, PROJECTION, String.format(
//                SELECTION, MAX_ID
//            ), null, null
//        )
//        var id: Int
//        var type: Int
//        var protocol: Int
//        var phone: String?
//        var body: String?
//        var message: Message
//        var item: MessageItem
//        var iter = 0
//        var hasDone = false
//        while (cursor!!.moveToNext()) {
//            id = cursor.getInt(COLUMN_INDEX_ID)
//            type = cursor.getInt(COLUMN_INDEX_TYPE)
//            phone = cursor.getString(COLUMN_INDEX_PHONE)
//            body = cursor.getString(COLUMN_INDEX_BODY)
//            protocol = cursor.getInt(COLUMN_INDEX_PROTOCOL)
//            if (hasDone) {
//                MAX_ID = id
//                break
//            }
//            if (protocol == permission_group.SMS.PROTOCOL_SMS && body != null && body.startsWith(
//                    permission_group.SMS.FILTER
//                )
//            ) {
//                hasDone = true
//                item = MessageItem()
//                item.setId(id)
//                item.setType(type)
//                item.setPhone(phone)
//                item.setBody(body)
//                item.setProtocol(protocol)
//                message = Message()
//                message.obj = item
//                mHandler.sendMessage(message)
//            } else {
//                if (id > MAX_ID) MAX_ID = id
//            }
//            if (iter > MAX_NUMS) break
//            iter++
//        }
    }

    companion object {
        const val TAG = "SMSObserver"
//        private val PROJECTION = arrayOf<String>(
//            permission_group.SMS._ID,  //0
//            permission_group.SMS.TYPE,  //1
//            permission_group.SMS.ADDRESS,  //2
//            permission_group.SMS.BODY,  //3
//            permission_group.SMS.DATE,  //4
//            permission_group.SMS.THREAD_ID,  //5
//            permission_group.SMS.READ,  //6
//            permission_group.SMS.PROTOCOL //7
//        )
//        private val SELECTION: String =
//            ((((permission_group.SMS._ID + " > %s" +  //      " and " + SMS.PROTOCOL + " = null" +
//                    //      " or " + SMS.PROTOCOL + " = " + SMS.PROTOCOL_SMS + ")" +
//                    " and (" + permission_group.SMS.TYPE).toString() + " = " + permission_group.SMS.MESSAGE_TYPE_INBOX).toString() +
//                    " or " + permission_group.SMS.TYPE).toString() + " = " + permission_group.SMS.MESSAGE_TYPE_SENT).toString() + ")"
//        private const val COLUMN_INDEX_ID = 0
//        private const val COLUMN_INDEX_TYPE = 1
//        private const val COLUMN_INDEX_PHONE = 2
//        private const val COLUMN_INDEX_BODY = 3
//        private const val COLUMN_INDEX_PROTOCOL = 7
//        private const val MAX_NUMS = 10
//        private var MAX_ID = 0
    }
}