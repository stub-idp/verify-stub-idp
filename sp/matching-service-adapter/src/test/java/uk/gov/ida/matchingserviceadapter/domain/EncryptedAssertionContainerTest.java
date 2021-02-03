package uk.gov.ida.matchingserviceadapter.domain;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class EncryptedAssertionContainerTest extends OpenSAMLRunner {

    @Test
    public void should_retrieveAssertionsFromAttributeQuery() {
        EncryptedAssertion encryptedAssertion = anAssertion().build();
        Assertion unencryptedAssertion = anAssertion().buildUnencrypted();
        AttributeQuery attributeQuery = anAttributeQuery().withSubject(aSubject().withSubjectConfirmation(
                aSubjectConfirmation().withSubjectConfirmationData(aSubjectConfirmationData()
                        .addAssertion(encryptedAssertion)
                        .addAssertion(unencryptedAssertion)
                        .build()).build()).build()).build();

        EncryptedAssertionContainer encryptedAssertionContainer = new EncryptedAssertionContainer(attributeQuery);

        assertThat(encryptedAssertionContainer.getEncryptedAssertions()).containsOnly(encryptedAssertion);
    }
}
