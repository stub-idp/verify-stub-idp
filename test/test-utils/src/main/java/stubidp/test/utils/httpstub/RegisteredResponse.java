package stubidp.test.utils.httpstub;

import java.util.Map;

public record RegisteredResponse(int status,
                                 String contentType,
                                 String body,
                                 Map<String, String> headers) {
}
