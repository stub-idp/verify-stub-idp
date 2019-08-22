package stubidp.saml.hub.hub.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.EncryptedAssertionBuilder;
import stubidp.saml.hub.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.HubAssertion;
import stubidp.saml.utils.core.domain.PersistentId;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.hub.hub.domain.HubEidasAttributeQueryRequest;
import stubidp.saml.utils.hub.factories.AttributeFactory_1_1;
import stubidp.saml.hub.hub.factories.AttributeQueryAttributeFactory;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static stubidp.saml.utils.core.test.builders.HubAssertionBuilder.aHubAssertion;
import static stubidp.saml.hub.hub.domain.UserAccountCreationAttribute.CURRENT_ADDRESS;
import static stubidp.saml.hub.hub.domain.UserAccountCreationAttribute.DATE_OF_BIRTH;
import static stubidp.saml.hub.hub.builders.HubEidasAttributeQueryRequestBuilder.aHubEidasAttributeQueryRequest;

@ExtendWith(MockitoExtension.class)
public class HubEidasAttributeQueryRequestToSamlAttributeQueryTransformerTest extends OpenSAMLRunner {

    public static final String ENCRYPTED_IDENTITY_ASSERTION = "encrypted-identity-assertion!";

    private HubEidasAttributeQueryRequestToSamlAttributeQueryTransformer transformer;

    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    @Mock
    private AttributeFactory_1_1 attributeFactory;

    @Mock
    private StringToOpenSamlObjectTransformer<Assertion> stringAssertionTransformer;

    @Mock
    private OutboundAssertionToSubjectTransformer outboundAssertionToSubjectTransformer;

    @Mock
    private AttributeQueryAttributeFactory attributeQueryAttributeFactory;

    @Mock
    private EncryptedAssertionUnmarshaller encryptedAssertionUnmarshaller;

    @BeforeEach
    public void setup() {
        openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        HubAssertionMarshaller assertionTransformer = new HubAssertionMarshaller(openSamlXmlObjectFactory, attributeFactory, outboundAssertionToSubjectTransformer);
        AssertionFromIdpToAssertionTransformer assertionFromIdpAssertionTransformer = new AssertionFromIdpToAssertionTransformer(stringAssertionTransformer);

        transformer = new HubEidasAttributeQueryRequestToSamlAttributeQueryTransformer(
            openSamlXmlObjectFactory,
            assertionTransformer,
            assertionFromIdpAssertionTransformer,
            attributeQueryAttributeFactory,
            encryptedAssertionUnmarshaller);
    }

    @Test
    public void shouldTransformProperly() {
        PersistentId persistentId = new PersistentId("default-name-id");
        HubEidasAttributeQueryRequest hubEidasAttributeQueryRequest = aHubEidasAttributeQueryRequest()
            .withId("originalId")
            .withPersistentId(persistentId)
            .build();

        AttributeQuery transformedQuery = transformer.apply(hubEidasAttributeQueryRequest);

        assertThat(transformedQuery.getID()).isEqualTo(hubEidasAttributeQueryRequest.getId());
        assertThat(transformedQuery.getSubject().getNameID().getValue()).isEqualTo(persistentId.getNameId());
        assertThat(transformedQuery.getIssuer().getValue()).isEqualTo(hubEidasAttributeQueryRequest.getIssuer());
        assertThat(transformedQuery.getVersion()).isEqualTo(SAMLVersion.VERSION_20);
    }

    @Test
    public void shouldIncludeCycle3Assertion() {
        final HubAssertion cycle3DataAssertion = aHubAssertion().build();

        HubEidasAttributeQueryRequest hubEidasAttributeQueryRequest = aHubEidasAttributeQueryRequest()
            .withCycle3DataAssertion(cycle3DataAssertion)
            .build();

        AttributeQuery transformedQuery = transformer.apply(hubEidasAttributeQueryRequest);

        List<XMLObject> unknownXMLObjects = transformedQuery.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData().getUnknownXMLObjects(Assertion.DEFAULT_ELEMENT_NAME);
        assertThat(unknownXMLObjects.size()).isEqualTo(1);
        assertThat(((Assertion) unknownXMLObjects.get(0)).getID()).isEqualTo(cycle3DataAssertion.getId());
    }

    @Test
    public void shouldIncludeEncryptedIdentityAssertionOnly() {
        HubEidasAttributeQueryRequest hubEidasAttributeQueryRequest = aHubEidasAttributeQueryRequest()
            .withEncryptedIdentityAssertion(ENCRYPTED_IDENTITY_ASSERTION)
            .build();

        final EncryptedAssertion value = new EncryptedAssertionBuilder().buildObject();
        when(encryptedAssertionUnmarshaller.transform(ENCRYPTED_IDENTITY_ASSERTION)).thenReturn(value);
        AttributeQuery transformedQuery = transformer.apply(hubEidasAttributeQueryRequest);

        List<XMLObject> encryptedAssertions = transformedQuery.getSubject()
            .getSubjectConfirmations().get(0).getSubjectConfirmationData().getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME);
        assertThat(encryptedAssertions.size()).isEqualTo(1);
        assertThat(((EncryptedAssertion) encryptedAssertions.get(0))).isEqualTo(value);

        List<XMLObject> assertions = transformedQuery.getSubject()
            .getSubjectConfirmations().get(0).getSubjectConfirmationData().getUnknownXMLObjects(Assertion.DEFAULT_ELEMENT_NAME);
        assertThat(assertions.size()).isEqualTo(0);
    }

    @Test
    public void shouldIncludeEncryptedIdentityAssertionAndCycle3Assertion() {
        final HubAssertion cycle3DataAssertion = aHubAssertion().build();

        HubEidasAttributeQueryRequest hubEidasAttributeQueryRequest = aHubEidasAttributeQueryRequest()
            .withEncryptedIdentityAssertion(ENCRYPTED_IDENTITY_ASSERTION)
            .withCycle3DataAssertion(cycle3DataAssertion)
            .build();

        final EncryptedAssertion value = new EncryptedAssertionBuilder().buildObject();
        when(encryptedAssertionUnmarshaller.transform(ENCRYPTED_IDENTITY_ASSERTION)).thenReturn(value);
        AttributeQuery transformedQuery = transformer.apply(hubEidasAttributeQueryRequest);

        List<XMLObject> encryptedAssertions = transformedQuery.getSubject()
            .getSubjectConfirmations().get(0).getSubjectConfirmationData().getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME);
        assertThat(encryptedAssertions.size()).isEqualTo(1);
        assertThat(((EncryptedAssertion) encryptedAssertions.get(0))).isEqualTo(value);

        List<XMLObject> assertions = transformedQuery.getSubject()
            .getSubjectConfirmations().get(0).getSubjectConfirmationData().getUnknownXMLObjects(Assertion.DEFAULT_ELEMENT_NAME);
        assertThat(assertions.size()).isEqualTo(1);
        assertThat(((Assertion) assertions.get(0)).getID()).isEqualTo(cycle3DataAssertion.getId());
    }

    @Test
    public void shouldSetTheSPNameQualifierAndNameQualifierToValuesThatShouldntBeThereButCurrentlyHaveNoWhereBetterToBe() {
        final HubAssertion cycle3DataAssertion = aHubAssertion().build();

        HubEidasAttributeQueryRequest hubEidasAttributeQueryRequest = aHubEidasAttributeQueryRequest()
            .withCycle3DataAssertion(cycle3DataAssertion)
            .withAssertionConsumerServiceUrl(URI.create("/foo"))
            .withAuthnRequestIssuerEntityId("authn-request-issuer")
            .build();

        AttributeQuery transformedQuery = transformer.apply(hubEidasAttributeQueryRequest);

        NameID nameID = transformedQuery.getSubject().getNameID();

        assertThat(nameID.getSPNameQualifier()).isEqualTo("authn-request-issuer");
        assertThat(nameID.getNameQualifier()).isEqualTo("/foo");
    }

    @Test
    public void shouldSetAttributesToUserAccountCreationAttributes() {
        Attribute attribute1 = openSamlXmlObjectFactory.createAttribute();
        Attribute attribute2 = openSamlXmlObjectFactory.createAttribute();
        when(attributeQueryAttributeFactory.createAttribute(CURRENT_ADDRESS)).thenReturn(attribute1);
        when(attributeQueryAttributeFactory.createAttribute(DATE_OF_BIRTH)).thenReturn(attribute2);

        HubEidasAttributeQueryRequest hubEidasAttributeQueryRequest = aHubEidasAttributeQueryRequest()
            .addUserAccountCreationAttribute(CURRENT_ADDRESS)
            .addUserAccountCreationAttribute(DATE_OF_BIRTH)
            .build();

        AttributeQuery transformedQuery = transformer.apply(hubEidasAttributeQueryRequest);

        List<Attribute> transformedQueryAttributes = transformedQuery.getAttributes();
        assertThat(transformedQueryAttributes.size()).isEqualTo(2);
        assertThat(transformedQueryAttributes).contains(attribute1);
        assertThat(transformedQueryAttributes).contains(attribute2);
    }

    @Test
    public void shouldNotExplodeWhenUserAccountCreationAttributesAreAbsent() {
        HubEidasAttributeQueryRequest hubEidasAttributeQueryRequest = aHubEidasAttributeQueryRequest()
            .withoutUserAccountCreationAttributes()
            .build();

        AttributeQuery transformedQuery = transformer.apply(hubEidasAttributeQueryRequest);

        List<Attribute> transformedQueryAttributes = transformedQuery.getAttributes();
        assertThat(transformedQueryAttributes.size()).isEqualTo(0);
    }
}
