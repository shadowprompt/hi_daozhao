package com.daozhao.hello

import android.net.Uri
import android.provider.BaseColumns


interface SMS : BaseColumns {
   companion object {
      val CONTENT_URI = Uri.parse("content://sms")
      const val FILTER = "!imichat"
      const val TYPE = "type"
      const val THREAD_ID = "thread_id"
      const val ADDRESS = "address"
      const val PERSON_ID = "person"
      const val DATE = "date"
      const val READ = "read"
      const val BODY = "body"
      const val PROTOCOL = "protocol"
      const val MESSAGE_TYPE_ALL = 0
      const val MESSAGE_TYPE_INBOX = 1
      const val MESSAGE_TYPE_SENT = 2
      const val MESSAGE_TYPE_DRAFT = 3
      const val MESSAGE_TYPE_OUTBOX = 4
      const val MESSAGE_TYPE_FAILED = 5 // for failed outgoing messages
      const val MESSAGE_TYPE_QUEUED = 6 // for messages to send later
      const val PROTOCOL_SMS = 0 //SMS_PROTO
      const val PROTOCOL_MMS = 1 //MMS_PROTO
   }
}