package stubidp.utils.rest.common;

public interface HttpHeaders {
    String CACHE_CONTROL_KEY = org.apache.http.HttpHeaders.CACHE_CONTROL; //"Cache-Control"
    String CACHE_CONTROL_NO_CACHE_VALUE = "no-cache, no-store";
    String PRAGMA_KEY = org.apache.http.HttpHeaders.PRAGMA;
    String PRAGMA_NO_CACHE_VALUE = "no-cache";
    String MAX_AGE = "max-age";
    String REFERER = org.apache.http.HttpHeaders.REFERER;
    String X_FORWARDED_FOR = "X-Forwarded-For";
}
