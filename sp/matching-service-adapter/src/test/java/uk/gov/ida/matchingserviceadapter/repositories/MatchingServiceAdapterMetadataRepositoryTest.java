package uk.gov.ida.matchingserviceadapter.repositories;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.assertj.core.data.TemporalUnitWithinOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import stubidp.saml.security.StringBackedMetadataResolver;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.metadata.EntityDescriptorFactory;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.metadata.transformers.KeyDescriptorsUnmarshaller;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.security.security.Certificate;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.CertificateStore;
import uk.gov.ida.matchingserviceadapter.exceptions.FederationMetadataLoadingException;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static stubidp.utils.common.string.StringEncoding.toBase64Encoded;
import static stubidp.utils.common.xml.XmlUtils.writeToString;

@ExtendWith(MockitoExtension.class)
public class MatchingServiceAdapterMetadataRepositoryTest extends OpenSAMLRunner {

    private final String hubSsoEndPoint = "http://localhost:50300/SAML2/SSO";

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));

    @Mock
    private MatchingServiceAdapterConfiguration msaConfiguration;

    @Mock
    private MetadataResolver msaMetadataResolver;

    @Mock
    private CertificateStore certificateStore;

    @Mock
    private MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;

    private KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller;

    private Function<EntitiesDescriptor, Element> entityDescriptorElementTransformer;

    private MatchingServiceAdapterMetadataRepository matchingServiceAdapterMetadataRepository;

    private String entityId = "http://issuer";

    @BeforeEach
    public void setUp() throws Exception {
        when(msaConfiguration.getEntityId()).thenReturn(entityId);
        when(msaConfiguration.getMatchingServiceAdapterExternalUrl()).thenReturn(URI.create("http://localhost"));
        when(certificateStore.getEncryptionCertificates()).thenReturn(Collections.emptyList());
        when(matchingServiceAdapterConfiguration.getHubSSOUri()).thenReturn(URI.create(hubSsoEndPoint));
        when(matchingServiceAdapterConfiguration.shouldRepublishHubCertificates()).thenReturn(false);

        entityDescriptorElementTransformer = new CoreTransformersFactory().getXmlObjectToElementTransformer();
        keyDescriptorsUnmarshaller = new KeyDescriptorsUnmarshaller(new OpenSamlXmlObjectFactory());

        matchingServiceAdapterMetadataRepository = new MatchingServiceAdapterMetadataRepository(
                msaConfiguration,
                keyDescriptorsUnmarshaller,
                entityDescriptorElementTransformer,
                certificateStore,
                msaMetadataResolver,
                matchingServiceAdapterConfiguration,
                TestEntityIds.HUB_ENTITY_ID,
                clock);
    }

    @Test
    public void shouldNotReturnTheHubEntityDescriptorWhenConfiguredToNotNeedHubCerts() throws Exception {
        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        assertThat(getEntityDescriptor(matchingServiceAdapterMetadata, TestEntityIds.HUB_ENTITY_ID)).isNull();
    }

    @Test
    public void shouldHaveAnIDPSSODescriptor() throws ResolverException, FederationMetadataLoadingException {
        when(certificateStore.getSigningCertificates()).thenReturn(Collections.singletonList(getCertificate()));

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntityDescriptor msa = getEntityDescriptor(matchingServiceAdapterMetadata, entityId);

        assertThat(msa.getRoleDescriptors().size()).isEqualTo(2);
        IDPSSODescriptor idpssoDescriptor = msa.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
        assertThat(idpssoDescriptor).isNotNull();
        assertThat(idpssoDescriptor.getSingleSignOnServices()).hasSize(1);
        assertThat(idpssoDescriptor.getSingleSignOnServices().get(0).getLocation()).isEqualTo(hubSsoEndPoint);

        // Shibboleth SP doesn't like the xsi:type="md:EndpointType" attribute on the SingleSignOnService element:
        assertThat(idpssoDescriptor.getSingleSignOnServices().get(0).getSchemaType()).isNull();

        assertThat(idpssoDescriptor.getKeyDescriptors()).hasSize(1);
    }

    @Test
    public void shouldHaveOneSigningKeyDescriptorWhenMsaIsConfiguredWithNoSecondaryPublicSigningKey() throws Exception {
        when(certificateStore.getSigningCertificates()).thenReturn(Collections.singletonList(getCertificate()));

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntityDescriptor msa = getEntityDescriptor(matchingServiceAdapterMetadata, entityId);

        assertThat(msa.getRoleDescriptors().size()).isEqualTo(2);
        assertThat(msa.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().size()).isEqualTo(1);
    }

    @Test
    public void shouldHaveTwoSigningKeyDescriptorsWhenMsaIsConfiguredWithSecondaryPublicSigningKey() throws Exception {
        when(certificateStore.getSigningCertificates()).thenReturn(List.of(getCertificate(), getCertificate()));

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntityDescriptor msa = getEntityDescriptor(matchingServiceAdapterMetadata, entityId);

        assertThat(msa.getRoleDescriptors().size()).isEqualTo(2);
        assertThat(msa.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnTheHubEntityDescriptorInMSAMetadataWhenConfiguredToDoSo() throws Exception {
        when(matchingServiceAdapterConfiguration.shouldRepublishHubCertificates()).thenReturn(true);
        when(msaMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TestEntityIds.HUB_ENTITY_ID)))).thenReturn(new EntityDescriptorFactory().hubEntityDescriptor());

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntityDescriptor hub = getEntityDescriptor(matchingServiceAdapterMetadata, TestEntityIds.HUB_ENTITY_ID);

        assertThat(hub.getRoleDescriptors().size()).isEqualTo(1);
        assertThat(hub.getSPSSODescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().size()).isEqualTo(3);
    }

    @Test
    public void shouldThrowAnExceptionIfFederationMetadataCannotBeLoadedORHubIsMissing() throws Exception {
        when(matchingServiceAdapterConfiguration.shouldRepublishHubCertificates()).thenReturn(true);
        when(msaMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TestEntityIds.HUB_ENTITY_ID)))).thenReturn(null);
        when(certificateStore.getSigningCertificates()).thenReturn(Collections.singletonList(getCertificate()));

        assertThatExceptionOfType(FederationMetadataLoadingException.class)
                .isThrownBy(() -> matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata());
    }

    @Test
    public void shouldBeAbleToLoadMSAMetadataUsingMetadataResolver() throws Exception {
        when(matchingServiceAdapterConfiguration.shouldRepublishHubCertificates()).thenReturn(true);
        when(msaMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TestEntityIds.HUB_ENTITY_ID)))).thenReturn(new EntityDescriptorFactory().hubEntityDescriptor());

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        String metadata = writeToString(matchingServiceAdapterMetadata);

        StringBackedMetadataResolver stringBackedMetadataResolver = new StringBackedMetadataResolver(metadata);
        BasicParserPool pool = new BasicParserPool();
        pool.initialize();
        stringBackedMetadataResolver.setParserPool(pool);
        stringBackedMetadataResolver.setId("Some ID");
        stringBackedMetadataResolver.initialize();

        assertThat(stringBackedMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId))).getEntityID()).isEqualTo(entityId);
        assertThat(stringBackedMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TestEntityIds.HUB_ENTITY_ID))).getEntityID()).isEqualTo(TestEntityIds.HUB_ENTITY_ID);
    }

    @Test
    public void shouldGenerateMetadataValidFor1Hour() throws Exception {
        when(certificateStore.getSigningCertificates()).thenReturn(Collections.singletonList(getCertificate()));

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntitiesDescriptor entitiesDescriptor = getEntitiesDescriptor(matchingServiceAdapterMetadata);

        assertThat(entitiesDescriptor.getValidUntil()).isCloseTo(Instant.now(clock).plus(1, ChronoUnit.HOURS), new TemporalUnitWithinOffset(10, ChronoUnit.SECONDS));
    }

    private Certificate getCertificate() {
        return new Certificate(entityId, TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT, Certificate.KeyUse.Signing);
    }

    private EntityDescriptor getEntityDescriptor(Document matchingServiceAdapterMetadata, String entityId) {
        EntitiesDescriptor entitiesDescriptor = getEntitiesDescriptor(matchingServiceAdapterMetadata);

        EntityDescriptor matchingEntityDescriptor = null;
        for (EntityDescriptor entityDescriptor : entitiesDescriptor.getEntityDescriptors()) {
            if (entityDescriptor.getEntityID().equals(entityId)) {
                matchingEntityDescriptor = entityDescriptor;
            }
        }
        return matchingEntityDescriptor;
    }

    private EntitiesDescriptor getEntitiesDescriptor(Document matchingServiceAdapterMetadata) {
        StringToOpenSamlObjectTransformer<XMLObject> stringtoOpenSamlObjectTransformer = new CoreTransformersFactory().getStringtoOpenSamlObjectTransformer(input -> {});

        return (EntitiesDescriptor) stringtoOpenSamlObjectTransformer.apply(toBase64Encoded(writeToString(matchingServiceAdapterMetadata)));
    }
}
