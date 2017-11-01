package com.example.anew.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by New on 2017/10/24.
 */

public class Basic {
    /**Gson 解析的时候是根据字段名来匹配的
     * 注解，在对象转json或json转对象时，字段名自动替换注解名，让数据正确流入
     */
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }


}
