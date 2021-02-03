package uk.gov.ida.matchingserviceadapter.domain;

import stubidp.saml.domain.matching.IdaMatchingServiceResponse;

import java.time.Instant;
import java.util.UUID;

import static java.text.MessageFormat.format;

public class HealthCheckResponseFromMatchingService extends IdaMatchingServiceResponse {
    
    public HealthCheckResponseFromMatchingService(String entityId, String healthCheckRequestId, String msaVersion, boolean eidasEnabled, boolean shouldSignWithSHA1) {
        super(format("healthcheck-response-id-{0}-version-{1}-eidasenabled-{2}-shouldsignwithsha1-{3}", UUID.randomUUID(), msaVersion, eidasEnabled, shouldSignWithSHA1), healthCheckRequestId, entityId, Instant.now());
    }
}

