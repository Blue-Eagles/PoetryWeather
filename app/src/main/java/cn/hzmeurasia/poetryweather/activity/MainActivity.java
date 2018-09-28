package cn.hzmeurasia.poetryweather.activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import cn.hzmeurasia.poetryweather.MyApplication;
import cn.hzmeurasia.poetryweather.R;
import cn.hzmeurasia.poetryweather.adapter.CardAdapter;
import cn.hzmeurasia.poetryweather.db.CityDb;
import cn.hzmeurasia.poetryweather.entity.CardEntity;
import cn.hzmeurasia.poetryweather.entity.SearchCityEntity;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private TextView tvTip;

    private List<CityDb> cityDbList = new ArrayList<>();
    private CardAdapter cardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        //控件初始化
        tvTip = findViewById(R.id.tv_tip);
        //注册和风天气
        HeConfig.init("HE1808181021011344","c6a58c3230694b64b78facdebd7720fb");
        HeConfig.switchToFreeServerNode();
        //主页城市列表本地数据库创建
        LitePal.getDatabase();
        //启用toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //注册EventBus事件
        EventBus.getDefault().register(this);

        //载入左滑提示按钮
        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //显示出按钮
            actionBar.setDisplayHomeAsUpEnabled(true);
            //载入按钮图片
            actionBar.setHomeAsUpIndicator(R.drawable.ic_person1);
        }
        //悬浮按钮点击事件
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchCityActivity.class);
                startActivity(intent);
            }
        });

        //判断是否添加了城市
        isCardEmpty();
        RecyclerView recyclerView = findViewById(R.id.rv_main);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        //查询数据库
        cityDbList = LitePal.findAll(CityDb.class);
        cardAdapter = new CardAdapter(cityDbList);
        recyclerView.setAdapter(cardAdapter);
    }



    /**
     * 判断是否添加了城市
     */
    private void isCardEmpty() {
        Log.d(TAG, "isCardEmpty: "+LitePal.count(CityDb.class));
        if (LitePal.count(CityDb.class) == 0) {
            tvTip.setVisibility(View.VISIBLE);
        } else {
            tvTip.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * EventBus事件处理
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void searchCityEvent(final SearchCityEntity searchCityEntity) {
        Log.d(TAG, "Event: "+searchCityEntity.getCityCode());
        HeWeather.getWeatherNow(this, searchCityEntity.getCityCode(), new HeWeather.OnResultWeatherNowBeanListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i(TAG, "onError: ",throwable);
                Toast.makeText(MainActivity.this,"天气获取失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(List<Now> list) {
                Log.i(TAG, "onSuccess: "+ new Gson().toJson(list));
                if (LitePal.where("cityDb_cid = ?", searchCityEntity.getCityCode())
                        .count(CityDb.class) > 0) {
                    Toast.makeText(MainActivity.this,"您已添加过该城市",Toast.LENGTH_SHORT).show();
                }else {
                    for (Now now : list) {
                        CityDb cityDb = new CityDb();
                        cityDb.setCityDb_cid(now.getBasic().getCid());
                        cityDb.setCityDb_cityName(now.getBasic().getLocation());
                        cityDb.setCityDb_txt(now.getNow().getCond_txt());
                        cityDb.setCityDb_temperature(now.getNow().getFl());
                        cityDb.setCityDb_imageId(R.drawable.bg);
                        cityDb.save();
                        cityDbList.add(new CityDb(now.getBasic().getCid(), now.getBasic().getLocation(),
                                now.getNow().getCond_txt(), now.getNow().getFl(), R.drawable.bg));
                    }
                    //刷新Card视图
                    cardAdapter.notifyDataSetChanged();
                    isCardEmpty();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除注册EventBus
        EventBus.getDefault().unregister(this);
    }
}
