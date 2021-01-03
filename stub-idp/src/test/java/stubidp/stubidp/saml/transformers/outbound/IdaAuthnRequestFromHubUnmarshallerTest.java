package stubidp.stubidp.saml.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.stubidp.saml.transformers.inbound.IdaAuthnRequestFromHubUnmarshaller;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration.EXACT;
import static stubidp.saml.domain.assertions.AuthnContext.LEVEL_1;
import static stubidp.saml.domain.assertions.AuthnContext.LEVEL_2;

@ExtendWith(MockitoExtension.class)
class IdaAuthnRequestFromHubUnmarshallerTest extends OpenSAMLRunner {

    @Mock
    private AuthnRequest authnRequest;
    @Mock
    private RequestedAuthnContext requestedAuthnContext;
    @Mock
    private AuthnContextClassRef authnContextClassRef;
    @Mock
    private Conditions conditions;
    @Mock
    private Issuer issuer;

    @BeforeEach
    void setupAuthnRequest() {
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getRequestedAuthnContext()).thenReturn(requestedAuthnContext);
        when(authnRequest.getConditions()).thenReturn(conditions);
        when(requestedAuthnContext.getAuthnContextClassRefs()).thenReturn(Arrays.asList(authnContextClassRef, authnContextClassRef));
        when(requestedAuthnContext.getComparison()).thenReturn(EXACT);
        when(authnContextClassRef.getURI()).thenReturn(IdaAuthnContext.LEVEL_2_AUTHN_CTX, IdaAuthnContext.LEVEL_1_AUTHN_CTX);
    }

    @Test
    void shouldMapLevelOfAssurance() {
        IdaAuthnRequestFromHubUnmarshaller unmarshaller = new IdaAuthnRequestFromHubUnmarshaller();

        IdaAuthnRequestFromHub outputIdaAuthnRequestFromHub = unmarshaller.fromSaml(authnRequest);

        assertThat(outputIdaAuthnRequestFromHub.getLevelsOfAssurance()).isEqualTo(Arrays.asList(LEVEL_1, LEVEL_2));
    }

    @Test
    void shouldKeepComparisonType() {
        IdaAuthnRequestFromHubUnmarshaller unmarshaller = new IdaAuthnRequestFromHubUnmarshaller();

        IdaAuthnRequestFromHub outputIdaAuthnRequestFromHub = unmarshaller.fromSaml(authnRequest);

        assertThat(outputIdaAuthnRequestFromHub.getComparisonType()).isEqualTo(EXACT);
    }
}
