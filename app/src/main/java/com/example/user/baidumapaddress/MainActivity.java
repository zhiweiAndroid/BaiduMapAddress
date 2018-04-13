package com.example.user.baidumapaddress;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Button  btn = findViewById(R.id.btn_map);
        btn.setOnClickListener(this);
        TextView tvAddress = findViewById(R.id.tv_address);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_map:
                initLocationPermission();
                break;
        }
    }

    private void initLocationPermission() {
        RxPermissions rxPermissions=new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean){
                            startActivity(new Intent(MainActivity.this,MapActivity.class));
                        }else {

                        }
                    }
                });

    }



}
