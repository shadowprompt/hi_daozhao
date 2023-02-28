package com.daozhao.hello.model

data class SmsMsg(val address: String, val body: String) {

}

data class SmsMsgParams(val _source: String, val _name: String, val payload: SmsMsg) {

}