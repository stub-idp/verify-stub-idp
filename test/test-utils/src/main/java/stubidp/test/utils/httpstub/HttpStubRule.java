package stubidp.test.utils.httpstub;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import stubidp.test.utils.httpstub.builders.ExpectedRequestBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

public class HttpStubRule implements AfterAllCallback {

    private final AbstractHttpStub httpStub;

    public HttpStubRule() {
        this(new HttpStub());
    }

    protected HttpStubRule(AbstractHttpStub httpStub) {
        this.httpStub = httpStub;
        this.httpStub.start();
    }

    public int getPort() {
        return httpStub.getHttpPort();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        httpStub.stop();
    }

    public URI uri(String path) {
        return baseUri().path(path).build();
    }

    public UriBuilder baseUri() {
        return UriBuilder.fromUri("http://localhost").port(getPort());
    }

    public void register(String path, int responseStatus) {
        RequestAndResponse requestAndResponse = ExpectedRequestBuilder.expectRequest().withPath(path).andWillRespondWith().withStatus(responseStatus).build();
        register(requestAndResponse);
    }

    public void register(String path, int responseStatus, String contentType, String responseBody) {
        RequestAndResponse requestAndResponse = ExpectedRequestBuilder.expectRequest().withPath(path).andWillRespondWith().withStatus(responseStatus).withContentType(contentType).withBody(responseBody).build();
        register(requestAndResponse);
    }

    public void register(String path, int status, Object responseEntity) throws JsonProcessingException {
        RequestAndResponse requestAndResponse = ExpectedRequestBuilder.expectRequest().withPath(path).andWillRespondWith().withStatus(status).withBody(responseEntity).build();
        register(requestAndResponse);
    }

    public void register(String path, RegisteredResponse registeredResponse) {
        register(ExpectedRequestBuilder.expectRequest().withPath(path).build(), registeredResponse);
    }

    public void register(ExpectedRequest expectedRequest, RegisteredResponse registeredResponse) {
        RequestAndResponse requestAndResponse = new RequestAndResponse(expectedRequest, registeredResponse);
        register(requestAndResponse);
    }

    public void register(RequestAndResponse requestAndResponse) {
        httpStub.register(requestAndResponse);
    }

    public void reset() {
        httpStub.reset();
    }

    public long getCountOfRequestsTo(final String path) {
        return httpStub.getCountOfRequestsTo(path);
    }

    public int getCountOfRequests() {
        return httpStub.getCountOfRequests();
    }

    public RecordedRequest getLastRequest() {
        return httpStub.getLastRequest();
    }

    public List<RecordedRequest> getRecordedRequest() {
       return httpStub.getRecordedRequests();
    }
}
