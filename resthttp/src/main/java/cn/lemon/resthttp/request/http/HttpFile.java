package cn.lemon.resthttp.request.http;

import android.os.Handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import cn.lemon.resthttp.request.RequestDispatcher;
import cn.lemon.resthttp.request.callback.HttpCallback;
import cn.lemon.resthttp.util.HttpLog;

/**
 * Created by linlongxin on 2016/7/10.
 */

public class HttpFile {

    /**
     * 消息体中的分隔符
     */
    private final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";

    private static HttpFile instance;

    private Handler handler;

    private HttpFile() {
        handler = new Handler();
    }

    public static HttpFile getInstance() {
        if (instance == null) {
            instance = new HttpFile();
        }
        return instance;
    }

    public void uploadFile(final String path, final Map<String, String> params, final String fileParamName, final File file, final HttpCallback callback) {
        RequestDispatcher.getInstance().executeRunnable(new Runnable() {
            @Override
            public void run() {
                uploadFileTask(path, params, fileParamName, file, callback);
            }
        });
    }

    /**
     * 上传文件
     */
    private void uploadFileTask(String path, Map<String, String> params, String fileParamName, File file, final HttpCallback callback) {
        try {
            URL url = new URL(path);
            final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            StringBuffer messageForm = new StringBuffer();

            /**
             * 普通参数表单提交
             */
            if (params != null) {
                for (String key : params.keySet()) {
                    messageForm.append("--").append(BOUNDARY).append("\r\n");
                    messageForm.append("Content-Disposition: form; name=\"").append(key).append("\"").append("\r\n");
                    messageForm.append("\r\n");
                    messageForm.append(params.get(key)).append("\r\n");
                }
            }

            String uploadFileName = file.getName();
            /**
             * 提交文件表单
             */
            messageForm.append("--").append(BOUNDARY).append("\r\n");
            messageForm.append("Content-Disposition: form-data; name=\"").append(fileParamName).append("\"; filename=\"")
                    .append(uploadFileName).append("\"").append("\r\n");
            messageForm.append("Content-Type: image/jpeg").append("\r\n");
            messageForm.append("\r\n");

            /**
             * 以--boundary--结束，这里是中间空了两个空行。。。坑死我了。。
             */
            String endInfo = "\r\n--" + BOUNDARY + "--\r\n";

            int messageFormLength = messageForm.length();
            HttpLog.Log("上传文件消息体\n" + messageForm.toString() + endInfo + "\n");

            httpURLConnection.setRequestMethod("POST");
            /**
             * 设置请求头：multipart/form-data
             */
            long contentLength = messageFormLength + file.length() + endInfo.length();
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            httpURLConnection.setRequestProperty("Content-Length", contentLength + "");
            httpURLConnection.setDoOutput(true);
            OutputStream outToServer = httpURLConnection.getOutputStream();

            HttpLog.Log("ContentLength : " + contentLength);
            /**
             * 发送消息体到服务器端
             */
            outToServer.write(messageForm.toString().getBytes());

            /**
             * 发送文件字节流到服务器端
             */
            InputStream in = new FileInputStream(file);
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                outToServer.write(buffer, 0, len);
            }

            outToServer.write(endInfo.getBytes()); //结束字段
            outToServer.close();
            in.close();

            final String result = inputStreamToString(httpURLConnection.getInputStream());

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.success(result);
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback.failure(httpURLConnection.getResponseCode(), result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
            HttpLog.Log("异常：" + e.getMessage());
        }

    }

    /**
     * 读取输入流信息，转化成String
     */
    private String inputStreamToString(InputStream in) {
        String result = "";
        String line;
        if (in != null) {
            BufferedReader bin = new BufferedReader(new InputStreamReader(in));
            try {
                while ((line = bin.readLine()) != null) {
                    result += line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
