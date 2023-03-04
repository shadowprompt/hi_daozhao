package com.daozhao.hello

import java.io.Serializable


class MessageItem : Serializable {
    var id = 0
    var type = 0
    var protocol = 0
    var phone: String? = null
    var body: String? = null

    override fun toString(): String {
        return "id = " + id + ";" +
                "type = " + type + ";" +
                "protocol = " + protocol + ";" +
                "phone = " + phone + ";" +
                "body = " + body
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}