package com.example.anew.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anew.coolweather.db.City;
import com.example.anew.coolweather.db.County;
import com.example.anew.coolweather.db.Province;
import com.example.anew.coolweather.util.HttpUtil;
import com.example.anew.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by New on 2017/10/19.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE =0;

    public static final int LEVEL_CITY=1;

    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList =new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText =(TextView)view.findViewById(R.id.title_text);
        backButton =(Button)view.findViewById(R.id.back_button);
        listView =(ListView)view.findViewById(R.id.list_view);
        adapter =new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel ==LEVEL_PROVINCE){
                    selectedProvince =provinceList.get(position);
                    queryCities();
                }else if(currentLevel ==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    //
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){

                }else if(currentLevel==LEVEL_CITY){

                }

            }
        });
    }


    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces(){
        titleText.setText("中国");
        //按钮不可见
        backButton.setVisibility(View.GONE);
        //查询指定表的，所有数据
        provinceList = DataSupport.findAll(Province.class);
        //判断表中是否有数据
        if(provinceList.size()>0){
            //清理元素
            dataList.clear();
            //遍历集合对象
            for(Province province : provinceList){
                //把查询出来的数据添加进data集合
                dataList.add(province.getProvinceName());
                //适配器刷新
                adapter.notifyDataSetChanged();
                //将下标为0的值，显示在列表最上面
                listView.setSelection(0);
                //并把查询等级更改
                currentLevel=LEVEL_PROVINCE;
            }
        }else {
            //没有获取到任何数据，则去调用方法到服务器获取数据
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param address
     * @param type
     */

    private void queryFromServer(String address, final String type) {
        showProgressDislog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread（）方法返回主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //回调接口，获取接口返回的服务器数据
                String responseText = response.body().string();
                boolean result=false;
                //判断返回的类型，选择对应的解析方法
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    //切换到主线程，解析和处理已经获取到的数据，调用对应方法，显示出来。
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });

    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */

    private void queryCounties() {
    }


    private void queryCities() {
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDislog() {
        if(progressDialog!=null){
            progressDialog =new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

}
