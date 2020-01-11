package stubsp.stubsp.integration.support;

import certificates.values.CACertificates;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.metadata.test.factories.metadata.EntitiesDescriptorFactory;
import stubidp.saml.metadata.test.factories.metadata.MetadataFactory;
import stubidp.saml.utils.Constants;
import stubidp.saml.utils.core.test.builders.metadata.EntityDescriptorBuilder;
import stubidp.saml.utils.core.test.builders.metadata.IdpSsoDescriptorBuilder;
import stubidp.saml.utils.core.test.builders.metadata.KeyDescriptorBuilder;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.keystore.KeyStoreResource;
import stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder;
import stubsp.stubsp.StubSpApplication;
import stubsp.stubsp.configuration.StubSpConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;

public class StubSpAppExtension extends DropwizardAppExtension<StubSpConfiguration> {

    private static final Logger LOG = Logger.getLogger(StubSpAppExtension.class);
    private static final HttpStubRule metadataServer = new HttpStubRule();
    private static final String IDP_METADATA_PATH = "/saml/metadata/idp";

    private static final String IDP_ENTITY_ID = String.format("http://stub_idp.acme.org/%s/SSO/POST", "stub-idp-one");
    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource idpTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    private static ConfigOverride[] withDefaultOverrides(Map<String, String> configOverrides) {
        Map<String, String> config = Map.ofEntries(
                Map.entry("server.applicationConnectors[0].port", "0"),
                Map.entry("server.adminConnectors[0].port", "0"),
                Map.entry("logging.appenders[0].type", "console"),
                Map.entry("server.requestLog.appenders[0].type", "console"),
                Map.entry("metadata.uri", "http://localhost:" + metadataServer.getPort() + IDP_METADATA_PATH),
                Map.entry("metadata.expectedEntityId", IDP_ENTITY_ID),
                Map.entry("metadata.trustStore.store", metadataTrustStore.getAbsolutePath()),
                Map.entry("metadata.trustStore.password", metadataTrustStore.getPassword()),
                Map.entry("metadata.idpTrustStore.store", idpTrustStore.getAbsolutePath()),
                Map.entry("metadata.idpTrustStore.password", idpTrustStore.getPassword())
        );
        config = new HashMap<>(config);
        config.putAll(configOverrides);
        final List<ConfigOverride> overrides = config.entrySet().stream()
                .map(o -> ConfigOverride.config(o.getKey(), o.getValue()))
                .collect(Collectors.toUnmodifiableList());
        return overrides.toArray(new ConfigOverride[config.size()]);
    }

    public StubSpAppExtension() {
        this(Map.of());
    }

    public StubSpAppExtension(Map<String, String> configOverrides) {
        super(StubSpApplication.class, "../configuration/stub-sp.yml", withDefaultOverrides(configOverrides));
    }

    @BeforeAll
    public void before() throws Exception {
        metadataTrustStore.create();
        idpTrustStore.create();
        metadataServer.reset();
        metadataServer.register(IDP_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, getIdpMetadata());

        super.before();
    }

    @Override
    public void after() {
        metadataTrustStore.delete();
        idpTrustStore.delete();

        super.after();
    }

    private static String getIdpMetadata() throws MarshallingException, SignatureException {
        List<EntityDescriptor> entityDescriptors = new ArrayList<>();
        entityDescriptors.add(EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(IDP_ENTITY_ID)
                .withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(STUB_IDP_PUBLIC_PRIMARY_CERT).build())
                        .withoutDefaultSigningKey()
                        .build())
                .build());
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorFactory()
                .signedEntitiesDescriptor(entityDescriptors, METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY);
        final String metadata = new MetadataFactory().metadata(entitiesDescriptor);
        return metadata;
    }
}
