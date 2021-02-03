package uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.assertions.TransliterableMdsValue;
import stubidp.saml.extensions.extensions.StringValueSamlObject;
import stubidp.saml.extensions.extensions.impl.VerifiedImpl;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;
import uk.gov.ida.matchingserviceadapter.saml.factories.UserAccountCreationAttributeFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceAssertionBuilder.aMatchingServiceAssertion;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.DATE_OF_BIRTH;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.MIDDLE_NAME_VERIFIED;

@ExtendWith(MockitoExtension.class)
public class AssertionServiceAssertionToAssertionTransformerTest {

    private MatchingServiceAssertionToAssertionTransformer transformer;
    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    @BeforeEach
    public void setup() {
        openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        transformer = new MatchingServiceAssertionToAssertionTransformer(
                openSamlXmlObjectFactory,
                new MatchingServiceAuthnStatementToAuthnStatementTransformer(openSamlXmlObjectFactory),
                new OutboundAssertionToSubjectTransformer(openSamlXmlObjectFactory));
    }

    @Test
    public void shouldTransformAssertionWithAuthnStatement() {
        MatchingServiceAssertion matchingServiceAssertion = aMatchingServiceAssertion().build();
        Assertion assertion = transformer.apply(matchingServiceAssertion);
        assertThat(assertion.getAuthnStatements()).hasSize(1);
        assertThat(assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getURI()).isEqualTo(matchingServiceAssertion.getAuthnStatement().getAuthnContext().getUri());
    }

    @Test
    public void shouldNotHaveAttributeStatementIfNoAttributesPresent() {
        MatchingServiceAssertion matchingServiceAssertion = aMatchingServiceAssertion()
                .withUserAttributesForAccountCreation(new ArrayList<Attribute>())
                .build();
        Assertion assertion = transformer.apply(matchingServiceAssertion);
        assertThat(assertion.getAttributeStatements()).isEmpty();
    }

    @Test
    public void shouldTransformAssertionsWithFirstNameAttribute() {
        String personName = "John";
        Attribute attribute = new UserAccountCreationAttributeFactory(openSamlXmlObjectFactory).createUserAccountCreationFirstNameAttribute(
                new TransliterableMdsValue(personName, null));

        Assertion assertion = transformAssertionWithAttribute(attribute);
        XMLObject firstAttributeValue = assertAssertionAndGetAttributeValue(FIRST_NAME, assertion);

        assertThat(((StringValueSamlObject) firstAttributeValue).getValue()).isEqualTo(personName);
    }

    @Test
    public void shouldTransformAssertionsWithMiddleNameVerifiedAttribute() {
        boolean verified = true;
        Attribute attribute = new UserAccountCreationAttributeFactory(openSamlXmlObjectFactory).createUserAccountCreationVerifiedAttribute(MIDDLE_NAME_VERIFIED, verified);

        Assertion assertion = transformAssertionWithAttribute(attribute);
        XMLObject firstAttributeValue = assertAssertionAndGetAttributeValue(MIDDLE_NAME_VERIFIED, assertion);

        assertThat(((VerifiedImpl) firstAttributeValue).getValue()).isEqualTo(verified);
    }

    @Test
    public void shouldTransformAssertionsWithDateOfBirthAttribute() {
        LocalDate dob = LocalDate.of(1980, 10, 30);
        Attribute attribute = new UserAccountCreationAttributeFactory(openSamlXmlObjectFactory).createUserAccountCreationDateOfBirthAttribute(
                new SimpleMdsValue<>(dob, null, null, false));

        Assertion assertion = transformAssertionWithAttribute(attribute);
        XMLObject firstAttributeValue = assertAssertionAndGetAttributeValue(DATE_OF_BIRTH, assertion);

        assertThat(((StringValueSamlObject) firstAttributeValue).getValue()).isEqualTo(dob.toString());
    }

    private XMLObject assertAssertionAndGetAttributeValue(final UserAccountCreationAttribute userAccountCreationAttribute, final Assertion assertion) {
        assertThat(assertion.getAttributeStatements()).hasSize(1);
        List<Attribute> attributes = assertion.getAttributeStatements().get(0).getAttributes();
        assertThat(attributes).hasSize(1);
        Attribute firstAttribute = attributes.get(0);
        assertThat(firstAttribute.getFriendlyName()).isEqualTo(userAccountCreationAttribute.getAttributeName());
        assertThat(firstAttribute.getAttributeValues()).hasSize(1);

        return firstAttribute.getAttributeValues().get(0);
    }

    private Assertion transformAssertionWithAttribute(final Attribute attribute) {
        MatchingServiceAssertion matchingServiceAssertion = aMatchingServiceAssertion()
                .withUserAttributesForAccountCreation(List.of(attribute))
                .build();
        return transformer.apply(matchingServiceAssertion);
    }

}
