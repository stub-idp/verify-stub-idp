package stubidp.test.utils.httpstub;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;

public class ExpectedRequest {
    private final String path;
    private final String method;
    private Map<String, List<String>> headers;
    private String body;

    public ExpectedRequest(@Nullable String path, @Nullable String method, @Nullable Map<String, List<String>> headers, @Nullable String body) {
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

    public boolean applies(ReceivedRequest baseRequest) {
        if(path != null && !baseRequest.getPath().equals(path)) {
            return false;
        }
        if(method != null && !baseRequest.getMethod().equals(method)) {
            return false;
        }
        if(headers != null && notAllHeadersFound(baseRequest.getHeaders())) {
            return false;
        }
        if(body != null && !new String(baseRequest.getEntityBytes()).equals(body)) {
            return false;
        }
        return true;
    }

    private boolean notAllHeadersFound(Map<String, List<String>> _headers) {
        return !this.headers.keySet().stream()
                .allMatch(k -> _headers.keySet().contains(k) &&
                        _headers.get(k).containsAll(this.headers.get(k)));
    }
}
