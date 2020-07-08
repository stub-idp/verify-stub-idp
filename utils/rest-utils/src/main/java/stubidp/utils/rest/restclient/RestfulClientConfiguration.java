package stubidp.utils.rest.restclient;

import io.dropwizard.client.JerseyClientConfiguration;

public interface RestfulClientConfiguration {

    boolean getEnableRetryTimeOutConnections();

    JerseyClientConfiguration getJerseyClientConfiguration();
}
