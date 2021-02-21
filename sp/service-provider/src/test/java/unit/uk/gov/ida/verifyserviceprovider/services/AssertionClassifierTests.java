package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.AssertionBuilder;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier.AssertionType;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.test.builders.AuthnContextBuilder.anAuthnContext;
import static stubidp.saml.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static stubidp.test.devpki.TestEntityIds.STUB_IDP_ONE;

public class AssertionClassifierTests extends OpenSAMLRunner {

    @Test
    public void shouldClassifyAnAssertionBasedOnWhetherItContainsAuthnStatements() {
        Assertion mdsAssertion = aMatchingDatasetAssertion("requestId").buildUnencrypted();
        Assertion authnStatementAssertion = anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted();

        AssertionClassifier assertionClassifier = new AssertionClassifier();

        assertThat(assertionClassifier.classifyAssertion(mdsAssertion)).isEqualTo(AssertionType.MDS_ASSERTION);
        assertThat(assertionClassifier.classifyAssertion(authnStatementAssertion)).isEqualTo(AssertionType.AUTHN_ASSERTION);
    }

    public static AssertionBuilder aMatchingDatasetAssertion(String requestId) {
        return anAssertion()
                .withId("mds-assertion")
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .addAttributeStatement(anAttributeStatement().build());
    }

    public static AssertionBuilder anAuthnStatementAssertion( String authnContext, String inResponseTo) {
        return anAssertion()
                .addAuthnStatement(
                        anAuthnStatement()
                                .withAuthnContext(
                                        anAuthnContext()
                                                .withAuthnContextClassRef(
                                                        anAuthnContextClassRef()
                                                                .withAuthnContextClasRefValue(authnContext)
                                                                .build())
                                                .build())
                                .build())
                .withSubject(
                        aSubject()
                                .withSubjectConfirmation(
                                        aSubjectConfirmation()
                                                .withSubjectConfirmationData(
                                                        aSubjectConfirmationData()
                                                                .withInResponseTo(inResponseTo)
                                                                .build()
                                                ).build()
                                ).build())
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build());
    }

}
