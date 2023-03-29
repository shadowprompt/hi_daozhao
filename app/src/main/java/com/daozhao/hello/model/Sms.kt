package com.daozhao.hello.model

data class SmsMsg(val title: String, val body: String) {

}

data class SmsMsgParams(val _source: String, val _name: String, val _ignoreGroups: Array<String>? = arrayOf(), val payload: SmsMsg) {

}