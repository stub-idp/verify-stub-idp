package stubidp.test.utils.httpstub;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReceivedRequest {
    protected final String requestURI;
    protected final Map<String, List<String>> headers;
    protected final String querystring;
    protected final String method;
    protected final String url;
    protected byte[] entity;

    public ReceivedRequest(HttpServletRequest request) {
        this.method = request.getMethod();
        this.url = request.getRequestURL().toString();
        this.querystring = request.getQueryString();
        this.requestURI = request.getRequestURI();
        this.headers = getHeaders(request);
        this.entity = readEntity(request);
    }

    protected static byte[] readEntity(HttpServletRequest request) {
        try (InputStream in = request.getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Map<String, List<String>> getHeaders(HttpServletRequest request) {
        Map<String, List<String>> _headers = new HashMap<>();
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String headerName = headerNames.nextElement();
            if(_headers.containsKey(headerName.toLowerCase())) {
                throw new RuntimeException("duplicateheaders");
//                _headers.put(headerName.toLowerCase(), Collections.<String>list(request.getHeaders(headerName)));
            } else {
                _headers.put(headerName.toLowerCase(), Collections.<String>list(request.getHeaders(headerName)));
            }
        }
        return _headers;
    }

    public String getPath() {
        return requestURI;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        if(headers.containsKey(name.toLowerCase())) {
            final List<String> strings = headers.get(name.toLowerCase());
            if(Objects.nonNull(strings) && strings.size() > 0) {
                return strings.get(0);
            }
        }
        return null;
    }

    public String getQuerystring() {
        return querystring;
    }

    public String getMethod() {
        return method;
    }

    /**
     * @return raw entity bytes, could be compressed etc
     */
    public byte[] getEntityBytes() {
        return entity;
    }

}
