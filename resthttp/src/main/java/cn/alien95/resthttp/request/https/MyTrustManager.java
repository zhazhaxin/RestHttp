package cn.alien95.resthttp.request.https;


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

class MyTrustManager implements X509TrustManager {
      X509Certificate cert;

      MyTrustManager(X509Certificate cert) {
          this.cert = cert;
      }

      @Override
      // for server only
      public void checkClientTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
          // 我们在客户端只做服务器端证书校验。
      }

      @Override
      // only trust the given certificate or certificate issued by it
      public void checkServerTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
          // 确认服务器端证书的 Intermediate CRT 和代码中 hard code 的 CRT 证书主体一致。
          if (!chain[0].getIssuerDN().equals(cert.getSubjectDN())) {
              throw new CertificateException("Parent certificate of server was different than expected signing certificate");
          }

          try {
              // 确认服务器端证书被代码中 hard code 的 Intermediate CRT 证书的公钥签名。
              chain[0].verify(cert.getPublicKey());
	
              // 确认服务器端证书没有过期
              chain[0].checkValidity();
          } catch (Exception e) {
              throw new CertificateException("Parent certificate of server was different than expected signing certificate");
          }
      }


    @Override
      public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
      }

  }
