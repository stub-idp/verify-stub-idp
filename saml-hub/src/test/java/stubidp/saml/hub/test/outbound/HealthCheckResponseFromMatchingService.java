package stubidp.saml.hub.test.outbound;

import stubidp.saml.domain.matching.IdaMatchingServiceResponse;

import java.time.Instant;

public class HealthCheckResponseFromMatchingService extends IdaMatchingServiceResponse {
    public HealthCheckResponseFromMatchingService(String entityId, String healthCheckReqeustId) {
        super("healthcheck-response-id", healthCheckReqeustId, entityId, Instant.now());
    }

    public HealthCheckResponseFromMatchingService(final String responseId,
                                                  final String entityId,
                                                  final String healthCheckReqeustId) {
        super(responseId, healthCheckReqeustId, entityId, Instant.now());
    }
}

