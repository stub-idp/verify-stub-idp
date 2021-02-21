package acceptance.uk.gov.ida.verifyserviceprovider;

import acceptance.uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppExtension;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class VersionNumberAcceptanceTest {

    private static final MockMsaServer msaServer = new MockMsaServer();

    private static final DropwizardAppExtension<VerifyServiceProviderConfiguration> application = new VerifyServiceProviderAppExtension(msaServer);

    private static Client client;

    @BeforeEach
    public void beforeEach() {
        client = application.client();
    }

    @Test
    public void shouldRespondWithVersionNumber() {
        Response response = client
                .target(String.format("http://localhost:%d/version-number", application.getLocalPort()))
                .request()
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        //tests run /out directory instead of jar file hence can't read actual version from META-INF
        assertThat(response.readEntity(String.class)).isEqualTo("UNKNOWN_VERSION");
    }
}
