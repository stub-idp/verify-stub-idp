package stubidp.stubidp.builders;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import stubidp.saml.extensions.IdaSamlBootstrap;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EidasResponseBuilderTest {

    @BeforeAll
    public static void setUpClass() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldCreateEidasResponseWithRequiredFields() {
        List<Attribute> attributes = Collections.emptyList();
        Instant issueInstant = Instant.now().minusSeconds(2);
        Instant assertionIssueInstant = Instant.now().minusSeconds(1);
        Instant authnStatementIssueInstant = Instant.now();

        Response response = EidasResponseBuilder.createEidasResponse("responseIssuerId", "statusCodeValue",
                "pid", "loa", attributes, "inResponseTo", issueInstant, assertionIssueInstant, authnStatementIssueInstant, "destinationUrl", "connectorNodeIssuerId");
        Assertion assertion = response.getAssertions().get(0);
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);

        assertThat(assertion.getAttributeStatements().get(0).getAttributes()).isEqualTo(attributes);
        assertThat(authnStatement.getAuthnContext().getAuthnContextClassRef().getURI()).isEqualTo("loa");
        assertThat(authnStatement.getAuthnInstant()).isEqualTo(authnStatementIssueInstant);
        assertThat(assertion.getSubject().getNameID().getValue()).isEqualTo("UK/EU/pid");
        assertThat(assertion.getSubject().getNameID().getFormat()).isEqualTo(NameIDType.PERSISTENT);
        assertThat(assertion.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData().getInResponseTo()).isEqualTo("inResponseTo");
        assertThat(assertion.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData().getRecipient()).isEqualTo("destinationUrl");
        assertThat(assertion.getSubject().getSubjectConfirmations().get(0).getMethod()).isEqualTo(SubjectConfirmation.METHOD_BEARER);
        assertThat(assertion.getIssuer().getValue()).isEqualTo("responseIssuerId");
        assertThat(assertion.getConditions().getNotBefore()).isBeforeOrEqualTo(Instant.now());
        assertThat(assertion.getConditions().getNotOnOrAfter()).isBeforeOrEqualTo(Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(5).toInstant());
        assertThat(assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).getURI()).isEqualTo("connectorNodeIssuerId");
        assertThat(response.getIssuer().getValue()).isEqualTo("responseIssuerId");
        assertThat(response.getID()).isNotBlank();
        assertThat(response.getInResponseTo()).isEqualTo("inResponseTo");
        assertThat(response.getDestination()).isEqualTo("destinationUrl");
        assertThat(response.getIssueInstant()).isEqualTo(issueInstant);
        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo("statusCodeValue");
    }
}