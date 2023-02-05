package com.daozhao.hello.ui.crime

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daozhao.hello.R
import com.daozhao.hello.model.Crime
import com.daozhao.hello.model.CrimeListViewModel
import java.util.UUID

class CrimeListFragment: Fragment() {
    private lateinit var crimeRecyclerView:  RecyclerView
    private var adapter: CrimeAdapter ?= CrimeAdapter(emptyList())

    private var callback: Callback ? = null

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as Callback
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView

        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        crimeRecyclerView.adapter = adapter

//        updateUI()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.d(TAG, "Got crimes: ${crimes.size}")

                    updateUI(crimes)
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                var crime = Crime()
                crimeListViewModel.addCrime(crime)
                callback?.onCrimeSelected(crime.id)
                true
            } else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
    // 创建视图
    private inner class CrimeHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        val dateTextView: TextView = itemView.findViewById(R.id.crime_date)

        private lateinit var crime: Crime

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val toast = Toast.makeText(context, "${crime.title} pressed", Toast.LENGTH_SHORT);
            toast.setGravity(0, 500, 50)
            toast.show()
            
            callback?.onCrimeSelected(crime.id)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            titleTextView.setTextColor(Color.parseColor("#000000"))
            dateTextView.text = this.crime.date.toString()
            dateTextView.setTextColor(Color.parseColor("#000000"))
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>): RecyclerView.Adapter<CrimeHolder>() {
        // 负责需要创建的视图
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            // 根据不同的viewType用不同的布局
            val resourceId = if (viewType == 1) {
                R.layout.list_item_crime
            } else{
                R.layout.list_item_crime_requires_police
            }
            val view = layoutInflater.inflate(resourceId, parent, false)
            return CrimeHolder(view)
        }
        // 将数据集中指定数据填充到ViewHolder视图显示
        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]

            holder.bind(crime)
        }

        override fun getItemCount(): Int = crimes.size

        override fun getItemViewType(position: Int): Int {
            // 根据不同的index返回不同的viewType
            return if (position % 2 == 0) {
                1
            } else {
                2
            }
        }
    }

    interface Callback {
        fun onCrimeSelected(crimeId: UUID)
    }

    // 将viewModel里面的数据填充到UI
    fun updateUI(crimes: List<Crime>) {
//        val crimes = crimeListViewModel.crimes
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }



    companion object {
        val TAG = "CrimeListFragment"

        fun newInstance() : CrimeListFragment {
            return CrimeListFragment()
        }
    }
}
