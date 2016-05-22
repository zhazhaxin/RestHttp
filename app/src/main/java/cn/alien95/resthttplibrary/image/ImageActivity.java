package cn.alien95.resthttplibrary.image;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import cn.alien95.resthttplibrary.R;
import cn.alien95.view.RefreshRecyclerView;

public class ImageActivity extends AppCompatActivity {

    private RefreshRecyclerView refreshRecyclerView;
    private ImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        refreshRecyclerView = (RefreshRecyclerView) findViewById(R.id.recycler_view);

        refreshRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        refreshRecyclerView.setAdapter(adapter = new ImageAdapter(this));

        adapter.addAll(new String[]{
                "http://i02.pictn.sogoucdn.com/3c28af542f2d49f7-fe9c78d2ff4ac332-fe56d6b51daa9e7d5a5cda4d58ce5b71",
                "http://i03.pictn.sogoucdn.com/3c28af542f2d49f7-8f0182a4cf50287e-06eecdc5e2d27882e3324c12cffee88c",
                "http://i04.pictn.sogoucdn.com/3c28af542f2d49f7-8437bbc8e07dde51-5383e0be36b683ff9d071ee78de0f698",
                "http://i01.pictn.sogoucdn.com/3c28af542f2d49f7-fe9c78d2ff4ac332-2929872e8dc4b24817d8cd86532e1790",
                "http://i01.pictn.sogoucdn.com/3c28af542f2d49f7-9e7c5d699eaea93e-0a56dee26260de08b633403a367221d8",
                "http://i01.pictn.sogoucdn.com/3c28af542f2d49f7-fe9c78d2ff4ac332-f857690f48adb5b8175bb76589d11d33",
                "http://i01.pictn.sogoucdn.com/3c28af542f2d49f7-fe9c78d2ff4ac332-11bc7b6b4fbaaf4c2200a5dfb6e34477",
                "http://i01.pictn.sogoucdn.com/3c28af542f2d49f7-9e7c5d699eaea93e-a233778f49b9e9f4865bcb66a24c7486",
                "http://i04.pictn.sogoucdn.com/3c28af542f2d49f7-fe9c78d2ff4ac332-ca9d199af039967b6e65e914bde18357",
                "http://i04.pictn.sogoucdn.com/3c28af542f2d49f7-8437bbc8e07dde51-710e73d1180a60338d03392955e7419a",
                "http://i04.pictn.sogoucdn.com/3c28af542f2d49f7-8437bbc8e07dde51-bf281c07635ca67fd013a5fb041fac99"
        });
    }
}
