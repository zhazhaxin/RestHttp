package cn.alien95.restdemo.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.tencent.wstt.gt.client.AbsGTParaLoader;
import com.tencent.wstt.gt.client.GT;
import com.tencent.wstt.gt.client.InParaManager;
import com.tencent.wstt.gt.client.OutParaManager;

import cn.alien95.restdemo.R;
import cn.alien95.util.Utils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        Button http = (Button) findViewById(R.id.http);
        Button https = (Button) findViewById(R.id.https);
        Button selfSignHttps = (Button) findViewById(R.id.self_sign_https);
        Button restHttp = (Button) findViewById(R.id.rest);
        Button uploadFile = (Button) findViewById(R.id.upload_file);
        Button images = (Button) findViewById(R.id.image);
        Button connectGT = (Button) findViewById(R.id.connect_gt);
        Button disconnectGT = (Button) findViewById(R.id.disconnect_gt);

        http.setOnClickListener(this);
        https.setOnClickListener(this);
        selfSignHttps.setOnClickListener(this);
        restHttp.setOnClickListener(this);
        uploadFile.setOnClickListener(this);
        images.setOnClickListener(this);
        connectGT.setOnClickListener(this);
        disconnectGT.setOnClickListener(this);
    }

    public void startActivity(Class clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.http:
                startActivity(cn.alien95.restdemo.main.HttpActivity.class);
                break;
            case R.id.https:
                startActivity(cn.alien95.restdemo.main.HttpsActivity.class);
                break;
            case R.id.self_sign_https:
                startActivity(cn.alien95.restdemo.main.SelfSignHttpsActivity.class);
                break;
            case R.id.rest:
                startActivity(cn.alien95.restdemo.main.MusicListActivity.class);
                break;
            case R.id.upload_file:
                startActivity(cn.alien95.restdemo.main.UploadFileActivity.class);
                break;
            case R.id.image:
                startActivity(cn.alien95.restdemo.main.ImageActivity.class);
                break;
            case R.id.connect_gt:
                connectGT();
                break;
            case R.id.disconnect_gt:
                disconnetGT();
                break;
        }
    }

    /**
     * 连接GT控制台
     */
    public void connectGT() {
        GT.connect(this, new AbsGTParaLoader() {
            @Override
            public void loadInParas(InParaManager inParaManager) {
                /**
                 * 注册输入参数，将在GT控制台上按顺序显示
                 */
                inParaManager.register("并发线程数", "TN", "3","2","1");
                inParaManager.register("KeepAlive", "KA", "true", "false");
                inParaManager.register("读超时", "读超时", "5000", "10000", "1000");
                inParaManager.register("连接超时", "连接超时", "5000", "10000", "1000");

                // 定义默认显示在GT悬浮窗的3个输入参数
                inParaManager.defaultInParasInAC("并发线程数", "KeepAlive", "读超时");

                // 设置默认无效的一个入参（GT1.1支持）
//                inParaManager.defaultInParasInDisableArea("连接超时");
            }

            @Override
            public void loadOutParas(OutParaManager outParaManager) {
                /**
                 * 注册输出参数，将在GT控制台上按顺序显示
                 */
                outParaManager.register("下载耗时", "耗时");
                outParaManager.register("实际带宽", "带宽");
                outParaManager.register("singlePicSpeed", "SSPD");
                outParaManager.register("NumberOfDownloadedPics", "NDP");

                // 定义默认显示在GT悬浮窗的3个输出参数
                outParaManager.defaultOutParasInAC("下载耗时", "实际带宽", "singlePicSpeed", "NumberOfDownloadedPics");
            }
        });

        // 默认在GT一连接后就展示悬浮窗（GT1.1支持）
        GT.setFloatViewFront(true);

        // 默认打开性能统计开关（GT1.1支持）
        GT.setProfilerEnable(true);
        Utils.Toast("连接GT成功");
    }

    public void disconnetGT(){
        GT.disconnect(getApplicationContext());
        Utils.Toast("已断开GT连接");
    }
}
