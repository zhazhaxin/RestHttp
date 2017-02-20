package cn.alien95.restdemo.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;

import cn.alien95.restdemo.R;
import cn.lemon.view.RefreshRecyclerView;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        RefreshRecyclerView refreshRecyclerView = (RefreshRecyclerView) findViewById(R.id.recycler_view);
        refreshRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageAdapter adapter;
        refreshRecyclerView.setAdapter(adapter = new ImageAdapter(this));
        adapter.colseLog();
        adapter.addAll(new String[]{
                "http://i02.pictn.sogoucdn.com/3c28af542f2d49f7-fe9c78d2ff4ac332-fe56d6b51daa9e7d5a5cda4d58ce5b71",
                "http://img01.sogoucdn.com/app/a/100520093/bddd017c1255d067-585cb7009cdde8e3-db1a7c3b141456ab2e2b64b38ac845cc.jpg",
                "http://i04.pictn.sogoucdn.com/3c28af542f2d49f7-8437bbc8e07dde51-5383e0be36b683ff9d071ee78de0f698",
                "http://img02.sogoucdn.com/app/a/100520093/a127e7d8adc3ae1b-0e5fe79cec4c9091-684379f4191cd77a5946f9055127a47f.jpg",
                "http://i01.pictn.sogoucdn.com/3c28af542f2d49f7-9e7c5d699eaea93e-0a56dee26260de08b633403a367221d8",
                "http://img04.sogoucdn.com/app/a/100520093/803d8006b5d521bb-2eb356b9e8bc4ae6-8819ee6cf56624da2b1c57b8d14b185e.jpg",
                "http://i01.pictn.sogoucdn.com/3c28af542f2d49f7-fe9c78d2ff4ac332-11bc7b6b4fbaaf4c2200a5dfb6e34477",
                "http://i02.pictn.sogoucdn.com/9447ce35247ebf23",
                "http://img04.sogoucdn.com/app/a/100520093/d52704b61c70ae76-8a6775c3c4e1c205-eba9fd6aef11db6e15132e3d9f2a12da.jpg",
                "http://i02.pictn.sogoucdn.com/8368f1c509adc5e9",
                "http://i04.pictn.sogoucdn.com/3c28af542f2d49f7-8437bbc8e07dde51-bf281c07635ca67fd013a5fb041fac99",
                "http://i03.pictn.sogoucdn.com/2cdca4ec6bbe565f"
        });
        adapter.showNoMore();
    }
}
