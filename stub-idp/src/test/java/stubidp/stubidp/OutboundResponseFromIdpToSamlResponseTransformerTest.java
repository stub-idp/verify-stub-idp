package stubidp.stubidp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.stubidp.saml.transformers.outbound.IdentityProviderAssertionToAssertionTransformer;
import stubidp.stubidp.saml.transformers.outbound.IdentityProviderAuthnStatementToAuthnStatementTransformer;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.MatchingDatasetBuilder;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.hub.factories.AttributeFactory_1_1;
import stubidp.stubidp.domain.IdpIdaStatusMarshaller;
import stubidp.saml.domain.response.OutboundResponseFromIdp;
import stubidp.stubidp.saml.transformers.outbound.OutboundResponseFromIdpToSamlResponseTransformer;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;

public class OutboundResponseFromIdpToSamlResponseTransformerTest extends OpenSAMLRunner {

    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private OutboundResponseFromIdpToSamlResponseTransformer transformer;

    @BeforeEach
    public void setup() {
        IdpIdaStatusMarshaller statusTransformer = new IdpIdaStatusMarshaller(openSamlXmlObjectFactory);
        OutboundAssertionToSubjectTransformer outboundAssertionToSubjectTransformer = new OutboundAssertionToSubjectTransformer(openSamlXmlObjectFactory);
        IdentityProviderAssertionToAssertionTransformer assertionTransformer = new IdentityProviderAssertionToAssertionTransformer(
                openSamlXmlObjectFactory,
                new AttributeFactory_1_1(openSamlXmlObjectFactory),
                new IdentityProviderAuthnStatementToAuthnStatementTransformer(openSamlXmlObjectFactory),
                outboundAssertionToSubjectTransformer);
        transformer = new OutboundResponseFromIdpToSamlResponseTransformer(
                statusTransformer,
                openSamlXmlObjectFactory,
                assertionTransformer);
    }

    @Test
    public void transform_shouldTransformMatchingDataAssertion() throws Exception {
        Response response = openSamlXmlObjectFactory.createResponse();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().build()).build();
        OutboundResponseFromIdp originalResponse = OutboundResponseFromIdp.createSuccessResponseFromIdp(
                "response-id",
                "in-response-to",
                "issuer-id",
                assertion,
                null,
                null);

        transformer.transformAssertions(originalResponse, response);

        assertThat(response.getAssertions().size()).isEqualTo(1);
        assertThat(response.getAssertions().get(0).getAttributeStatements().size()).isEqualTo(1);
    }
}
