package stubidp.kotlin.test.integration.support

import certificates.values.CACertificates
import com.fasterxml.jackson.core.JsonProcessingException
import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.ConfigOverride
import io.dropwizard.testing.junit5.DropwizardAppExtension
import io.prometheus.client.Collector
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import org.opensaml.core.config.InitializationService
import org.opensaml.core.xml.io.MarshallingException
import org.opensaml.saml.saml2.metadata.EntityDescriptor
import org.opensaml.xmlsec.signature.support.SignatureException
import stubidp.saml.constants.Constants
import stubidp.saml.security.IdaKeyStore
import stubidp.saml.test.builders.AssertionConsumerServiceBuilder
import stubidp.saml.test.builders.EntityDescriptorBuilder
import stubidp.saml.test.builders.IdpSsoDescriptorBuilder
import stubidp.saml.test.builders.KeyDescriptorBuilder
import stubidp.saml.test.builders.SPSSODescriptorBuilder
import stubidp.saml.test.metadata.EntitiesDescriptorFactory
import stubidp.saml.test.metadata.MetadataFactory
import stubidp.stubidp.StubIdpApplication
import stubidp.stubidp.configuration.IdpStubsConfiguration
import stubidp.stubidp.configuration.StubIdp
import stubidp.stubidp.configuration.StubIdpConfiguration
import stubidp.stubidp.exceptions.mappers.InvalidAuthnRequestExceptionMapper
import stubidp.stubidp.exceptions.mappers.InvalidEidasAuthnRequestExceptionMapper
import stubidp.stubidp.resources.eidas.EidasAuthnRequestReceiverResource
import stubidp.stubidp.resources.idp.HeadlessIdpResource
import stubidp.stubidp.resources.idp.IdpAuthnRequestReceiverResource
import stubidp.stubidp.saml.BaseAuthnRequestValidator
import stubidp.stubidp.services.AuthnRequestReceiverService
import stubidp.stubidp.services.EidasAuthnResponseService
import stubidp.stubidp.services.NonSuccessAuthnResponseService
import stubidp.stubidp.services.SuccessAuthnResponseService
import stubidp.test.devpki.TestCertificateStrings
import stubidp.test.devpki.TestEntityIds
import stubidp.test.utils.httpstub.HttpStubRule
import stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder
import stubidp.utils.security.security.PrivateKeyFactory
import stubidp.utils.security.security.PublicKeyFactory
import stubidp.utils.security.security.X509CertificateFactory
import java.io.File
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.KeyPair
import java.util.ArrayList
import java.util.Base64
import java.util.HashMap
import java.util.UUID
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.ws.rs.core.UriBuilder

class StubIdpAppExtension @JvmOverloads constructor(configOverrides: Map<String?, String?> = java.util.Map.of()) : DropwizardAppExtension<StubIdpConfiguration?>(StubIdpApplication::class.java, "../configuration/stub-idp.yml", *withDefaultOverrides(configOverrides)) {
    private val stubIdps: MutableList<StubIdp> = ArrayList()

    @Throws(Exception::class)
    override fun before() {
        resetStaticMetrics() // a hack to reset any prometheus metrics that are static as they will be re-used between test classes
        metadataTrustStore.create()
        spTrustStore.create()
        val idpStubsConfiguration: IdpStubsConfiguration = TestIdpStubsConfiguration(stubIdps)
        try {
            Files.writeString(STUB_IDPS_FILE.toPath(), Jackson.newObjectMapper().writeValueAsString(idpStubsConfiguration), StandardCharsets.UTF_8)
            STUB_IDPS_FILE.deleteOnExit()
            InitializationService.initialize()
            verifyMetadataServer.reset()
            eidasMetadataServer.reset()
            verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, verifyMetadata)
            eidasMetadataServer.register(EIDAS_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, eidasMetadata)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        super.before()
    }

    @get:Throws(MarshallingException::class, SignatureException::class)
    private val verifyMetadata: String
        get() {
            val entityDescriptors: MutableList<EntityDescriptor> = ArrayList()
            entityDescriptors.add(EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(SP_ENTITY_ID)
                    .withSpSsoDescriptor(SPSSODescriptorBuilder.anSpServiceDescriptor()
                            .withoutDefaultEncryptionKey()
                            .withoutDefaultSigningKey()
                            .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForEncryption(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT).build())
                            .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT).build())
                            .addAssertionConsumerService(AssertionConsumerServiceBuilder.anAssertionConsumerService().withLocation(assertionConsumerServices.toASCIIString()).build()).build())
                    .build())
            for (stubIdp in stubIdps) {
                entityDescriptors.add(EntityDescriptorBuilder.anEntityDescriptor()
                        .withEntityId(String.format("http://stub_idp.acme.org/%s/SSO/POST", stubIdp.friendlyId))
                        .withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
                                .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT).build())
                                .withoutDefaultSigningKey()
                                .build()).build())
            }
            val entitiesDescriptor = EntitiesDescriptorFactory()
                    .signedEntitiesDescriptor(entityDescriptors, TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT, TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY)
            return MetadataFactory().metadata(entitiesDescriptor)
        }

    @get:Throws(MarshallingException::class, SignatureException::class)
    private val eidasMetadata: String
        get() {
            val entityDescriptorList = listOf(EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(TestEntityIds.HUB_CONNECTOR_ENTITY_ID)
                    .withSpSsoDescriptor(SPSSODescriptorBuilder.anSpServiceDescriptor()
                            .withoutDefaultEncryptionKey()
                            .withoutDefaultSigningKey()
                            .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForEncryption(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_ENCRYPTION_CERT).build())
                            .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT).build())
                            .addAssertionConsumerService(AssertionConsumerServiceBuilder.anAssertionConsumerService().withLocation(assertionConsumerServices.toASCIIString()).build())
                            .build())
                    .build())
            val entitiesDescriptor = EntitiesDescriptorFactory()
                    .signedEntitiesDescriptor(entityDescriptorList, TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT, TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY)
            return MetadataFactory().metadata(entitiesDescriptor)
        }

    override fun after() {
        metadataTrustStore.delete()
        spTrustStore.delete()
        STUB_IDPS_FILE.delete()
        super.after()
    }

    private fun resetStaticMetrics() {
        val countersToReset = listOf(AuthnRequestReceiverService.successfulEidasAuthnRequests,
                AuthnRequestReceiverService.successfulVerifyAuthnRequests,
                SuccessAuthnResponseService.sentVerifyAuthnResponses,
                EidasAuthnResponseService.sentEidasAuthnFailureResponses,
                EidasAuthnResponseService.sentEidasAuthnSuccessResponses,
                NonSuccessAuthnResponseService.sentVerifyAuthnFailureResponses,
                EidasAuthnRequestReceiverResource.receivedEidasAuthnRequests,
                IdpAuthnRequestReceiverResource.receivedVerifyAuthnRequests,
                InvalidAuthnRequestExceptionMapper.invalidVerifyAuthnRequests,
                InvalidEidasAuthnRequestExceptionMapper.invalidEidasAuthnRequests,
                HeadlessIdpResource.receivedHeadlessAuthnRequests,
                HeadlessIdpResource.successfulHeadlessAuthnRequests)
        // this wipes _all_ metrics from the app, which unfortunately means that
        // static metrics aren't re-initialised.
        CollectorRegistry.defaultRegistry.clear()
        try {
            CollectorRegistry.defaultRegistry.unregister(BaseAuthnRequestValidator.replayCacheCollector)
        } catch (e: NullPointerException) {
            // unregistering may fail in this way but if it does we can continue
        }
        BaseAuthnRequestValidator.replayCacheCollector.register<Collector>()
        countersToReset.forEach(Consumer { c: Counter ->
            c.clear()
            CollectorRegistry.defaultRegistry.register(c)
        })
    }

    fun withStubIdp(stubIdp: StubIdp): StubIdpAppExtension {
        stubIdps.add(stubIdp)
        return this
    }

    val verifyMetadataPath: URI
        get() = URI.create("http://localhost:" + verifyMetadataServer.port + VERIFY_METADATA_PATH)

    val assertionConsumerServices: URI
        get() = UriBuilder.fromUri("https://somedomain/destination").build()

    private class TestIdpStubsConfiguration(idps: List<StubIdp>?) : IdpStubsConfiguration() {
        init {
            stubIdps = idps
        }
    }

    val hubKeyStore: IdaKeyStore
        get() {
            val privateKey = PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY))
            val publicKey = PublicKeyFactory(X509CertificateFactory()).createPublicKey(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT)
            val encryptionKeys = listOf(KeyPair(publicKey, privateKey))
            return IdaKeyStore(null, encryptionKeys)
        }

    val eidasKeyStore: IdaKeyStore
        get() {
            val privateKey = PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_ENCRYPTION_KEY))
            val publicKey = PublicKeyFactory(X509CertificateFactory()).createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT)
            val encryptionKeys = listOf(KeyPair(publicKey, privateKey))
            return IdaKeyStore(null, encryptionKeys)
        }

    companion object {
        private const val VERIFY_METADATA_PATH = "/saml/metadata/sp"
        private const val EIDAS_METADATA_PATH = "/saml/metadata/eidas/connector"
        const val SP_ENTITY_ID = "http://localhost/stubsp/SAML2/metadata/federation"
        private val verifyMetadataServer = HttpStubRule()
        private val eidasMetadataServer = HttpStubRule()
        private val metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build()
        private val spTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("coreCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build()
        private val STUB_IDPS_FILE = File(System.getProperty("java.io.tmpdir"), "stub-idps.yml")
        private val fakeFrontend = HttpStubRule()
        private fun withDefaultOverrides(configOverrides: Map<String?, String?>): Array<ConfigOverride> {
            var config = java.util.Map.ofEntries(
                    java.util.Map.entry("basicAuthEnabledForUserResource", "true"),
                    java.util.Map.entry("isPrometheusEnabled", "false"),
                    java.util.Map.entry("isHeadlessIdpEnabled", "false"),
                    java.util.Map.entry("isIdpEnabled", "true"),
                    java.util.Map.entry("dynamicReloadOfStubIdpYmlEnabled", "false"),
                    java.util.Map.entry("server.requestLog.appenders[0].type", "console"),
                    java.util.Map.entry("server.applicationConnectors[0].port", "0"),
                    java.util.Map.entry("server.adminConnectors[0].port", "0"),
                    java.util.Map.entry("logging.level", "WARN"),
                    java.util.Map.entry("logging.appenders[0].type", "console"),
                    java.util.Map.entry("stubIdpsYmlFileLocation", STUB_IDPS_FILE.absolutePath),
                    java.util.Map.entry("signingKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                    java.util.Map.entry("signingKeyPairConfiguration.privateKeyConfiguration.key", TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY),
                    java.util.Map.entry("signingKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                    java.util.Map.entry("signingKeyPairConfiguration.publicKeyConfiguration.cert", TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT),
                    java.util.Map.entry("idpMetadataSigningKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                    java.util.Map.entry("idpMetadataSigningKeyPairConfiguration.privateKeyConfiguration.key", TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY),
                    java.util.Map.entry("idpMetadataSigningKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                    java.util.Map.entry("idpMetadataSigningKeyPairConfiguration.publicKeyConfiguration.cert", TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT),
                    java.util.Map.entry("metadata.uri", "http://localhost:" + verifyMetadataServer.port + VERIFY_METADATA_PATH),
                    java.util.Map.entry("metadata.expectedEntityId", SP_ENTITY_ID),
                    java.util.Map.entry("metadata.trustStore.store", metadataTrustStore.absolutePath),
                    java.util.Map.entry("metadata.trustStore.password", metadataTrustStore.password),
                    java.util.Map.entry("metadata.spTrustStore.store", spTrustStore.absolutePath),
                    java.util.Map.entry("metadata.spTrustStore.password", spTrustStore.password),  // FIXME: add port...
                    java.util.Map.entry("saml.expectedDestination", "http://localhost:0"),
                    java.util.Map.entry("europeanIdentity.enabled", "false"),
                    java.util.Map.entry("europeanIdentity.hubConnectorEntityId", TestEntityIds.HUB_CONNECTOR_ENTITY_ID),
                    java.util.Map.entry("europeanIdentity.stubCountryBaseUrl", "http://localhost:0"),
                    java.util.Map.entry("europeanIdentity.metadata.uri", "http://localhost:" + eidasMetadataServer.port + EIDAS_METADATA_PATH),
                    java.util.Map.entry("europeanIdentity.metadata.expectedEntityId", TestEntityIds.HUB_CONNECTOR_ENTITY_ID),
                    java.util.Map.entry("europeanIdentity.metadata.trustStore.store", metadataTrustStore.absolutePath),
                    java.util.Map.entry("europeanIdentity.metadata.trustStore.password", metadataTrustStore.password),
                    java.util.Map.entry("europeanIdentity.metadata.spTrustStore.store", spTrustStore.absolutePath),
                    java.util.Map.entry("europeanIdentity.metadata.spTrustStore.password", spTrustStore.password),
                    java.util.Map.entry("europeanIdentity.signingKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                    java.util.Map.entry("europeanIdentity.signingKeyPairConfiguration.privateKeyConfiguration.key", TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY),
                    java.util.Map.entry("europeanIdentity.signingKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                    java.util.Map.entry("europeanIdentity.signingKeyPairConfiguration.publicKeyConfiguration.cert", TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT),
                    java.util.Map.entry("database.url", "jdbc:h2:mem:" + UUID.randomUUID().toString() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1"),
                    java.util.Map.entry("singleIdpJourney.enabled", "false"),
                    java.util.Map.entry("singleIdpJourney.serviceListUri", "http://localhost:" + fakeFrontend.port + "/get-available-services"),
                    java.util.Map.entry("secureCookieConfiguration.secure", "true"),
                    java.util.Map.entry("secureCookieConfiguration.keyConfiguration.base64EncodedKey", Base64.getEncoder().encodeToString(ByteArray(64)))
            )
            config = HashMap(config)
            config.putAll(configOverrides)
            val overrides = config.entries.stream()
                    .map { o: Map.Entry<String?, String?> -> ConfigOverride.config(o.key, o.value) }
                    .collect(Collectors.toUnmodifiableList())
            return overrides.toTypedArray()
        }
    }

    init {
        try {
            fakeFrontend.register("/get-available-services", 200, "application/json", "[]")
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
    }
}