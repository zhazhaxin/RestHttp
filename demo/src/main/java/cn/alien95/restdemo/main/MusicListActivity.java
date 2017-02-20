package cn.alien95.restdemo.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.alien95.restdemo.R;
import cn.alien95.resthttp.request.callback.RestCallback;
import cn.alien95.resthttp.request.rest.RestFactory;
import cn.alien95.restdemo.data.Config;
import cn.alien95.restdemo.data.ServiceAPI;
import cn.alien95.restdemo.data.bean.Music;
import cn.alien95.restdemo.main.music.MusicAdapter;
import cn.alien95.util.Utils;
import cn.lemon.view.RefreshRecyclerView;

public class MusicListActivity extends AppCompatActivity {

    private RefreshRecyclerView mRecyclerView;
    private MusicAdapter adapter;
    private ServiceAPI serviceAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_actiivty_list);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        RestFactory restRequest = new RestFactory.Builder()
                .baseUrl(Config.MUSIC_HOST)
                .build();
        serviceAPI = (ServiceAPI) restRequest.create(ServiceAPI.class);

        mRecyclerView = (RefreshRecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setAdapter(adapter = new MusicAdapter(this));
        adapter.colseLog();

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.showSwipeRefresh();
                getData(5);
            }
        });
    }

    public void getData(int topId) {
        serviceAPI.getMusicData(topId, "19069", "21eb3605b599408192531ff4c89eec68", new RestCallback<String>() {
            @Override
            public void callback(String result) {
                try {
                    JSONObject allData = new JSONObject(result);
                    JSONObject body = allData.getJSONObject("showapi_res_body");
                    JSONObject javaBean = body.getJSONObject("pagebean");
                    JSONArray array = javaBean.getJSONArray("songlist");
                    List<Music> data = new ArrayList<Music>();
                    for (int i = 0; i < array.length(); i++) {
                        Music music = new Music();
                        JSONObject jsonObject = (JSONObject) array.get(i);
                        try {
                            music.setAlbumpic_big(jsonObject.getString("albumpic_big"));
                        } catch (JSONException e) {
                            music.setAlbumpic_big("");
                            Utils.Log("没有albumpic_big字段");
                        }

                        try {
                            music.setAlbumpic_small(jsonObject.getString("albumpic_small"));
                        } catch (JSONException e) {
                            music.setAlbumpic_small("");
                            Utils.Log("没有albumpic_small字段");
                        }
                        try {
                            music.setDownUrl(jsonObject.getString("downUrl"));
                        } catch (JSONException e) {
                            Utils.Log("没有downUrl");
                        }
                        try {
                            music.setSingername(jsonObject.getString("singername"));
                        } catch (JSONException e) {
                            Utils.Log("没有singername");
                        }
                        try {
                            music.setSeconds(jsonObject.getInt("seconds"));
                        } catch (JSONException e) {
                            Utils.Log("没有Seconds");
                        }
                        try {
                            music.setSongname(jsonObject.getString("songname"));
                        } catch (JSONException e) {
                            Utils.Log("没有songname");
                        }
                        try {
                            music.setUrl(jsonObject.getString("url"));
                        } catch (JSONException e) {
                            Utils.Log("没有url");
                        }
                        data.add(music);
                    }
                    adapter.addAll(data);
                    mRecyclerView.dismissSwipeRefresh();
                    adapter.showNoMore();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Utils.Log("数据解析错误");
                }
            }
        });
    }


}

