package com.example.anew.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.anew.coolweather.gson.Forecast;
import com.example.anew.coolweather.gson.Weather;
import com.example.anew.coolweather.util.HttpUtil;
import com.example.anew.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;


    private String weatherString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            //获取配置视图实例
            View decorView=getWindow().getDecorView();
            //让布局显示到状态栏上
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //设置状态栏为透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        //  region 初始化各个控件
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);

        bingPicImg=(ImageView)findViewById(R.id.bing_pic);

        //侧滑菜单
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_button);

        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.swip_refresh);
        //下拉颜色
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        //endregion


        //每个应用都有一个默认的配置文件preferences.xml，使用getDefaultSharedPreferences获取。
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        //负责读取存在的数据（Editor写入的缓冲数据）
        weatherString = prefs.getString("weather",null);


        //必应图片显示
        String bingPic =prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
        }
        else{
            loadBingPic();
        }
        //在手动下拉刷新时，再次向服务器请求数据显示到界面
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        //打开侧滑菜单
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        if(weatherString!=null){  //判断缓冲
            //有缓冲时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            //更新天气
            mWeatherId=weather.basic.weatherId;
            //mWeatherId=getIntent().getStringExtra("weather_id");
            showWeatherInfo(weather);
        }
        else{
            //无缓冲时去服务器查询天气,获取传递过来的天气code
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);

        }


    }



    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId){
        //天气请求地址
        String weatherUrl ="http://guolin.tech/api/weather?cityid="
                +weatherId+"&key=6f912ed632f8436a835ba5442fea3b1b";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //设置为false表示刷新结束，隐藏进度条
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                //获取响应的JSON数据
                final String responseText = response.body().string();
                //解析JSON数据，返回转换后的对象weather
                final Weather weather = Utility.handleWeatherResponse(responseText);
                //在主线程更新UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //判断状态是否正确
                        if(weather!=null&&"ok".equals(weather.status)){
                            //获取默认的配置，Editor负责写入数据
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            //将数据写入，作为缓冲
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            //显示数据到布局
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });

            }
        });
        loadBingPic();
    }

    /**
     * 处理并显示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        //分割空格，取1个
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        degreeText.setText(degree);
        titleUpdateTime.setText(updateTime);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            //动态加载布局
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            //用加载的布局为对象引用控件
            TextView dateText = (TextView)view.findViewById(R.id.date_txt);
            TextView infoText=(TextView)view.findViewById(R.id.info_txt);
            TextView maxText=(TextView)view.findViewById(R.id.max_txt);
            TextView minText=(TextView)view.findViewById(R.id.min_txt);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi !=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort ="舒适度"+weather.suggestion.comfort.info;
        String carWash = "洗车指数"+weather.suggestion.carWash.info;
        String sport ="运动建议"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 加载必应每日一图
     */

    private void loadBingPic() {

        String requestBindPic="http://guolin.tech/api/bing_pic";
        //通过地址去请求数据
        HttpUtil.sendOkHttpRequest(requestBindPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //获取响应的数据，这里响应的数据时必应每日一图的最新图片地址
                final String bingpc =response.body().string();
                //通过本地缓冲将获取的图片链接保存下来，供其他使用
                SharedPreferences.Editor editor =PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingpc);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingpc).into(bingPicImg);
                    }
                });

            }
        });
    }
}
