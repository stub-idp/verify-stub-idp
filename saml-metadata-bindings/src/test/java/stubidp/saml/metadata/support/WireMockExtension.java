package stubidp.saml.metadata.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class WireMockExtension extends WireMockServer implements BeforeAllCallback, AfterAllCallback {

    public WireMockExtension(Options options) {
        super(options);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        stop();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        WireMock.configureFor("localhost", port());
        start();
    }
}
