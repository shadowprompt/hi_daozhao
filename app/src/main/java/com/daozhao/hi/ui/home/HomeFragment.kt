package com.daozhao.hi.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.daozhao.hi.R
import com.daozhao.hi.model.UrlViewModel
import com.daozhao.hi.databinding.FragmentHomeBinding


class WebAppInterface(private val mContext: Context) {

    // 在JS中直接调用Android的方法
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }
}

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mContext: Context? = null;
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


        myWebView = root.findViewById(com.daozhao.hi.R.id.webview) as WebView

        initWebview();

        viewModel.activeUrl.observe(viewLifecycleOwner, Observer {
            loadUrl(it)
        })

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context;
    }

    private fun initWebview() {
        // Configure related browser settings
        myWebView!!.settings.loadsImagesAutomatically = true
        myWebView!!.settings.javaScriptEnabled = true
        myWebView!!.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        // Configure the client to use when opening URLs
        myWebView!!.webViewClient = WebViewClient()
        // 缩放开关，设置此属性，仅支持双击缩放，不支持触摸缩放
        myWebView!!.settings.useWideViewPort = true
        // 设置可以支持缩放
        myWebView!!.settings.setSupportZoom(true)
        // 设置出现缩放工具
//        myWebView!!.settings.builtInZoomControls = true
        // 设定缩放控件隐藏
        myWebView!!.settings.displayZoomControls = true

        myWebView!!.addJavascriptInterface(WebAppInterface(mContext!!), "AndroidFunction");
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