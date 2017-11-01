package com.example.anew.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by New on 2017/10/24.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;
    //数组

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
