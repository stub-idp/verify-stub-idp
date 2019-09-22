package stubidp.stubidp;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import stubidp.stubidp.configuration.SingleIdpConfiguration;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.services.ServiceListService;
import stubidp.utils.rest.jerseyclient.ErrorHandlingClient;
import stubidp.utils.rest.jerseyclient.JsonClient;
import stubidp.utils.rest.jerseyclient.JsonResponseProcessor;
import stubidp.utils.rest.restclient.ClientProvider;

import javax.ws.rs.client.Client;

public class StubIdpSingleIdpBinder extends AbstractBinder {

    private final StubIdpConfiguration stubIdpConfiguration;
    private final Environment environment;

    public StubIdpSingleIdpBinder(StubIdpConfiguration stubIdpConfiguration,
                                  Environment environment) {
        this.stubIdpConfiguration = stubIdpConfiguration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        if(stubIdpConfiguration.getSingleIdpJourneyConfiguration().isEnabled()) {
            bind(stubIdpConfiguration.getSingleIdpJourneyConfiguration()).to(SingleIdpConfiguration.class);
            final Client client = new ClientProvider(environment,
                    stubIdpConfiguration.getSingleIdpJourneyConfiguration().getServiceListClient(),
                    true, "StubIdpJsonClient").get();
            final JsonClient jsonClient = new JsonClient(new ErrorHandlingClient(client), new JsonResponseProcessor(Jackson.newObjectMapper()));
            bind(new ServiceListService(stubIdpConfiguration.getSingleIdpJourneyConfiguration(), jsonClient)).to(ServiceListService.class);
        }
    }
}
