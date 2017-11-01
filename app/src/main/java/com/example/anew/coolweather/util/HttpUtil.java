package com.example.anew.coolweather.util;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by New on 2017/10/18.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        Log.d("TAG", "sendOkHttpRequest: "+address);
        OkHttpClient client =new OkHttpClient();
        Request request =new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
