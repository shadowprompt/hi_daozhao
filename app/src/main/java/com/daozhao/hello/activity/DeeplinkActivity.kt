package com.daozhao.hello.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.daozhao.hello.R
import com.daozhao.hello.model.UrlViewModel


/*
*  Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at

*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
/**
 * Opening a Specified Page of an App, and Receive data in the customized Activity class.
 */
class DeeplinkActivity : AppCompatActivity() {
    private val viewModel: UrlViewModel by viewModels()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deeplink)

        getIntentData(intent)
    }

    private fun getIntentData(intent: Intent?) {
        if (intent != null) {
            val uri = intent.data
            if (uri != null) {
                Log.i(TAG, uri.toString())
                val target = uri.getQueryParameter("target")
                if (target == "pdgzf") {
                    viewModel.selectItem("https://www.daozhao.com");
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getIntentData(intent)
    }

    companion object {
        private const val TAG = "DeeplinkActivity"
    }
}