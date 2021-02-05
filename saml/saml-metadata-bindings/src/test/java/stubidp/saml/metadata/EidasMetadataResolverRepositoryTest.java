package stubidp.saml.metadata;

import certificates.values.CACertificates;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.LoggerFactory;
import stubidp.eidas.trustanchor.CountryTrustAnchor;
import stubidp.saml.metadata.factories.DropwizardMetadataResolverFactory;
import stubidp.saml.metadata.factories.MetadataSignatureTrustEngineFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.X509CertificateFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;
import java.security.KeyStoreException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EidasMetadataResolverRepositoryTest {

    @Mock(lenient = true) // for shouldRemoveOldMetadataResolverWhenRefreshing
    private EidasTrustAnchorResolver trustAnchorResolver;

    @Mock
    private EidasMetadataConfiguration metadataConfiguration;

    @Mock
    private DropwizardMetadataResolverFactory dropwizardMetadataResolverFactory;

    @Mock
    private Timer timer;

    @Mock
    private JerseyClientMetadataResolver metadataResolver;

    @Mock
    private JerseyClientMetadataResolver metadataResolver2;

    @Mock
    private JerseyClientMetadataResolver metadataResolver3;

    @Mock
    private JerseyClientMetadataResolver metadataResolver4;

    @Mock
    private MetadataSignatureTrustEngineFactory metadataSignatureTrustEngineFactory;

    @Mock
    private ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine;

    @Mock
    private Client metadataClient;

    @Mock
    private MetadataResolverConfigBuilder metadataResolverConfigBuilder;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<MetadataResolverConfiguration> metadataResolverConfigurationCaptor;

    private final X509CertificateFactory certificateFactory = new X509CertificateFactory();

    private List<JWK> trustAnchors;

    @BeforeEach
    void setUp() {
        trustAnchors = new ArrayList<>();
    }

    @Test
    void shouldCreateMetadataResolverWhenTrustAnchorIsValid() throws KeyStoreException, CertificateException, ComponentInitializationException, SignatureException, ParseException, JOSEException {
        when(trustAnchorResolver.getTrustAnchors()).thenReturn(trustAnchors);
        when(dropwizardMetadataResolverFactory.createMetadataResolverWithClient(any(), eq(true), eq(metadataClient))).thenReturn(metadataResolver);
        when(metadataSignatureTrustEngineFactory.createSignatureTrustEngine(metadataResolver)).thenReturn(explicitKeySignatureTrustEngine);

        List<String> stringCertChain = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_IDP_CA,
                TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT
        );

        String entityId = "http://signin.gov.uk/entity/id";
        JWK trustAnchor = createJWK(entityId, stringCertChain, true);
        trustAnchors.add(trustAnchor);

        when(metadataConfiguration.getMetadataSourceUri()).thenReturn(UriBuilder.fromUri("https://source.local").build());
        when(metadataConfiguration.getTrustAnchorMaxRefreshDelay()).thenReturn(Duration.ofSeconds(60));
        EidasMetadataResolverRepository metadataResolverRepository = new EidasMetadataResolverRepository(
                trustAnchorResolver,
                metadataConfiguration,
                dropwizardMetadataResolverFactory,
                timer,
                metadataSignatureTrustEngineFactory,
                new MetadataResolverConfigBuilder(),
                metadataClient);

        verify(dropwizardMetadataResolverFactory).createMetadataResolverWithClient(metadataResolverConfigurationCaptor.capture(), eq(true), eq(metadataClient));
        MetadataResolver createdMetadataResolver = metadataResolverRepository.getMetadataResolver(trustAnchor.getKeyID()).get();
        MetadataResolverConfiguration metadataResolverConfiguration = metadataResolverConfigurationCaptor.getValue();
        byte[] expectedTrustStoreCertificate = trustAnchor.getX509CertChain().get(0).decode();
        byte[] expectedTrustStoreCACertificate = trustAnchor.getX509CertChain().get(1).decode();
        byte[] actualTrustStoreCertificate = metadataResolverConfiguration.getTrustStore().getCertificate("certificate-0").getEncoded();
        byte[] actualTrustStoreCACertificate = metadataResolverConfiguration.getTrustStore().getCertificate("certificate-1").getEncoded();

        assertThat(createdMetadataResolver).isEqualTo(metadataResolver);
        assertThat(expectedTrustStoreCertificate).containsExactly(actualTrustStoreCertificate);
        assertThat(expectedTrustStoreCACertificate).containsExactly(actualTrustStoreCACertificate);
        assertThat(metadataResolverConfiguration.getUri().toString()).isEqualTo("https://source.local/" + ResourceEncoder.entityIdAsResource(entityId));
        assertThat(metadataResolverRepository.getSignatureTrustEngine(trustAnchor.getKeyID())).isEqualTo(Optional.of(explicitKeySignatureTrustEngine));
    }

    @Test
    void shouldUseEarliestExpiryDateOfX509Cert() throws ComponentInitializationException, CertificateException, SignatureException, ParseException, JOSEException {
        when(trustAnchorResolver.getTrustAnchors()).thenReturn(trustAnchors);
        when(dropwizardMetadataResolverFactory.createMetadataResolverWithClient(any(), eq(true), eq(metadataClient))).thenReturn(metadataResolver);
        when(metadataSignatureTrustEngineFactory.createSignatureTrustEngine(metadataResolver)).thenReturn(explicitKeySignatureTrustEngine);

        String entityId = "http://signin.gov.uk/entity-id";

        List<String> stringCertsChain = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_IDP_CA,
                TestCertificateStrings.STUB_COUNTRY_PUBLIC_TERTIARY_CERT
        );

        JWK trustAnchor = createJWK(entityId, stringCertsChain, true);
        trustAnchors.add(trustAnchor);

        when(metadataConfiguration.getMetadataSourceUri()).thenReturn(UriBuilder.fromUri("https://source.local").build());
        when(metadataConfiguration.getTrustAnchorMaxRefreshDelay()).thenReturn(Duration.ofSeconds(60));
        EidasMetadataResolverRepository metadataResolverRepository = new EidasMetadataResolverRepository(
                trustAnchorResolver,
                metadataConfiguration,
                dropwizardMetadataResolverFactory,
                timer,
                metadataSignatureTrustEngineFactory,
                new MetadataResolverConfigBuilder(),
                metadataClient);

        verify(dropwizardMetadataResolverFactory).createMetadataResolverWithClient(metadataResolverConfigurationCaptor.capture(), eq(true), eq(metadataClient));

        MetadataResolver createdMetadataResolver = metadataResolverRepository.getMetadataResolver(trustAnchor.getKeyID()).get();
        MetadataResolverConfiguration metadataResolverConfiguration = metadataResolverConfigurationCaptor.getValue();
        metadataResolverConfiguration.getMinRefreshDelay();

        List<X509Certificate> sortedCerts = metadataResolverRepository.sortCertsByDate(trustAnchor);

        assertThat(trustAnchor.getX509CertChain().size()).isEqualTo(3);
        assertThat(createdMetadataResolver).isEqualTo(metadataResolver);
        assertThat(sortedCerts.get(0).getNotAfter()).isBefore(sortedCerts.get(1).getNotAfter());
        assertThat(sortedCerts.get(1).getNotAfter()).isBefore(sortedCerts.get(2).getNotAfter());
        assertThat(sortedCerts.get(0)).isEqualTo(certificateFactory.createCertificate(CACertificates.TEST_ROOT_CA));
    }

    @Test
    void shouldNotCreateMetadataResolverAndLogWhenCertificateIsExpired() throws CertificateException, SignatureException, ParseException, JOSEException {
        when(trustAnchorResolver.getTrustAnchors()).thenReturn(trustAnchors);
        when(metadataConfiguration.getTrustAnchorMaxRefreshDelay()).thenReturn(Duration.ofSeconds(60));
        Logger logger = (Logger) LoggerFactory.getLogger(EidasMetadataResolverRepository.class);
        logger.addAppender(mockAppender);
        ArgumentCaptor<LoggingEvent> loggingEventCaptor = ArgumentCaptor.forClass(LoggingEvent.class);

        String entityId = "http://signin.gov.uk/entity-id";
        List<String> certificateChain = asList(
            CACertificates.TEST_ROOT_CA,
            CACertificates.TEST_IDP_CA,
            TestCertificateStrings.STUB_COUNTRY_PUBLIC_EXPIRED_CERT
        );
        trustAnchors.add(createJWK(entityId, certificateChain, false));
        trustAnchors.add(createJWK(entityId, certificateChain, false));
        trustAnchors.add(createJWK(entityId, certificateChain, false));

        EidasMetadataResolverRepository metadataResolverRepository = new EidasMetadataResolverRepository(
            trustAnchorResolver,
            metadataConfiguration,
            dropwizardMetadataResolverFactory,
            timer,
            metadataSignatureTrustEngineFactory,
            new MetadataResolverConfigBuilder(),
            metadataClient);

        assertThat(metadataResolverRepository.getMetadataResolver(entityId)).isEmpty();
        assertThat(metadataResolverRepository.getSignatureTrustEngine(entityId)).isEmpty();
        verify(mockAppender, times(3)).doAppend(loggingEventCaptor.capture());
        assertThat(loggingEventCaptor.getAllValues().stream().map(LoggingEvent::getMessage).collect(Collectors.toList()))
            .contains(String.format("Error creating MetadataResolver for %s", entityId));
    }

    @Test
    void shouldNotCreateMetadataResolverRepositoryWhenCertificateIsInvalid() throws CertificateException, SignatureException, ParseException, JOSEException {
        when(trustAnchorResolver.getTrustAnchors()).thenReturn(trustAnchors);
        when(metadataConfiguration.getTrustAnchorMaxRefreshDelay()).thenReturn(Duration.ofSeconds(60));
        String entityId = "http://signin.gov.uk/entity-id";
        List<String> invalidCertChain = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_IDP_CA,
                TestCertificateStrings.STUB_COUNTRY_PUBLIC_NOT_YET_VALID_CERT
        );
        trustAnchors.add(createJWK(entityId, invalidCertChain, false));

        final Error e = Assertions.assertThrows(Error.class, () -> new EidasMetadataResolverRepository(
                trustAnchorResolver,
                metadataConfiguration,
                dropwizardMetadataResolverFactory,
                timer,
                metadataSignatureTrustEngineFactory,
                new MetadataResolverConfigBuilder(),
                metadataClient));
        assertThat(e.getMessage()).startsWith("Managed to generate an invalid anchor: Certificate CN=IDA Stub Country Signing Dev");
    }

    @Test
    public void shouldNotRefreshMetadataResolversIfNewTrustAnchorsAreTheSameAsCurrentDespiteOrder() throws CertificateException, SignatureException, ParseException, JOSEException {
        when(dropwizardMetadataResolverFactory.createMetadataResolverWithClient(any(), eq(true), eq(metadataClient)))
                .thenReturn(metadataResolver, metadataResolver2);

        List<String> certificateChain1 = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_METADATA_CA,
                TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT
        );
        List<String> certificateChain2 = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_METADATA_CA,
                TestCertificateStrings.METADATA_SIGNING_B_PUBLIC_CERT
        );

        String entityId1 = "http://signin.gov.uk/entity/1";
        String entityId2 = "http://signin.gov.uk/entity/2";

        JWK trustAnchor1 = createJWK(entityId1, certificateChain1,true);
        JWK trustAnchor2 = createJWK(entityId2, certificateChain2,true);

        EidasMetadataResolverRepository metadataResolverRepository = createMetadataResolverRepositoryWithTrustAnchors(
                trustAnchor1,
                trustAnchor2
        );

        MetadataResolver metadataResolver1 = metadataResolverRepository.getMetadataResolver(entityId1).get();
        MetadataResolver metadataResolver2 = metadataResolverRepository.getMetadataResolver(entityId2).get();

        when(trustAnchorResolver.getTrustAnchors()).thenReturn(Arrays.asList(trustAnchor2, trustAnchor1));

        metadataResolverRepository.refresh();

        MetadataResolver metadataResolver1AfterRefresh = metadataResolverRepository.getMetadataResolver(entityId1).get();
        MetadataResolver metadataResolver2AfterRefresh = metadataResolverRepository.getMetadataResolver(entityId2).get();

        assertThat(metadataResolver1).isSameAs(metadataResolver1AfterRefresh);
        assertThat(metadataResolver2).isSameAs(metadataResolver2AfterRefresh);
    }

    @Test
    public void shouldRefreshMetadataResolversIfNewTrustAnchorsAreDifferentToCurrent() throws CertificateException, SignatureException, ParseException, JOSEException {
        when(dropwizardMetadataResolverFactory.createMetadataResolverWithClient(any(), eq(true), eq(metadataClient)))
                .thenReturn(metadataResolver, metadataResolver2, metadataResolver3, metadataResolver4);

        List<String> certificateChain1 = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_METADATA_CA,
                TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT
        );
        List<String> certificateChain2 = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_METADATA_CA,
                TestCertificateStrings.METADATA_SIGNING_B_PUBLIC_CERT
        );
        List<String> certificateChain3 = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_RP_CA,
                TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT
        );

        String entityId1 = "http://signin.gov.uk/entity/1";
        String entityId2 = "http://signin.gov.uk/entity/2";

        JWK trustAnchor1 = createJWK(entityId1, certificateChain1,true);
        JWK trustAnchor2 = createJWK(entityId2, certificateChain2,true);
        JWK trustAnchor3 = createJWK(entityId2, certificateChain3,true);

        EidasMetadataResolverRepository metadataResolverRepository = createMetadataResolverRepositoryWithTrustAnchors(
                trustAnchor1,
                trustAnchor2
        );

        MetadataResolver metadataResolver1 = metadataResolverRepository.getMetadataResolver(entityId1).get();
        MetadataResolver metadataResolver2 = metadataResolverRepository.getMetadataResolver(entityId2).get();

        when(trustAnchorResolver.getTrustAnchors()).thenReturn(Arrays.asList(trustAnchor1, trustAnchor3));

        metadataResolverRepository.refresh();

        MetadataResolver metadataResolver1AfterRefresh = metadataResolverRepository.getMetadataResolver(entityId1).get();
        MetadataResolver metadataResolver2AfterRefresh = metadataResolverRepository.getMetadataResolver(entityId2).get();

        assertThat(metadataResolver1).isNotSameAs(metadataResolver1AfterRefresh);
        assertThat(metadataResolver2).isNotSameAs(metadataResolver2AfterRefresh);
    }

    @Test
    void shouldAddNewMetadataResolverWhenRefreshing() throws CertificateException, SignatureException, ParseException, JOSEException, ComponentInitializationException {
        when(dropwizardMetadataResolverFactory.createMetadataResolverWithClient(any(), eq(true), eq(metadataClient))).thenReturn(metadataResolver);
        when(metadataSignatureTrustEngineFactory.createSignatureTrustEngine(metadataResolver)).thenReturn(explicitKeySignatureTrustEngine);
        when(metadataConfiguration.getTrustAnchorMaxRefreshDelay()).thenReturn(Duration.ofSeconds(60));

        EidasMetadataResolverRepository metadataResolverRepository = createMetadataResolverRepositoryWithTrustAnchors();

        assertThat(metadataResolverRepository.getTrustAnchorsEntityIds()).hasSize(0);

        List<String> certificateChain = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_METADATA_CA,
                TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT
        );
        JWK trustAnchor1 = createJWK("http://signin.gov.uk/entity/id", certificateChain, true);
        when(trustAnchorResolver.getTrustAnchors()).thenReturn(singletonList(trustAnchor1));
        metadataResolverRepository.refresh();

        assertThat(metadataResolverRepository.getTrustAnchorsEntityIds()).hasSize(1);
    }

    @Test
    void shouldRemoveOldMetadataResolverWhenRefreshing() throws CertificateException, SignatureException, ParseException, JOSEException, ComponentInitializationException {
        when(dropwizardMetadataResolverFactory.createMetadataResolverWithClient(any(), eq(true), eq(metadataClient))).thenReturn(metadataResolver);
        when(metadataSignatureTrustEngineFactory.createSignatureTrustEngine(metadataResolver)).thenReturn(explicitKeySignatureTrustEngine);

        List<String> certificateChain = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_METADATA_CA,
                TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT
        );
        JWK trustAnchor1 = createJWK("http://signin.gov.uk/entity/id", certificateChain,true);
        JWK trustAnchor2 = createJWK("http://signin.gov.uk/entity/id", certificateChain,true);

        EidasMetadataResolverRepository metadataResolverRepository = createMetadataResolverRepositoryWithTrustAnchors(trustAnchor1, trustAnchor2);

        assertThat(metadataResolverRepository.getTrustAnchorsEntityIds()).hasSize(2);

        when(trustAnchorResolver.getTrustAnchors()).thenReturn(singletonList(trustAnchor2));
        metadataResolverRepository.refresh();

        assertThat(metadataResolverRepository.getTrustAnchorsEntityIds()).hasSize(1);
        assertThat(metadataResolverRepository.getTrustAnchorsEntityIds()).contains(trustAnchor2.getKeyID());
    }

    @Test
    void shouldNotRecreateExistingMetadataResolversWhenRefreshing() throws ParseException, CertificateException, JOSEException, SignatureException, ComponentInitializationException {
        when(dropwizardMetadataResolverFactory.createMetadataResolverWithClient(any(), eq(true), eq(metadataClient))).thenReturn(metadataResolver);
        when(metadataSignatureTrustEngineFactory.createSignatureTrustEngine(metadataResolver)).thenReturn(explicitKeySignatureTrustEngine);

        List<String> certificateChain = asList(
                CACertificates.TEST_ROOT_CA,
                CACertificates.TEST_METADATA_CA,
                TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT
        );
        EidasMetadataResolverRepository metadataResolverRepository = createMetadataResolverRepositoryWithTrustAnchors(createJWK("http://signin.gov.uk/entity/id", certificateChain, true));

        Map<String, MetadataResolver> originalMetadataResolvers = metadataResolverRepository.getMetadataResolvers();
        reset(dropwizardMetadataResolverFactory);
        metadataResolverRepository.refresh();

        verifyNoMoreInteractions(dropwizardMetadataResolverFactory);
        Map<String, MetadataResolver> refreshedMetadataResolvers = metadataResolverRepository.getMetadataResolvers();
        refreshedMetadataResolvers.forEach((key, value) -> assertThat(value == originalMetadataResolvers.get(key)).isTrue());
    }

    private EidasMetadataResolverRepository createMetadataResolverRepositoryWithTrustAnchors(JWK... trustAnchors) throws ParseException, CertificateException, JOSEException, SignatureException {
        if(Objects.nonNull(trustAnchors) && trustAnchors.length>0) {
            when(trustAnchorResolver.getTrustAnchors()).thenReturn(asList(trustAnchors));
            when(metadataConfiguration.getTrustAnchorMaxRefreshDelay()).thenReturn(Duration.ofSeconds(60));
        }

        return new EidasMetadataResolverRepository(
                trustAnchorResolver,
                metadataConfiguration,
                dropwizardMetadataResolverFactory,
                timer,
                metadataSignatureTrustEngineFactory,
                metadataResolverConfigBuilder,
                metadataClient);
    }

    private JWK createJWK(String entityId, List<String> certificates, Boolean validate) {
        List<X509Certificate> certs = certificates.stream().map(certificateFactory::createCertificate).collect(Collectors.toList());
        return CountryTrustAnchor.make(certs, entityId, validate);
    }

}
