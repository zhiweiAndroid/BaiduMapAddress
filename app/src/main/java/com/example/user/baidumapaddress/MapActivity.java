package com.example.user.baidumapaddress;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.radar.RadarNearbyResult;
import com.baidu.mapapi.radar.RadarNearbySearchOption;
import com.baidu.mapapi.radar.RadarSearchError;
import com.baidu.mapapi.radar.RadarSearchListener;
import com.baidu.mapapi.radar.RadarSearchManager;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;


public class MapActivity extends AppCompatActivity implements View.OnClickListener , RadarSearchListener, MyOrientationListener.OnOrientationListener , OnGetGeoCoderResultListener {

    private BaiduMap mBaiduMap;
    private MapView mMapView;
    private LocationClient mLocationClient;
    private boolean isFirstLocation = true;
    private MyLocationListener mLocationListener;
    private LocationClientOption option;
    private TextView mAddress;
    private ImageView mIvLocation;
    private RadarSearchManager radarSearchManagermManager;
    private MarkerOptions markerOption;
    private double mCurrentLantitude;
    private double mCurrentLongitude;
    private float orientation_x = 100f;
    private MyOrientationListener orientationListener;
    private LatLng lastLatLng;
    private RelativeLayout rlCompleteLocation;
    private BMapManager mapManager;
    private GeoCoder mSearch;
    private RelativeLayout mTvdingwei;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);
        // 初始化MapActivity
        mapManager = new BMapManager();
        // init方法的第一个参数需填入申请的API Key
        mapManager.init();

        initView();
        initListener();
        initMap();

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

    }

    private BaiduMap.OnMapStatusChangeListener changeListener=new BaiduMap.OnMapStatusChangeListener() {

        @Override
        public void onMapStatusChangeStart(MapStatus arg0) {
            mTvdingwei.setVisibility(View.INVISIBLE);
        }
        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
        }

        @Override
        public void onMapStatusChange(MapStatus arg0) {
        }

        @Override
        public void onMapStatusChangeFinish(MapStatus arg0) {
            //此处进行操作
            int[] location = new int[2];
            mMapView.getLocationOnScreen(location);
            Point p=new Point(location[0]+mMapView.getWidth()/2, location[1]+mMapView.getHeight()/2);
            //TODO 已经获取到屏幕中心经纬度，可上传或者地理转码
            LatLng latLng=new LatLng(arg0.target.latitude,arg0.target.longitude);
            Log.i("location",latLng.toString());
            mTvdingwei.setVisibility(View.VISIBLE);
            animatorSet(MapActivity.this,rlCompleteLocation);
           // Toast.makeText(MapActivity.this,latLng.toString(),Toast.LENGTH_SHORT).show();
            // 反Geo搜索
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(latLng).newVersion(0));
        }

    };
    /**
     * 指定定位图标，默认为电动车
     *
     * @return
     */
    protected int initDescriptor() {
        return R.mipmap.gerendingwei;
    }
    /**
     * 指定定位展现形式，默认为跟随
     *
     * @return
     */
    protected MyLocationConfiguration.LocationMode initLocationMode() {
        return MyLocationConfiguration.LocationMode.NORMAL;
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mIvLocation = findViewById(R.id.iv_location);
        mAddress = findViewById(R.id.tv_address);
        mMapView = findViewById(R.id.bd_map);
        mTvdingwei = findViewById(R.id.ll_dingwei);
        rlCompleteLocation = findViewById(R.id.rl_complete_location);
        // 不显示缩放比例尺
        mMapView.showZoomControls(false);
        // 不显示百度地图Logo
        mMapView.removeViewAt(1);
        //百度地图
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(initLocationMode(), true, BitmapDescriptorFactory.fromResource(initDescriptor())));
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setOnMapStatusChangeListener(changeListener);
        refreshLocation(true);
        // 初始化传感器
        initOritationListener();
        //改变地图状态，使地图显示在恰当的缩放大小
        MapStatus mMapStatus = new MapStatus.Builder().zoom(15).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }

    private void initOritationListener() {
        orientationListener = new MyOrientationListener(getApplicationContext());
        orientationListener.setOnOrientationListener(this);
        orientationListener.start();
    }

    private void initListener() {
        mIvLocation.setOnClickListener(this);
        findViewById(R.id.btn_sure).setOnClickListener(this);
    }

    /**
     * 初始化地图
     */
    private void initMap() {

        UiSettings settings = mBaiduMap.getUiSettings();
        settings.setOverlookingGesturesEnabled(false);//关闭一切手势操作   屏蔽双指下拉时变成3D地图  

        //定位客户端的设置
        radarSearchManagermManager = RadarSearchManager.getInstance();
        radarSearchManagermManager.setUserID("");
        mLocationClient = new LocationClient(this);
        mLocationListener = new MyLocationListener();
        //注册监听
        mLocationClient.registerLocationListener(mLocationListener);
        initLocationOption();

        mLocationClient.setLocOption(option);
        mLocationClient.start();
        /*
         * 当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。调用requestLocation(
         * )后，每隔设定的时间，定位SDK就会进行一次定位。如果定位SDK根据定位依据发现位置没有发生变化，就不会发起网络请求，
         * 返回上一次定位的结果；如果发现位置改变，就进行网络请求进行定位，得到新的定位结果。
         * 定时定位时，调用一次requestLocation，会定时监听到定位结果。
         */
        mLocationClient.requestLocation();
    }

    private void initLocationOption() {
        //配置定位
        option = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(5000);
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，默认false,设置是否使用gps
        option.setOpenGps(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setLocationNotify(false);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setEnableSimulateGps(false);
    }



    @Override
    protected void onStop() {
        super.onStop();
        //关闭定位
        mBaiduMap.setMyLocationEnabled(false);
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mBaiduMap.setMyLocationEnabled(true);
        mMapView.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_location:
                ininCurrentLocation();
                break;
            case R.id.btn_sure:
                Toast.makeText(this, "成功", Toast.LENGTH_LONG).show();
                break;


        }
    }

    /**
     * 定位当前的位置
     */
    private void ininCurrentLocation() {
        isFirstLocation = true;
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(getApplicationContext());
            mLocationClient.setLocOption(option);
        }
        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    @Override
    public void onOrientationChanged(float x) {
        orientation_x = x;
        refreshLocation();
    }



    //自定义的定位监听
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //将获取的location信息给百度map
            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();

            mBaiduMap.setMyLocationData(data);
            lastLatLng=new LatLng(location.getLatitude(),location.getLongitude());
            mCurrentLantitude=location.getLatitude();
            mCurrentLongitude=location.getLongitude();
            if (isFirstLocation) {
                //获取经纬度
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(ll);
                //mBaiduMap.setMapStatus(status);//直接到中间
                mBaiduMap.animateMapStatus(status);//动画的方式到中间
                isFirstLocation = false;

                initRadar(ll);

//                Toast.makeText(MapActivity.this, "位置：" + location.getAddrStr(), Toast.LENGTH_LONG).show();
            }

        }

    }



    // 雷达周边搜索
    private void initRadar(LatLng latLng) {
        radarSearchManagermManager.removeNearbyInfoListener(this);
        RadarNearbySearchOption radius = new RadarNearbySearchOption()
                .centerPt(latLng).pageNum(0).radius(1000).pageCapacity(10);
        radarSearchManagermManager.nearbyInfoRequest(radius);
        radarSearchManagermManager.addNearbyInfoListener(this);

    }

    /**
     * 监听雷达附近返回点，并且显示到地图上
     *
     * @param result 监听事件返回结果
     * @param error
     */
    @Override
    public void onGetNearbyInfoList(RadarNearbyResult result, RadarSearchError error) {
        if (error == RadarSearchError.RADAR_NO_ERROR) {
            for (int i = 0; i < result.infoList.size(); i++) {
                if (result.infoList.get(i).comments.equals("2")) {
                    BitmapDescriptor fromResource = BitmapDescriptorFactory
                            .fromResource(R.mipmap.icon_che);
                    markerOption = new MarkerOptions().icon(fromResource).position(
                            result.infoList.get(i).pt);
                } else {
                    BitmapDescriptor fromResource = BitmapDescriptorFactory
                            .fromResource(R.mipmap.icon_silun);
                    markerOption = new MarkerOptions().icon(fromResource).position(
                            result.infoList.get(i).pt);
                }
                Bundle des = new Bundle();
                markerOption.extraInfo(des);
                mBaiduMap.addOverlay(markerOption);
            }
        } else {
        }
    }

    @Override
    public void onGetUploadState(RadarSearchError radarSearchError) {
        radarSearchManagermManager.destroy();
        radarSearchManagermManager = null;
    }

    @Override
    public void onGetClearInfoState(RadarSearchError radarSearchError) {

    }




    /**
     * 初始化方向传感器
     */
    protected void refreshLocation() {
        refreshLocation(false);
    }

    protected void refreshLocation(boolean follow) {
        if (mBaiduMap == null) {
            return;
        }

        MyLocationData locationData = new MyLocationData.Builder()
                .accuracy(0)
                .direction(orientation_x)
                .latitude(mCurrentLantitude)
                .longitude(mCurrentLongitude)
                .build();

        mBaiduMap.setMyLocationData(locationData);

        if (follow) {
            if (MyLocationConfiguration.LocationMode.FOLLOWING != mBaiduMap.getLocationConfiguration().locationMode) {
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(lastLatLng);
                mBaiduMap.animateMapStatus(update);
            }
        }
    }

    //简单位移动画
    public static void animatorSet(Context context, View view) {
        AnimatorSet animationSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0, DensityUtils.dp2px(context, -50));
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "translationY", -50, DensityUtils.dp2px(context, 0));
        animationSet.playTogether(animator);
        animationSet.playTogether(animator1);
        animationSet.setDuration(500);
        animationSet.start();
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MapActivity.this, "抱歉，未能找到结果", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
//        mBaiduMap.clear();
//        mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
//                .icon(BitmapDescriptorFactory
//                        .fromResource(R.mipmap.gerendingwei)));
//        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
//                .getLocation()));
       // Toast.makeText(MapActivity.this, result.getAddress()+" adcode: "+result.getAdcode(),Toast.LENGTH_LONG).show();

        mAddress.setText("位置：" + result.getAddress());
    }



}
