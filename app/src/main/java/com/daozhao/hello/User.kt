package com.daozhao.hello

data class User (val key: String, val id: String, val name: String, val phones: ArrayList<UserPhone>, var events: ArrayList<UserEvent>, val note: String?) {

}

class UserPhone (val userKey: String, data: String?, val type: String?, val label: String? ) {
    var data = data?.replace(" ", "")?.replace("/", "")
}


class UserEvent (val userKey: String, data: String?, val type: String?, val label: String?, ) {
    var data = data?.replace(" ", "")?.replace("/", "")?.replace("-", "")
}