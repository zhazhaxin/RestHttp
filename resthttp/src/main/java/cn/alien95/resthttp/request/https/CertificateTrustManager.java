package cn.alien95.resthttp.request.https;


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import cn.alien95.resthttp.util.RestHttpLog;

/**
 * 自签名证书信任管理
 */
class CertificateTrustManager implements X509TrustManager {
    X509Certificate cert;

    CertificateTrustManager(X509Certificate cert) {
        this.cert = cert;
    }

    @Override
    //校验客户端
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @Override
    //校验服务器端证书和和代码中 hard code 的 CRT 证书相同，windows到处来的是cer格式证书
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // 确认服务器端证书
        RestHttpLog.i("chain.length : " + chain.length);
        for(X509Certificate c : chain){
            RestHttpLog.i("name : " + c.getSigAlgName() + " id :" + c.getSigAlgOID());
            if(c.equals(cert)){
                RestHttpLog.i("checkServerTrusted : Certificate from server is valid!");
                return;
            }
        }
        throw new CertificateException("checkServerTrusted : No trusted server cert found!");
    }


    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

}
