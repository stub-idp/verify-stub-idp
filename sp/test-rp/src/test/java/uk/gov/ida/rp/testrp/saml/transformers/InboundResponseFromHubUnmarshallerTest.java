package uk.gov.ida.rp.testrp.saml.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import stubidp.saml.domain.assertions.PassthroughAssertion;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.saml.hub.transformers.inbound.PassthroughAssertionUnmarshaller;
import stubidp.saml.hub.transformers.inbound.TransactionIdaStatusUnmarshaller;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.test.OpenSAMLRunner;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.test.builders.PersistentIdBuilder.aPersistentId;
import static stubidp.saml.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static stubidp.saml.test.builders.ResponseBuilder.aResponse;
import static uk.gov.ida.rp.testrp.builders.PassthroughAssertionBuilder.aPassthroughAssertion;

@ExtendWith(MockitoExtension.class)
public class InboundResponseFromHubUnmarshallerTest extends OpenSAMLRunner {

    @Mock
    private TransactionIdaStatusUnmarshaller statusUnmarshaller;
    @Mock
    private PassthroughAssertionUnmarshaller assertionUnmarshaller;

    private InboundResponseFromHubUnmarshaller unmarshaller;

    @BeforeEach
    public void setup() {
        unmarshaller = new InboundResponseFromHubUnmarshaller(statusUnmarshaller, assertionUnmarshaller);
    }

    @Test
    public void transform_shouldTransformTheMatchingServiceAssertion() throws Exception {
        Assertion assertion = anAssertion().buildUnencrypted();
        Response originalResponse = aResponse()
                .addAssertion(assertion)
                .build();
        PassthroughAssertion transformedMatchingServiceAssertion =
                aPassthroughAssertion().withPersistentId(aPersistentId().withNameId("some-id").build()).buildMatchingServiceAssertion();
        TransactionIdaStatus transformedStatus = TransactionIdaStatus.Success;
        when(statusUnmarshaller.fromSaml(any(Status.class))).thenReturn(transformedStatus);
        when(assertionUnmarshaller.fromAssertion(any(Assertion.class)))
                .thenReturn(transformedMatchingServiceAssertion);

        InboundResponseFromHub transformedResponse = unmarshaller.fromSaml(new ValidatedResponse(originalResponse), new ValidatedAssertions(Collections.singletonList(assertion)));

        assertThat(transformedResponse.getStatus()).isEqualTo(transformedStatus);
        assertThat(transformedResponse.getPersistentId().get().getNameId()).isEqualTo("some-id");
        assertThat(transformedResponse.getAuthnContext()).isEqualTo(transformedMatchingServiceAssertion.getAuthnContext());
    }

    @Test
    public void transform_shouldPassThroughAttributeStatementsIfPresent() throws Exception {
        Attribute personNameAttribute = aPersonName_1_1().buildAsFirstname();
        Assertion assertionWithAttributes = anAssertion().addAttributeStatement(anAttributeStatement().addAttribute(personNameAttribute).build()).buildUnencrypted();
        Response originalResponse = aResponse().addAssertion(assertionWithAttributes).build();
        when(assertionUnmarshaller.fromAssertion(assertionWithAttributes)).thenReturn(aPassthroughAssertion().buildMatchingServiceAssertion());

        InboundResponseFromHub transformedResponse = unmarshaller.fromSaml(new ValidatedResponse(originalResponse), new ValidatedAssertions(Collections.singletonList(assertionWithAttributes)));

        assertThat(transformedResponse.getAttributes().get()).hasSize(1);
        assertThat(transformedResponse.getAttributes().get().get(0)).isEqualTo(personNameAttribute);
    }
}
