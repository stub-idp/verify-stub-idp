package stubidp.utils.rest.jerseyclient;

import io.dropwizard.setup.Environment;
import stubidp.utils.rest.restclient.ClientProvider;
import stubidp.utils.rest.restclient.RestfulClientConfiguration;

import javax.inject.Inject;

public class DefaultClientProvider extends ClientProvider {

    @Inject
    public DefaultClientProvider(
            Environment environment,
            RestfulClientConfiguration restfulClientConfiguration) {

        super(
                environment,
                restfulClientConfiguration.getJerseyClientConfiguration(),
                restfulClientConfiguration.getEnableRetryTimeOutConnections(),
                "MicroserviceClient"
        );
    }
}
