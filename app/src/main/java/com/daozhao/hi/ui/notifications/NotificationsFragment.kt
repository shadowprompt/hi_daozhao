package com.daozhao.hi.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.daozhao.hi.model.Msg
import com.daozhao.hi.Utils
import com.daozhao.hi.databinding.FragmentNotificationsBinding
import com.daozhao.hi.ui.ListAdapter



class NotificationsFragment : Fragment(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener  {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var root: View? = null;

    private var mContext: Context?= null

    private var lv: ListView? = null

    private lateinit var adapter: ListAdapter

    private var data = ArrayList<Msg>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        root = binding.root

        lv = (root as ConstraintLayout).findViewById<Button>(com.daozhao.hi.R.id.list_view) as ListView //得到ListView对象的引用

        adapter = ListAdapter(mContext!!, data)
        lv!!.adapter = adapter

        lv!!.setOnItemClickListener(this)
        lv!!.setOnItemLongClickListener(this)

        getLog()

//        initData()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context;
    }

    fun getLog() {
        var logList = Utils.getLogList(mContext!!, "test", "msgList")
//        data = logList;
//        Log.e("aad", logList.toString())
        for (item in logList) {
            data.add(item)
        }
    }

    fun initData() {
        data.add(Msg("a1", "bb1", "33"))
        data.add(Msg("a2", "bb2", "444"))
    }

    //list view的item的点击事件
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Toast.makeText(mContext!!,"你点击了->" + data[position].title, Toast.LENGTH_SHORT).show()
    }

    //listview的item 的长按时间
    override fun onItemLongClick(parent: AdapterView<*>?,view: View?, position: Int,id: Long ): Boolean {
        Toast.makeText(mContext!!,"你长按了->" + data[position].title,Toast.LENGTH_SHORT).show()
        return true
    }

}