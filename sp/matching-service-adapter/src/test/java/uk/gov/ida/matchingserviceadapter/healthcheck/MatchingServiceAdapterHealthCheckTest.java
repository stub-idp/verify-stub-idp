package uk.gov.ida.matchingserviceadapter.healthcheck;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchingServiceAdapterHealthCheckTest {

    private final MatchingServiceAdapterHealthCheck healthCheck = new MatchingServiceAdapterHealthCheck();

    @Test
    public void shouldReturnHealthy() {
        assertThat(healthCheck.getName()).isEqualTo("Matching Service Adapter Health Check");
        assertThat(healthCheck.check().isHealthy()).isTrue();
    }

}