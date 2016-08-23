package cn.alien95.resthttplibrary.music;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.alien95.resthttp.request.rest.RestRequest;
import cn.alien95.resthttp.request.rest.callback.RestCallback;
import cn.alien95.resthttplibrary.R;
import cn.alien95.resthttplibrary.config.Config;
import cn.alien95.resthttplibrary.data.ServiceAPI;
import cn.alien95.resthttplibrary.data.bean.Music;
import cn.alien95.util.Utils;
import cn.alien95.view.RefreshRecyclerView;

public class MusicListActivity extends AppCompatActivity {

    private RefreshRecyclerView refreshRecyclerView;
    private MusicAdapter adapter;
    private RestRequest restRequest;
    private ServiceAPI serviceAPI;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_actiivty_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        restRequest = new RestRequest.Builder()
                .baseUrl(Config.MUSIC_BASE_URL)
                .build();
        serviceAPI = (ServiceAPI) restRequest.create(ServiceAPI.class);

        refreshRecyclerView = (RefreshRecyclerView) findViewById(R.id.recycler_view);
        refreshRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        refreshRecyclerView.setAdapter(adapter = new MusicAdapter(this));

        getData(5);
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
                    Utils.Log("music--length : " + array.length());
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
                        adapter.add(music);
                        Utils.Log(music.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Utils.Log("数据解析错误");
                }
            }
        });
    }


}

