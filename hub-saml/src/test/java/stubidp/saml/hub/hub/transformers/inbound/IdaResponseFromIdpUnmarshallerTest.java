package stubidp.saml.hub.hub.transformers.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.hub.core.test.builders.PassthroughAssertionBuilder;
import stubidp.saml.hub.hub.domain.InboundResponseFromIdp;
import stubidp.saml.hub.core.OpenSAMLRunner;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.utils.core.domain.PassthroughAssertion;
import stubidp.saml.utils.core.test.builders.SignatureBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static stubidp.saml.utils.core.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.utils.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.utils.core.test.builders.AuthnStatementBuilder.anAuthnStatement;

@ExtendWith(MockitoExtension.class)
public class IdaResponseFromIdpUnmarshallerTest extends OpenSAMLRunner {

    @Mock
    private IdpIdaStatusUnmarshaller statusUnmarshaller;
    @Mock
    private PassthroughAssertionUnmarshaller passthroughAssertionUnmarshaller;
    @Mock
    private Response response;
    @Mock
    private Issuer issuer = null;
    private IdaResponseFromIdpUnmarshaller unmarshaller;
    private Signature signature = new SignatureBuilder().build();

    @BeforeEach
    public void setup() {
        when(response.getIssuer()).thenReturn(issuer);
        when(response.getDestination()).thenReturn("http://hello.com");
        when(response.getSignature()).thenReturn(signature);
        unmarshaller = new IdaResponseFromIdpUnmarshaller(statusUnmarshaller, passthroughAssertionUnmarshaller);
    }

    @Test
    public void transform_shouldTransformTheSamlResponseToIdaResponseByIdp() throws Exception {
        Assertion mdsAssertion = anAssertion().addAttributeStatement(anAttributeStatement().build()).buildUnencrypted();
        Assertion authnStatementAssertion = anAssertion().addAuthnStatement(anAuthnStatement().build()).buildUnencrypted();

        when(response.getAssertions()).thenReturn(List.of(mdsAssertion, authnStatementAssertion));
        PassthroughAssertion passthroughMdsAssertion = PassthroughAssertionBuilder.aPassthroughAssertion().buildMatchingDatasetAssertion();
        when(passthroughAssertionUnmarshaller.fromAssertion(mdsAssertion)).thenReturn(passthroughMdsAssertion);
        PassthroughAssertion passthroughAuthnAssertion = PassthroughAssertionBuilder.aPassthroughAssertion().buildAuthnStatementAssertion();
        when(passthroughAssertionUnmarshaller.fromAssertion(authnStatementAssertion)).thenReturn(passthroughAuthnAssertion);

        InboundResponseFromIdp inboundResponseFromIdp = unmarshaller.fromSaml(new ValidatedResponse(response), new ValidatedAssertions(response.getAssertions()));

        assertThat(inboundResponseFromIdp.getSignature().isPresent()).isEqualTo(true);
        assertThat(inboundResponseFromIdp.getMatchingDatasetAssertion().isPresent()).isEqualTo(true);
        assertThat(inboundResponseFromIdp.getAuthnStatementAssertion().isPresent()).isEqualTo(true);
        assertThat(inboundResponseFromIdp.getSignature().get()).isEqualTo(signature);
        assertThat(inboundResponseFromIdp.getAuthnStatementAssertion().get()).isEqualTo(passthroughAuthnAssertion);
        assertThat(inboundResponseFromIdp.getMatchingDatasetAssertion().get()).isEqualTo(passthroughMdsAssertion);
    }
}
