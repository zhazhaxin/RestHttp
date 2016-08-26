package cn.alien95.resthttp.request.https;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import cn.alien95.resthttp.request.Request;

/**
 * Created by linlongxin on 2016/8/25.
 */

public class SelfSignHttpsConnection extends HttpsConnection {

    //天坑。。。，这里必须是static，因为setCertificate()一般调用在主线程，而getSSLContext则会调用在子线程
    private static InputStream mCertificateInputStream;

    public static SelfSignHttpsConnection getInstance() {
        return getInstance(SelfSignHttpsConnection.class);
    }

    public void request(Request request) {
        String url = request.url;
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(url).openConnection();
            urlConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
            requestURLConnection(urlConnection, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setCertificate(InputStream certificateIn) {
        mCertificateInputStream = certificateIn;
    }

    public SSLContext getSSLContext() {
        if (mCertificateInputStream != null) {
            SSLContext ssl = null;
            try {
                ssl = SSLContext.getInstance("TLS");
                TrustManager tm = new CertificateTrustManager(getX509Certificate(mCertificateInputStream));
                ssl.init(null, new TrustManager[]{tm}, null);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();

            } catch (KeyManagementException e) {
                e.printStackTrace();

            }
            return ssl;
        } else {
            throw new NullPointerException("mCertificateInputStream == null , please invoke setCertificate()");
        }
    }

    private static X509Certificate getX509Certificate(InputStream caInput) {
        X509Certificate cert = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(caInput);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                caInput.close();
            } catch (Throwable ex) {
            }
        }
        return cert;
    }
}
