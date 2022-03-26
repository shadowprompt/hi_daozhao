package com.daozhao.hello.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.daozhao.hello.UrlViewModel
import com.daozhao.hello.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(), View.OnClickListener {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: UrlViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        (root as ConstraintLayout).findViewById<Button>(com.daozhao.hello.R.id.jd).setOnClickListener(this)
        (root as ConstraintLayout).findViewById<Button>(com.daozhao.hello.R.id.taobao).setOnClickListener(this)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUrl(url: String) {
        Log.i("updateUrl", url)

//        val mainFragment: HomeFragment? = activity?.supportFragmentManager?.findFragmentById(com.daozhao.hello.R.id.navigation_home) as HomeFragment?
//        mainFragment?.loadUrl(url)
        viewModel.selectItem(url);
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            com.daozhao.hello.R.id.jd -> updateUrl("https://www.jd.com")
            com.daozhao.hello.R.id.taobao -> updateUrl("https://www.taobao.com")
        }
    }

    companion object {
        private val dataMap = object {
            val jd = "https://www.jd.com"
            val taobao = "https://www.taobao.com"
        }
    }
}