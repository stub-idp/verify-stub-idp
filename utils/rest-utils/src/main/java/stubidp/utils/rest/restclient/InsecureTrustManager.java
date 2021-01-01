package stubidp.utils.rest.restclient;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class InsecureTrustManager implements X509TrustManager {

    private static final X509Certificate[] X509_CERTIFICATES = new X509Certificate[0];

    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
        // Everyone is trusted!
    }

    public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
        // Everyone is trusted!
    }

    public X509Certificate[] getAcceptedIssuers() {
        return X509_CERTIFICATES;
    }
}
