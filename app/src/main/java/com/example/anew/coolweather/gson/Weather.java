package com.example.anew.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by New on 2017/10/24.
 */

public class Weather {
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")//包含的是一个数组，需要List集合引用该类
    public List<Forecast> forecastList;
}
