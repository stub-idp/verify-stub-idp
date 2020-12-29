package stubidp.saml.metadata;

import certificates.values.CACertificates;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import stubidp.saml.test.metadata.EntityDescriptorFactory;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.test.devpki.TestEntityIds;
import stubidp.test.utils.keystore.KeyStoreRule;
import stubidp.test.utils.keystore.builders.KeyStoreRuleBuilder;
import stubidp.utils.security.security.X509CertificateFactory;
import stubidp.utils.security.security.verification.CertificateChainValidator;
import stubidp.utils.security.security.verification.PKIXParametersProvider;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class CertificateChainValidationFilterTest extends OpenSAMLRunner {
    private static final List<String> IDP_ENTITY_IDS = asList(TestEntityIds.STUB_IDP_ONE, TestEntityIds.STUB_IDP_TWO, TestEntityIds.STUB_IDP_THREE, TestEntityIds.STUB_IDP_FOUR);
    private static final List<String> HUB_ENTITY_IDS = Collections.singletonList(TestEntityIds.HUB_ENTITY_ID);
    private static final List<String> HUB_KEY_NAMES = asList(EntityDescriptorFactory.SIGNING_ONE, EntityDescriptorFactory.SIGNING_TWO, EntityDescriptorFactory.ENCRYPTION);

    @RegisterExtension
    static final KeyStoreRule idpKeyStoreRule = KeyStoreRuleBuilder.aKeyStoreRule().withCertificate("idp", CACertificates.TEST_IDP_CA)
                                                                    .withCertificate("root", CACertificates.TEST_ROOT_CA).build();

    @RegisterExtension
    static final KeyStoreRule hubKeyStoreRule = KeyStoreRuleBuilder.aKeyStoreRule().withCertificate("hub", CACertificates.TEST_CORE_CA)
                                                                    .withCertificate("root", CACertificates.TEST_ROOT_CA).build();

    @RegisterExtension
    static final KeyStoreRule rpKeyStoreRule = KeyStoreRuleBuilder.aKeyStoreRule().withCertificate("rp", CACertificates.TEST_RP_CA)
                                                                   .withCertificate("root", CACertificates.TEST_ROOT_CA).build();

    private final MetadataFactory metadataFactory = new MetadataFactory();
    private final CertificateChainValidator certificateChainValidator = new CertificateChainValidator(new PKIXParametersProvider(), new X509CertificateFactory());

    @Test
    void shouldNotFilterOutTrustedCertificatesWhenAllCertificatesAreValid() throws Exception {
        final CertificateChainValidationFilter spCertificateChainValidationFilter = new CertificateChainValidationFilter(SPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, hubKeyStoreRule.getKeyStore());
        final CertificateChainValidationFilter idpCertificateChainValidationFilter = new CertificateChainValidationFilter(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, idpKeyStoreRule.getKeyStore());

        XMLObject metadata = validateMetadata(spCertificateChainValidationFilter, metadataFactory.defaultMetadata());
        metadata = idpCertificateChainValidationFilter.filter(metadata, null);

        assertThat(getEntityIdsFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).hasSameElementsAs(HUB_ENTITY_IDS);
        assertThat(getKeyNamesFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME, TestEntityIds.HUB_ENTITY_ID)).hasSameElementsAs(HUB_KEY_NAMES);
        assertThat(getEntityIdsFromMetadata(metadata, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)).hasSameElementsAs(IDP_ENTITY_IDS);
    }

    @Test
    void shouldReturnNullWhenMetadataIsEmpty() throws Exception {
        final CertificateChainValidationFilter spCertificateChainValidationFilter = new CertificateChainValidationFilter(SPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, hubKeyStoreRule.getKeyStore());
        final CertificateChainValidationFilter idpCertificateChainValidationFilter = new CertificateChainValidationFilter(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, idpKeyStoreRule.getKeyStore());

        XMLObject metadata = validateMetadata(spCertificateChainValidationFilter, metadataFactory.emptyMetadata());
        metadata = idpCertificateChainValidationFilter.filter(metadata, null);

        assertThat(metadata).isNull();
    }

    @Test
    void shouldFilterOutUntrustedIdpCertificatesWhenAllIdpCertificatesAreNotSignedByCorrectCA() throws Exception {
        final CertificateChainValidationFilter certificateChainValidationFilter = new CertificateChainValidationFilter(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, hubKeyStoreRule.getKeyStore());

        final XMLObject metadata = validateMetadata(certificateChainValidationFilter, metadataFactory.defaultMetadata());

        assertThat(getEntityIdsFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).hasSameElementsAs(HUB_ENTITY_IDS);
        assertThat(getKeyNamesFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME, TestEntityIds.HUB_ENTITY_ID)).hasSameElementsAs(HUB_KEY_NAMES);
        assertThat(getEntityIdsFromMetadata(metadata, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEmpty();
    }

    @Test
    void shouldFilterOutUntrustedHubCertificatesWhenAllHubCertificatesAreNotSignedByCorrectCA() throws Exception {
        final CertificateChainValidationFilter certificateChainValidationFilter = new CertificateChainValidationFilter(SPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, idpKeyStoreRule.getKeyStore());

        final XMLObject metadata = validateMetadata(certificateChainValidationFilter, metadataFactory.defaultMetadata());

        assertThat(getEntityIdsFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEmpty();
        assertThat(getEntityIdsFromMetadata(metadata, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)).hasSameElementsAs(IDP_ENTITY_IDS);
    }

    @Test
    void shouldReturnNullWhenAllCertificatesAreNotSignedByCorrectCA() throws Exception {
        final CertificateChainValidationFilter spCertificateChainValidationFilter = new CertificateChainValidationFilter(SPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, rpKeyStoreRule.getKeyStore());
        final CertificateChainValidationFilter idpCertificateChainValidationFilter = new CertificateChainValidationFilter(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, rpKeyStoreRule.getKeyStore());

        XMLObject metadata = validateMetadata(spCertificateChainValidationFilter, metadataFactory.defaultMetadata());
        metadata = idpCertificateChainValidationFilter.filter(metadata, null);

        assertThat(metadata).isNull();
    }

    @Test
    void shouldFilterOutUntrustedIdpCertificateWhenOneIdpCertificateIsNotSignedByCorrectCA() throws Exception {
        final CertificateChainValidationFilter certificateChainValidationFilter = new CertificateChainValidationFilter(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, idpKeyStoreRule.getKeyStore());
        final EntityDescriptorFactory entityDescriptorFactory =  new EntityDescriptorFactory();
        String metadataWithOneBadIdpCertificate = metadataFactory.metadata(
            asList(
                entityDescriptorFactory.hubEntityDescriptor(),
                entityDescriptorFactory.idpEntityDescriptor(TestEntityIds.STUB_IDP_ONE),
                entityDescriptorFactory.idpEntityDescriptor(TestEntityIds.STUB_IDP_TWO),
                entityDescriptorFactory.idpEntityDescriptor(TestEntityIds.STUB_IDP_THREE),
                entityDescriptorFactory.idpEntityDescriptor(TestEntityIds.STUB_IDP_FOUR),
                entityDescriptorFactory.idpEntityDescriptor(TestEntityIds.TEST_RP)));

        final XMLObject metadata = validateMetadata(certificateChainValidationFilter, metadataWithOneBadIdpCertificate);

        assertThat(getEntityIdsFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).hasSameElementsAs(HUB_ENTITY_IDS);
        assertThat(getKeyNamesFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME, TestEntityIds.HUB_ENTITY_ID)).hasSameElementsAs(HUB_KEY_NAMES);
        assertThat(getEntityIdsFromMetadata(metadata, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)).hasSameElementsAs(IDP_ENTITY_IDS);
        assertThat(getEntityIdsFromMetadata(metadata, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)).doesNotContain(TestEntityIds.TEST_RP);
    }

    @Test
    void shouldFilterOutUntrustedHubSigningCertificateWhenAHubSigningCertificateIsNotSignedByCorrectCA() throws Exception {
        final CertificateChainValidationFilter spCertificateChainValidationFilter = new CertificateChainValidationFilter(SPSSODescriptor.DEFAULT_ELEMENT_NAME, certificateChainValidator, hubKeyStoreRule.getKeyStore());
        final EntityDescriptorFactory entityDescriptorFactory =  new EntityDescriptorFactory();
        String metadataWithOneBadKeyName = metadataFactory.metadata(Collections.singletonList(entityDescriptorFactory.badHubEntityDescriptor()));

        final XMLObject metadata = validateMetadata(spCertificateChainValidationFilter, metadataWithOneBadKeyName);

        assertThat(getEntityIdsFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).hasSameElementsAs(HUB_ENTITY_IDS);
        assertThat(getKeyNamesFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME, TestEntityIds.HUB_ENTITY_ID)).hasSameElementsAs(HUB_KEY_NAMES);
        assertThat(getKeyNamesFromMetadata(metadata, SPSSODescriptor.DEFAULT_ELEMENT_NAME, TestEntityIds.HUB_ENTITY_ID)).doesNotContain(EntityDescriptorFactory.SIGNING_BAD);
    }

    private XMLObject validateMetadata(final CertificateChainValidationFilter certificateChainValidationFilter, String metadataContent) throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        XMLObject metadata = XMLObjectSupport.unmarshallFromInputStream(parserPool, new ByteArrayInputStream(metadataContent.getBytes(UTF_8)));
        return certificateChainValidationFilter.filter(metadata, null);
    }

    private List<String> getEntityIdsFromMetadata(final XMLObject metadata, final QName role) {
        List<String> entityIds = new ArrayList<>();
        if (metadata != null) {
            final EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) metadata;

            entitiesDescriptor.getEntityDescriptors().forEach(entityDescriptor -> {
                final String entityID = entityDescriptor.getEntityID();
                entityDescriptor.getRoleDescriptors()
                                .stream()
                                .filter(roleDescriptor -> roleDescriptor.getElementQName().equals(role))
                                .map(roleDescriptor -> entityID)
                                .forEach(entityIds::add);
            });
        }
        return entityIds;
    }

    private List<String> getKeyNamesFromMetadata(final XMLObject metadata, final QName role, final String entityId) {
        List<String> keyNames = new ArrayList<>();
        if (metadata != null) {
            final EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) metadata;

            entitiesDescriptor.getEntityDescriptors()
                              .stream()
                              .filter(entityDescriptor -> entityId.equals(entityDescriptor.getEntityID()))
                              .forEach(
                                entityDescriptor ->
                                    entityDescriptor.getRoleDescriptors()
                                                    .stream()
                                                    .filter(roleDescriptor -> roleDescriptor.getElementQName().equals(role))
                                                    .forEach(
                                                        roleDescriptor ->
                                                            roleDescriptor.getKeyDescriptors()
                                                                          .forEach(
                                                                                keyDescriptor ->
                                                                                    keyDescriptor.getKeyInfo()
                                                                                                 .getKeyNames()
                                                                                                 .forEach(
                                                                                                    keyName ->
                                                                                                        keyNames.add(keyName.getValue())))));
        }
        return keyNames;
    }
}
