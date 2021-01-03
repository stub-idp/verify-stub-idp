package stubidp.saml.hub.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.StatusResponseType;
import org.opensaml.saml.saml2.core.impl.AttributeImpl;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.request.EidasAuthnRequestFromHub;
import stubidp.saml.extensions.extensions.RequestedAttribute;
import stubidp.saml.extensions.extensions.RequestedAttributes;
import stubidp.saml.extensions.extensions.SPType;
import stubidp.saml.extensions.extensions.impl.RequestedAttributeImpl;
import stubidp.saml.extensions.extensions.impl.SPTypeImpl;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.hub.domain.LevelOfAssurance;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.transformers.AuthnContextFactory;

import javax.xml.namespace.QName;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.EidasAuthnRequestBuilder.anEidasAuthnRequest;

class EidasAuthnRequestFromHubToAuthnRequestTransformerTest extends OpenSAMLRunner {

    private static final String A_PROVIDER = "A_PROVIDER";
    private static final String HTTP_ISSUER_ENTITY_ID_COM = "http://issuer-entity-id.com";
    private static final String AUTHN_REQUEST_ID = "aTestId";
    private static final String EIDAS_SSO_LOCATION = "http://eidas/ssoLocation";

    private EidasAuthnRequestFromHubToAuthnRequestTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new EidasAuthnRequestFromHubToAuthnRequestTransformer(new OpenSamlXmlObjectFactory(), new AuthnContextFactory());
    }

    @Test
    void shouldApplyNameIdPolicy() {
        EidasAuthnRequestFromHub request = new EidasAuthnRequestFromHub(
                "theId",
                "theIssuer",
                Instant.now(),
                Collections.singletonList(AuthnContext.LEVEL_2),
                URI.create("theUri"),
                "theProviderName"
        );

        AuthnRequest authnRequest = transformer.apply(request);

        assertThat(authnRequest.getNameIDPolicy()).isNotNull();
        assertThat(authnRequest.getNameIDPolicy().getFormat()).isEqualTo(NameIDType.PERSISTENT);
        assertThat(authnRequest.getNameIDPolicy().getAllowCreate()).isTrue();
    }

    @Test
    void shouldCreateAnEidasAuthnRequest() {
        List<AuthnContext> authnContexts = Collections.singletonList(AuthnContext.LEVEL_2);
        EidasAuthnRequestFromHub originalRequestFromTransaction = anEidasAuthnRequestFromHub(A_PROVIDER, HTTP_ISSUER_ENTITY_ID_COM, authnContexts);

        AuthnRequest transformedRequest = transformer.apply(originalRequestFromTransaction);

        assertThat(transformedRequest.getProtocolBinding()).isEqualTo(SAMLConstants.SAML2_POST_BINDING_URI);
        assertThat(transformedRequest).isNotNull();
        assertThat(transformedRequest.getIssueInstant()).isNotNull();
        assertThat(transformedRequest.getDestination()).isEqualTo(EIDAS_SSO_LOCATION);
        assertThat(transformedRequest.getID()).isEqualTo(AUTHN_REQUEST_ID);

        assertThat(transformedRequest.getConsent()).isEqualTo(StatusResponseType.UNSPECIFIED_CONSENT);
        assertThat(transformedRequest.isForceAuthn()).isTrue();
        assertThat(transformedRequest.isPassive()).isFalse();
        assertThat(transformedRequest.getVersion()).isEqualTo(SAMLVersion.VERSION_20);

        assertThat(transformedRequest.getProviderName()).isEqualTo(A_PROVIDER);

        assertThat(transformedRequest.getIssuer().getValue()).isEqualTo(HTTP_ISSUER_ENTITY_ID_COM);

        NameIDPolicy nameIDPolicy = transformedRequest.getNameIDPolicy();
        assertThat(nameIDPolicy.getAllowCreate()).isTrue();
        assertThat(nameIDPolicy.getFormat()).isEqualTo(NameIDType.PERSISTENT);

        RequestedAuthnContext requestedAuthnContext = transformedRequest.getRequestedAuthnContext();
        assertThat(requestedAuthnContext.getComparison()).isEqualTo(AuthnContextComparisonTypeEnumeration.MINIMUM);
        AuthnContextClassRef authnContextClassRef = requestedAuthnContext.getAuthnContextClassRefs().get(0);
        assertThat(authnContextClassRef.getURI()).isEqualTo(LevelOfAssurance.SUBSTANTIAL.toString());
    }

    @Test
    void shouldGenerateAnEidasAuthnRequestExtensions() {
        List<AuthnContext> authnContexts = Collections.singletonList(AuthnContext.LEVEL_2);
        EidasAuthnRequestFromHub originalRequestFromTransaction = anEidasAuthnRequestFromHub(A_PROVIDER, HTTP_ISSUER_ENTITY_ID_COM, authnContexts);

        AuthnRequest transformedRequest = transformer.apply(originalRequestFromTransaction);
        Extensions extensions = transformedRequest.getExtensions();

        assertThat(extensions).isNotNull();
        Optional<XMLObject> spType = extensions
                .getUnknownXMLObjects(SPType.DEFAULT_ELEMENT_NAME)
                .stream().findFirst();
        assertThat(spType.isPresent()).isTrue().withFailMessage("There should be at least one eidas:SPType element");
        XMLObject xmlObject = spType.get();
        assertThat(xmlObject).isInstanceOf(SPTypeImpl.class).withFailMessage("Should be an instance of SPType");
        assertThat(((SPTypeImpl) xmlObject).getValue()).isEqualTo("public");

        Optional<XMLObject> requestedAttributes = extensions
                .getUnknownXMLObjects(RequestedAttributes.DEFAULT_ELEMENT_NAME)
                .stream().findFirst();

        assertThat(requestedAttributes.isPresent()).isTrue().withFailMessage("There should be at least one eidas:RequestedAttributes");

        List<XMLObject> requestedAttributeList = requestedAttributes.get().getOrderedChildren();
        assertThat(requestedAttributeList.size()).isGreaterThan(0).withFailMessage("There should be at least one eidas:RequestedAttribute");

        Map<String, RequestedAttributeImpl> reqAttrMap = getRequestedAttributesByFriendlyName(requestedAttributeList);

        RequestedAttributeImpl firstNameRequestedAttribute = reqAttrMap.get("FirstName");
        QName elementQName = firstNameRequestedAttribute.getElementQName();
        assertThat(elementQName.getLocalPart()).isEqualTo(RequestedAttribute.DEFAULT_ELEMENT_LOCAL_NAME);
        assertThat(elementQName.getNamespaceURI()).isEqualTo("http://eidas.europa.eu/saml-extensions");
        assertThat(elementQName.getPrefix()).isEqualTo("eidas");

        assertThat(firstNameRequestedAttribute).isNotNull();
        assertThat(EidasAuthnRequestFromHubToAuthnRequestTransformer.NATURAL_PERSON_NAME_PREFIX + "CurrentGivenName").isEqualTo(firstNameRequestedAttribute.getName());
        assertThat(firstNameRequestedAttribute.getNameFormat()).isEqualTo(Attribute.URI_REFERENCE);
        assertThat(firstNameRequestedAttribute.isRequired()).isTrue();

        assertThat(reqAttrMap.get("FirstName")).isNotNull();
        assertThat(reqAttrMap.get("FamilyName")).isNotNull();
        assertThat(reqAttrMap.get("DateOfBirth")).isNotNull();
        assertThat(reqAttrMap.get("PersonIdentifier")).isNotNull();
    }

    private EidasAuthnRequestFromHub anEidasAuthnRequestFromHub(String A_PROVIDER, String HTTP_ISSUER_ENTITY_ID_COM, List<AuthnContext> authnContexts) {
        return anEidasAuthnRequest()
                .withDestination(EIDAS_SSO_LOCATION)
                .withId(AUTHN_REQUEST_ID)
                .withProviderName(A_PROVIDER)
                .withIssuer(HTTP_ISSUER_ENTITY_ID_COM)
                .withLevelsOfAssurance(authnContexts)
                .buildFromHub();
    }

    private Map<String, RequestedAttributeImpl> getRequestedAttributesByFriendlyName(List<XMLObject> requestedAttributes) {
        return requestedAttributes.stream()
                .map(x -> (RequestedAttributeImpl)x)
                .collect(Collectors.toMap(AttributeImpl::getFriendlyName, x -> x));
    }
}
