package uk.gov.ida.integrationtest.helpers;

import certificates.values.CACertificates;
import com.nimbusds.jose.JOSEException;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.eidas.trustanchor.Generator;
import stubidp.saml.constants.Constants;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.EntityDescriptorBuilder;
import stubidp.saml.test.builders.IdpSsoDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.builders.SignatureBuilder;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.keystore.KeyStoreResource;
import stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;

import javax.ws.rs.core.MediaType;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static stubidp.saml.metadata.ResourceEncoder.entityIdAsResource;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestCertificateStrings.getPrimaryPublicEncryptionCert;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.HUB_SECONDARY_ENTITY_ID;

public class MatchingServiceAdapterAppExtension extends DropwizardAppExtension<MatchingServiceAdapterConfiguration> {

    public static final String VERIFY_METADATA_PATH = "/verify-metadata";
    private static final String TRUST_ANCHOR_PATH = "/trust-anchor";
    private static final String METADATA_AGGREGATOR_PATH = "/metadata-aggregator";
    private static final String COUNTRY_METADATA_PATH = "/test-country";
    private static final String METADATA_SOURCE_PATH = "/metadata-source";

    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();
    private static final HttpStubRule metadataAggregatorServer = new HttpStubRule();
    private static final HttpStubRule trustAnchorServer = new HttpStubRule();

    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource hubTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("hubCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource idpTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource countryMetadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_CERT = "\n-----END CERTIFICATE-----";

    private String countryEntityId;

    public MatchingServiceAdapterAppExtension(Map<String, String> otherConfigOverrides) {
        this(false, otherConfigOverrides);
    }

    public MatchingServiceAdapterAppExtension(boolean isCountryEnabled) {
        this(isCountryEnabled, Map.of());
    }

    public MatchingServiceAdapterAppExtension(boolean isCountryEnabled, Map<String, String> otherConfigOverrides) {
        super(MatchingServiceAdapterApplication.class,
                "configuration/verify-matching-service-adapter.yml",
                MatchingServiceAdapterAppExtension.withDefaultOverrides(
                        isCountryEnabled,
                        true,
                        otherConfigOverrides)
        );
    }

    public MatchingServiceAdapterAppExtension(
            boolean isCountryEnabled,
            String configFile,
            boolean overrideTruststores,
            Map<String, String> otherConfigOverrides) {
        super(MatchingServiceAdapterApplication.class,
                "configuration/"+configFile,
                MatchingServiceAdapterAppExtension.withDefaultOverrides(
                        isCountryEnabled,
                        overrideTruststores,
                        otherConfigOverrides
                )
        );
    }

    @Override
    public void before() throws Exception {
        metadataTrustStore.create();
        hubTrustStore.create();
        idpTrustStore.create();
        countryMetadataTrustStore.create();

        countryEntityId = "https://localhost:12345" + METADATA_AGGREGATOR_PATH + COUNTRY_METADATA_PATH;

        try {
            InitializationService.initialize();
            String testCountryMetadata = new MetadataFactory().singleEntityMetadata(buildTestCountryEntityDescriptor());

            verifyMetadataServer.reset();
            verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());

            trustAnchorServer.reset();
            trustAnchorServer.register(TRUST_ANCHOR_PATH, 200, MediaType.APPLICATION_OCTET_STREAM, buildTrustAnchorString());

            metadataAggregatorServer.reset();
            metadataAggregatorServer.register(METADATA_SOURCE_PATH + "/" + entityIdAsResource(countryEntityId), 200, Constants.APPLICATION_SAMLMETADATA_XML, testCountryMetadata);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.before();
    }

    @Override
    public void after() {
        metadataTrustStore.delete();
        hubTrustStore.delete();
        idpTrustStore.delete();
        countryMetadataTrustStore.delete();

        super.after();
    }

    public static ConfigOverride[] withDefaultOverrides(
            boolean isCountryPresent,
            boolean overrideTruststores,
            Map<String, String> otherConfigOverrides) {
        Map<String, String> overrides = new HashMap<>(Map.ofEntries(
                Map.entry("returnStackTraceInErrorResponse", "true"),
                Map.entry("clockSkewInSeconds", "60"),
                Map.entry("server.applicationConnectors[0].port", "0"),
                Map.entry("server.adminConnectors[0].port", "0"),
                Map.entry("encryptionKeys[0].privateKey.key", TEST_RP_MS_PRIVATE_ENCRYPTION_KEY),
                Map.entry("encryptionKeys[0].privateKey.type", "encoded"),
                Map.entry("encryptionKeys[0].publicKey.type", "encoded"),
                Map.entry("encryptionKeys[0].publicKey.cert", getCertificate(getPrimaryPublicEncryptionCert(HUB_ENTITY_ID))),
                Map.entry("signingKeys.primary.privateKey.key", TEST_RP_MS_PRIVATE_SIGNING_KEY),
                Map.entry("signingKeys.primary.privateKey.type", "encoded"),
                Map.entry("signingKeys.primary.publicKey.type", "encoded"),
                Map.entry("signingKeys.primary.publicKey.cert", getCertificate(TEST_RP_MS_PUBLIC_SIGNING_CERT)),
                Map.entry("signingKeys.secondary.privateKey.key", TEST_RP_PRIVATE_SIGNING_KEY),
                Map.entry("signingKeys.secondary.privateKey.type", "encoded"),
                Map.entry("signingKeys.secondary.publicKey.type", "encoded"),
                Map.entry("signingKeys.secondary.publicKey.cert", getCertificate(TEST_RP_PUBLIC_SIGNING_CERT)),
                Map.entry("metadata.hubEntityId", HUB_ENTITY_ID),
                Map.entry("metadata.uri", "http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH),
                Map.entry("hub.hubEntityId", HUB_ENTITY_ID),
                Map.entry("shouldSignWithSHA1", String.valueOf(false))
        ));

        if (overrideTruststores) {
            Map<String, String> trustStoreOverrides = Map.ofEntries(
                    Map.entry("metadata.trustStore.type", "file"),
                    Map.entry("metadata.trustStore.path", metadataTrustStore.getAbsolutePath()),
                    Map.entry("metadata.trustStore.password", metadataTrustStore.getPassword()),
                    Map.entry("metadata.hubTrustStore.type", "file"),
                    Map.entry("metadata.hubTrustStore.path", hubTrustStore.getAbsolutePath()),
                    Map.entry("metadata.hubTrustStore.password", hubTrustStore.getPassword()),
                    Map.entry("metadata.idpTrustStore.type", "file"),
                    Map.entry("metadata.idpTrustStore.path", idpTrustStore.getAbsolutePath()),
                    Map.entry("metadata.idpTrustStore.password", idpTrustStore.getPassword())
            );
            overrides.putAll(trustStoreOverrides);
        }

        if (isCountryPresent) {
            Map<String, String> countryOverrides = Map.ofEntries(
                    Map.entry("europeanIdentity.hubConnectorEntityId", HUB_SECONDARY_ENTITY_ID),
                    Map.entry("europeanIdentity.enabled", "true"),

                    Map.entry("europeanIdentity.aggregatedMetadata.minRefreshDelay", "PT60s"),
                    Map.entry("europeanIdentity.aggregatedMetadata.maxRefreshDelay", "PT600s"),
                    Map.entry("europeanIdentity.aggregatedMetadata.jerseyClientName", "trust-anchor-client"),

                    Map.entry("europeanIdentity.aggregatedMetadata.client.timeout", "2s"),
                    Map.entry("europeanIdentity.aggregatedMetadata.client.timeToLive", "10m"),
                    Map.entry("europeanIdentity.aggregatedMetadata.client.cookiesEnabled", "false"),
                    Map.entry("europeanIdentity.aggregatedMetadata.client.connectionTimeout", "1s"),
                    Map.entry("europeanIdentity.aggregatedMetadata.client.retries", "3"),
                    Map.entry("europeanIdentity.aggregatedMetadata.client.keepAlive", "60s"),
                    Map.entry("europeanIdentity.aggregatedMetadata.client.chunkedEncodingEnabled", "false"),
                    Map.entry("europeanIdentity.aggregatedMetadata.client.validateAfterInactivityPeriod", "5s"),

                    Map.entry("europeanIdentity.aggregatedMetadata.client.tls.protocol", "TLSv1.2"),
                    Map.entry("europeanIdentity.aggregatedMetadata.client.tls.verifyHostname", "false"),
                    Map.entry("europeanIdentity.aggregatedMetadata.client.tls.trustSelfSignedCertificates", "true")
            );
            overrides.putAll(countryOverrides);

            if (overrideTruststores) {
                Map<String, String> countryTrustStoreOverrides = Map.ofEntries(
                        Map.entry("europeanIdentity.aggregatedMetadata.trustAnchorUri", "http://localhost:" + trustAnchorServer.getPort() + TRUST_ANCHOR_PATH),
                        Map.entry("europeanIdentity.aggregatedMetadata.metadataSourceUri", "http://localhost:" + metadataAggregatorServer.getPort() + METADATA_SOURCE_PATH),
                        Map.entry("europeanIdentity.aggregatedMetadata.trustStore.store", countryMetadataTrustStore.getAbsolutePath()),
                        Map.entry("europeanIdentity.aggregatedMetadata.trustStore.trustStorePassword", countryMetadataTrustStore.getPassword())
                );
                overrides.putAll(countryTrustStoreOverrides);
            }
        }

        overrides.putAll(otherConfigOverrides);
        final List<ConfigOverride> config = overrides.entrySet().stream()
                .map(o -> ConfigOverride.config(o.getKey(), o.getValue()))
                .collect(Collectors.toUnmodifiableList());
        return config.toArray(new ConfigOverride[overrides.size()]);
    }

    public String getCountryEntityId() {
        return countryEntityId;
    }

    private static String getCertificate(String strippedCertificate) {
        String certificate = BEGIN_CERT + strippedCertificate + END_CERT;
        return Base64.encodeBase64String(certificate.getBytes());
    }

    private String buildTrustAnchorString() throws JOSEException, CertificateEncodingException {
        X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
        PrivateKey trustAnchorKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(METADATA_SIGNING_A_PRIVATE_KEY));
        X509Certificate trustAnchorCert = x509CertificateFactory.createCertificate(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);
        Generator generator = new Generator(trustAnchorKey, trustAnchorCert);
        HashMap<String, List<X509Certificate>> trustAnchorMap = new HashMap<>();
        X509Certificate metadataCACert = x509CertificateFactory.createCertificate(CACertificates.TEST_METADATA_CA.replace(BEGIN_CERT, "").replace(END_CERT, "").replace("\n", ""));
        trustAnchorMap.put(countryEntityId, singletonList(metadataCACert));
        return generator.generateFromMap(trustAnchorMap).serialize();
    }

    private EntityDescriptor buildTestCountryEntityDescriptor() throws Exception {
        KeyDescriptor signingKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                .withX509ForSigning(STUB_COUNTRY_PUBLIC_PRIMARY_CERT)
                .build();

        IDPSSODescriptor idpSsoDescriptor = IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
                .withoutDefaultSigningKey()
                .addKeyDescriptor(signingKeyDescriptor)
                .build();

        Signature signature = SignatureBuilder.aSignature()
                .withSigningCredential(new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
                .withX509Data(METADATA_SIGNING_A_PUBLIC_CERT)
                .build();

        return EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(countryEntityId)
                .withIdpSsoDescriptor(idpSsoDescriptor)
                .setAddDefaultSpServiceDescriptor(false)
                .withValidUntil(Instant.now().plus(14, ChronoUnit.DAYS))
                .withSignature(signature)
                .build();
    }
}
