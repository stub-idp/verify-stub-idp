package uk.gov.ida.rp.testrp.metadata;

import com.google.common.base.Throwables;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.exceptions.InsecureMetadataException;
import uk.gov.ida.saml.metadata.JerseyClientMetadataResolver;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.client.Client;
import java.util.Timer;

public class MetadataResolverProvider implements Provider<MetadataResolver> {

    private final Client client;
    private final TestRpConfiguration configuration;

    @Inject
    public MetadataResolverProvider(Client client, TestRpConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    public MetadataResolver get() {
        if (insecureConnectionNotAllowed()) {
            throw new InsecureMetadataException("Tried to make http request to metadata but allowInsecureMetadataLocation was false");
        }
        try {
            Timer timer = new Timer();
            InitializationService.initialize();
            JerseyClientMetadataResolver metadataResolver = new JerseyClientMetadataResolver(timer, client, configuration.getMsaMetadataUri());
            BasicParserPool pool = new BasicParserPool();
            pool.initialize();
            metadataResolver.setParserPool(pool);
            metadataResolver.setRequireValidMetadata(true);
            metadataResolver.setFailFastInitialization(false);
            metadataResolver.setMaxRefreshDelay(600000);
            metadataResolver.setMinRefreshDelay(2000);
            metadataResolver.setId("metadataProvider");
            metadataResolver.initialize();
            return metadataResolver;
        } catch (ComponentInitializationException | InitializationException e) {
            Throwables.propagate(e);
        }
        return null;
    }

    private boolean insecureConnectionNotAllowed() {
        return !configuration.getAllowInsecureMetadataLocation() && "http".equals(configuration.getMsaMetadataUri().getScheme());
    }
}
