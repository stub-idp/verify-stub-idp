package stubidp.stubidp.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.impl.ExtensionsBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.extensions.extensions.RequestedAttribute;
import stubidp.saml.extensions.extensions.SPType;
import stubidp.saml.extensions.extensions.impl.RequestedAttributeBuilder;
import stubidp.saml.extensions.extensions.impl.RequestedAttributesBuilder;
import stubidp.saml.extensions.extensions.impl.RequestedAttributesImpl;
import stubidp.saml.extensions.extensions.impl.SPTypeBuilder;
import stubidp.saml.hub.domain.LevelOfAssurance;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.AuthnContextClassRefBuilder;
import stubidp.saml.test.builders.AuthnRequestBuilder;
import stubidp.saml.test.builders.IssuerBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class EidasAuthnRequestTest extends OpenSAMLRunner {

    private AuthnRequest authnRequest;

    @BeforeEach
    public void setUp(){
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldConvertAuthnRequestToEidasAuthnRequest() {
        authnRequest = AuthnRequestBuilder.anAuthnRequest()
                .withIssuer(
                        IssuerBuilder.anIssuer().withIssuerId("issuer-id").build()
                )
                .withId("request-id")
                .withDestination("Destination")
                .build();

        RequestedAuthnContext requestedAuthnContext = new RequestedAuthnContextBuilder().buildObject();

        AuthnContextClassRef authnContextClassRef = AuthnContextClassRefBuilder.anAuthnContextClassRef()
                .withAuthnContextClasRefValue(LevelOfAssurance.SUBSTANTIAL.toString())
                .build();

        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        authnRequest.setRequestedAuthnContext(requestedAuthnContext);

        authnRequest.setExtensions(createEidasExtensions());

        EidasAuthnRequest actualEidasAuthnRequest = EidasAuthnRequest.buildFromAuthnRequest(authnRequest);

        assertThat(actualEidasAuthnRequest.getRequestId()).isEqualTo("request-id");
        assertThat(actualEidasAuthnRequest.getIssuer()).isEqualTo("issuer-id");
        assertThat(actualEidasAuthnRequest.getDestination()).isEqualTo("Destination");
        assertThat(actualEidasAuthnRequest.getRequestedLoa()).isEqualTo("http://eidas.europa.eu/LoA/substantial");

        assertThat(actualEidasAuthnRequest.getAttributes().size()).isEqualTo(1);
        stubidp.stubidp.domain.RequestedAttribute requestedAttribute = actualEidasAuthnRequest.getAttributes().get(0);
        assertThat(requestedAttribute.getName()).isEqualTo(IdaConstants.Eidas_Attributes.FamilyName.NAME);
        assertThat(requestedAttribute.isRequired()).isEqualTo(true);
    }

    private Extensions createEidasExtensions() {
        SPType spType = new SPTypeBuilder().buildObject();
        spType.setValue("public");

        RequestedAttributesImpl requestedAttributes = (RequestedAttributesImpl)new RequestedAttributesBuilder().buildObject();
        requestedAttributes.setRequestedAttributes(createRequestedAttribute(IdaConstants.Eidas_Attributes.FamilyName.NAME));

        Extensions extensions = new ExtensionsBuilder().buildObject();
        extensions.getUnknownXMLObjects().add(spType);
        extensions.getUnknownXMLObjects().add(requestedAttributes);
        return extensions;
    }

    private RequestedAttribute createRequestedAttribute(String requestedAttributeName) {
        RequestedAttribute attr = new RequestedAttributeBuilder().buildObject();
        attr.setName(requestedAttributeName);
        attr.setNameFormat(Attribute.URI_REFERENCE);
        attr.setIsRequired(true);
        return attr;
    }
}
