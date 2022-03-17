package com.daozhao.news



import android.os.Bundle

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun showLog(text: String?) {
        Toast.makeText(this, "aaa", Toast.LENGTH_LONG).show();
    }
}