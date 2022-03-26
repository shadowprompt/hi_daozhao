package com.daozhao.hello.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.daozhao.hello.UrlViewModel
import com.daozhao.hello.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var myWebView: WebView? = null

    private val viewModel: UrlViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        myWebView = root.findViewById(com.daozhao.hello.R.id.webview) as WebView

        initWebview();
//        loadUrl("https://www.qq.com")

        viewModel.activeUrl.observe(viewLifecycleOwner, Observer {
            loadUrl(it)
        })

        return root
    }

    private fun initWebview() {
        // Configure related browser settings
        myWebView!!.settings.loadsImagesAutomatically = true
        myWebView!!.settings.javaScriptEnabled = true
        myWebView!!.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        // Configure the client to use when opening URLs
        myWebView!!.webViewClient = WebViewClient()
    }

    fun loadUrl(url: String) {
        // Load the initial URL
        myWebView!!.loadUrl(url);
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}