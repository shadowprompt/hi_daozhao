package com.daozhao.hello.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.daozhao.hello.model.Msg
import com.daozhao.hello.R

class ListAdapter(val context: Context, var data: List<Msg>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val vh: ViewHolder
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.msg_item, parent, false);
            vh = ViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        //填充数据
        var msg = getItem(position)
        if (msg != null) {
            vh.msgTitle.text = msg.title
            vh.msgTime.text = msg.time
            vh.msgBody.text = msg.body
        }

        return view
    }

    inner class ViewHolder(v: View) {
        val msgTitle: TextView = v.findViewById(R.id.msgTitle)
        val msgTime: TextView = v.findViewById(R.id.msgTime)
        val msgBody: TextView = v.findViewById(R.id.msgBody)
    }

    override fun getItem(position: Int): Msg {
        return data.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return data.size
    }
}
