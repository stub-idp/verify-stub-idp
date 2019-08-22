package stubidp.saml.hub.hub.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.hub.core.test.builders.PassthroughAssertionBuilder;
import stubidp.saml.hub.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.PassthroughAssertion;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static stubidp.saml.utils.core.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.utils.core.test.builders.ResponseBuilder.aResponse;
import static stubidp.saml.utils.core.test.builders.ResponseForHubBuilder.anAuthnResponse;

@ExtendWith(MockitoExtension.class)
public class OutboundResponseFromHubToSamlResponseTransformerTest extends OpenSAMLRunner {

    @Mock
    private TransactionIdaStatusMarshaller statusMarshaller = null;
    @Mock
    private EncryptedAssertionUnmarshaller encryptedAssertionUnmarshaller;

    private OutboundResponseFromHubToSamlResponseTransformer transformer;

    @BeforeEach
    public void setup() {
        OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        transformer = new OutboundResponseFromHubToSamlResponseTransformer(statusMarshaller, openSamlXmlObjectFactory, encryptedAssertionUnmarshaller);
    }

    @Test
    public void transformAssertions_shouldTransformMatchingServiceAssertions() throws Exception {
        PassthroughAssertion matchingServiceAssertion = PassthroughAssertionBuilder.aPassthroughAssertion().buildMatchingServiceAssertion();
        Response transformedResponse = aResponse().withNoDefaultAssertion().build();
        EncryptedAssertion transformedMatchingDatasetAssertion = anAssertion().build();
        when(encryptedAssertionUnmarshaller.transform(matchingServiceAssertion.getUnderlyingAssertionBlob())).thenReturn(transformedMatchingDatasetAssertion);

        String encryptedMatchingServiceAssertion = matchingServiceAssertion.getUnderlyingAssertionBlob();
        transformer.transformAssertions(anAuthnResponse().withEncryptedAssertions(Arrays.asList(encryptedMatchingServiceAssertion)).buildOutboundResponseFromHub(), transformedResponse);

        assertThat(transformedResponse.getEncryptedAssertions().size()).isEqualTo(1);
        assertThat(transformedResponse.getEncryptedAssertions().get(0)).isEqualTo(transformedMatchingDatasetAssertion);
    }
}
