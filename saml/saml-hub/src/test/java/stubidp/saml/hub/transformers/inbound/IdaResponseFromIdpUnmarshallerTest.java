package stubidp.saml.hub.transformers.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.domain.assertions.PassthroughAssertion;
import stubidp.saml.domain.response.InboundResponseFromIdp;
import stubidp.saml.hub.core.test.builders.PassthroughAssertionBuilder;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.SignatureBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anAuthnStatement;

@ExtendWith(MockitoExtension.class)
class IdaResponseFromIdpUnmarshallerTest extends OpenSAMLRunner {

    @Mock
    private IdpIdaStatusUnmarshaller statusUnmarshaller;
    @Mock
    private PassthroughAssertionUnmarshaller passthroughAssertionUnmarshaller;
    @Mock
    private Response response;
    @Mock
    private Issuer issuer = null;
    private IdaResponseFromIdpUnmarshaller<PassthroughAssertionUnmarshaller, PassthroughAssertion> unmarshaller;
    private final Signature signature = SignatureBuilder.aSignature().build();

    @BeforeEach
    void setup() {
        when(response.getIssuer()).thenReturn(issuer);
        when(response.getDestination()).thenReturn("http://hello.local");
        when(response.getSignature()).thenReturn(signature);
        unmarshaller = new IdaResponseFromIdpUnmarshaller<>(statusUnmarshaller, passthroughAssertionUnmarshaller);
    }

    @Test
    void transform_shouldTransformTheSamlResponseToIdaResponseByIdp() {
        Assertion mdsAssertion = anAssertion().addAttributeStatement(anAttributeStatement().build()).buildUnencrypted();
        Assertion authnStatementAssertion = anAssertion().addAuthnStatement(anAuthnStatement().build()).buildUnencrypted();

        when(response.getAssertions()).thenReturn(List.of(mdsAssertion, authnStatementAssertion));
        PassthroughAssertion passthroughMdsAssertion = PassthroughAssertionBuilder.aPassthroughAssertion().buildMatchingDatasetAssertion();
        when(passthroughAssertionUnmarshaller.fromAssertion(mdsAssertion)).thenReturn(passthroughMdsAssertion);
        PassthroughAssertion passthroughAuthnAssertion = PassthroughAssertionBuilder.aPassthroughAssertion().buildAuthnStatementAssertion();
        when(passthroughAssertionUnmarshaller.fromAssertion(authnStatementAssertion)).thenReturn(passthroughAuthnAssertion);

        InboundResponseFromIdp<PassthroughAssertion> inboundResponseFromIdp = unmarshaller.fromSaml(new ValidatedResponse(response), new ValidatedAssertions(response.getAssertions()));

        assertThat(inboundResponseFromIdp.getSignature().isPresent()).isEqualTo(true);
        assertThat(inboundResponseFromIdp.getMatchingDatasetAssertion().isPresent()).isEqualTo(true);
        assertThat(inboundResponseFromIdp.getAuthnStatementAssertion().isPresent()).isEqualTo(true);
        assertThat(inboundResponseFromIdp.getSignature().get()).isEqualTo(signature);
        assertThat(inboundResponseFromIdp.getAuthnStatementAssertion().get()).isEqualTo(passthroughAuthnAssertion);
        assertThat(inboundResponseFromIdp.getMatchingDatasetAssertion().get()).isEqualTo(passthroughMdsAssertion);
    }
}
