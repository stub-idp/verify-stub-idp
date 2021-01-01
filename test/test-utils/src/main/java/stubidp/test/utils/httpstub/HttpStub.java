package stubidp.test.utils.httpstub;

import java.util.Comparator;
import java.util.Optional;

public class HttpStub extends AbstractHttpStub {

    public HttpStub() {
        this(RANDOM_PORT);
    }

    public HttpStub(int port) {
        super(port);
    }

    @Override
    public StubHandler createHandler() {
        return new Handler();
    }

    private class Handler extends StubHandler {

        @Override
        protected void recordRequest(RecordedRequest recordedRequest) {
            recordedRequests.add(recordedRequest);
        }

        @Override
        public Optional<RequestAndResponse> findResponse(ReceivedRequest receivedRequest) {
            return requestsAndResponses
                    .stream()
                    .filter(requestAndResponse -> requestAndResponse.getRequest().applies(receivedRequest))
                    .min(Comparator.comparingInt(RequestAndResponse::callCount));
        }

    }
}
