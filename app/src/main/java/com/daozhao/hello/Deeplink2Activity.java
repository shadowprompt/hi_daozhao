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

package com.daozhao.hello;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class Deeplink2Activity extends AppCompatActivity {
    private static final String TAG = "PushDemoLog";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deeplink2);
        getIntentData(getIntent());
    }
    private void getIntentData(Intent intent) {
        if (null != intent) {
            // 获取指定数据
            String msgId = intent.getStringExtra("_push_msgId");
            String cmdType = intent.getStringExtra("_push_cmd_type");
            int notifyId = intent.getIntExtra("_push_notifyId", -1);
            Log.i(TAG, "receive data from push, msgId = " + msgId + ", cmd = " + cmdType + ", notifyId = " + notifyId);

            // 循环获取所有的数据
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    String content = bundle.getString(key);
                    Log.i(TAG, "loop receive data from push, key = " + key + ", content = " + content);
                }
            }
        } else {
            Log.i(TAG, "intent = null");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getIntentData(intent);
    }
}