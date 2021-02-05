package stubidp.stubidp.saml.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.FraudAuthnDetails;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.domain.assertions.IpAddress;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.assertions.TransliterableMdsValue;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.MatchingDatasetBuilder;
import stubidp.saml.test.builders.SimpleMdsValueBuilder;
import stubidp.saml.test.builders.TransliterableMdsValueBuilder;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.AddressFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.hub.factories.AttributeFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static stubidp.saml.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;
import static stubidp.saml.test.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;

@ExtendWith(MockitoExtension.class)
class IdentityProviderAssertionToAssertionTransformerTest extends OpenSAMLRunner {

    private IdentityProviderAssertionToAssertionTransformer transformer;
    @Mock
    private AttributeFactory attributeFactory;
    @Mock
    private IdentityProviderAuthnStatementToAuthnStatementTransformer identityProviderAuthnStatementToAuthnStatementTransformer;
    @Mock
    private OutboundAssertionToSubjectTransformer outboundAssertionToSubjectTransformer;

    private final Address currentAddress = AddressFactory.create(singletonList("subject-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", "1999-03-15", "2000-02-09", true);
    private final Address previousAddress = AddressFactory.create(singletonList("subject-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", "1999-03-15", "2000-02-09", true);
    private final TransliterableMdsValue previousSurname = TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-previousSurname").withVerifiedStatus(true).build();
    private final TransliterableMdsValue currentSurname = TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-currentSurname").withVerifiedStatus(true).build();

    @BeforeEach
    void setup() {
        OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        transformer = new IdentityProviderAssertionToAssertionTransformer(
                openSamlXmlObjectFactory,
                attributeFactory,
                identityProviderAuthnStatementToAuthnStatementTransformer,
                outboundAssertionToSubjectTransformer);
    }

    @Test
    void shouldTransformAssertionSubjects() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().build();

        transformer.transform(assertion);

        verify(outboundAssertionToSubjectTransformer).transform(assertion);
    }

    @Test
    void shouldTransformAssertionSubjectsFirstName() {
        TransliterableMdsValue firstname = TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("Bob").build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(
                MatchingDatasetBuilder.aMatchingDataset().addFirstname(firstname).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createFirstnameAttribute(getSimpleMdsValues(assertion.getMatchingDataset().get().getFirstNames()));
    }

    @Test
    void shouldHandleMissingAssertionSubjectsFirstname() {
        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset()
                .addMiddleNames(TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-middlename").withVerifiedStatus(true).build())
                .withSurnameHistory(asList(previousSurname, currentSurname))
                .withGender(SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).withVerifiedStatus(true).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aSimpleMdsValue().withValue(LocalDate.parse("2000-02-09")).withVerifiedStatus(true).build())
                .withCurrentAddresses(singletonList(currentAddress))
                .withPreviousAddresses(singletonList(previousAddress))
                .build();

        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createFirstnameAttribute(ArgumentMatchers.any());
    }

    @Test
    void shouldTransformAssertionSubjectsMiddleNames() {
        SimpleMdsValue<String> middleNames = SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("archibald ferdinand").build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().addMiddleNames(middleNames).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createMiddlenamesAttribute(assertion.getMatchingDataset().get().getMiddleNames());
    }

    @Test
    void shouldHandleMissingAssertionSubjectsMiddleNames() {
        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset()
                .addFirstname(TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-firstname").withVerifiedStatus(true).build())
                .withSurnameHistory(asList(previousSurname, currentSurname))
                .withGender(SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).withVerifiedStatus(true).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aSimpleMdsValue().withValue(LocalDate.parse("2000-02-09")).withVerifiedStatus(true).build())
                .withCurrentAddresses(singletonList(currentAddress))
                .withPreviousAddresses(singletonList(previousAddress))
                .build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createMiddlenamesAttribute(ArgumentMatchers.any());
    }

    @Test
    void shouldTransformAssertionSubjectsSurname() {
        TransliterableMdsValue surname = TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("Cratchit").build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().addSurname(surname).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createSurnameAttribute(getSimpleMdsValues(assertion.getMatchingDataset().get().getSurnames()));
    }

    @Test
    void shouldHandleMissingAssertionSubjectsSurname() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aFullyPopulatedMatchingDataset().withoutSurname().build()).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createSurnameAttribute(ArgumentMatchers.any());
    }

    @Test
    void shouldTransformAssertionSubjectsGender() {
        SimpleMdsValue<Gender> gender = SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().withGender(gender).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createGenderAttribute(gender);
    }

    @Test
    void shouldHandleMissingAssertionSubjectsGender() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aFullyPopulatedMatchingDataset().withGender(null).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createGenderAttribute(ArgumentMatchers.any());
    }

    @Test
    void shouldTransformAssertionSubjectsDateOfBirth() {
        SimpleMdsValue<LocalDate> dateOfBirth = SimpleMdsValueBuilder.<LocalDate>aSimpleMdsValue().withValue(LocalDate.parse("1986-12-05")).build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().addDateOfBirth(dateOfBirth).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createDateOfBirthAttribute(assertion.getMatchingDataset().get().getDateOfBirths());
    }

    @Test
    void shouldHandleMissingAssertionSubjectsDateOfBirth() {
        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset()
                .addFirstname(TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-firstname").withVerifiedStatus(true).build())
                .addMiddleNames(SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("subject-middlename").withVerifiedStatus(true).build())
                .withSurnameHistory(asList(previousSurname, currentSurname))
                .withGender(SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).withVerifiedStatus(true).build())
                .withCurrentAddresses(singletonList(currentAddress))
                .withPreviousAddresses(singletonList(previousAddress))
                .build();

        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createDateOfBirthAttribute(ArgumentMatchers.any());
    }

    @Test
    void shouldTransformAssertionSubjectsCurrentAddress() {
        List<Address> address = singletonList(AddressFactory.create(singletonList("221b Baker St."), "W4 1SH", "A 1", "4536789", "2007-09-28", "2007-10-29", true));
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().withCurrentAddresses(address).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createCurrentAddressesAttribute(address);
    }

    @Test
    void shouldHandleMissingAssertionSubjectsCurrentAddress() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aFullyPopulatedMatchingDataset().withCurrentAddresses(new ArrayList<>()).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createCurrentAddressesAttribute(ArgumentMatchers.anyList());
    }

    @Test
    void shouldTransformAssertionSubjectsPreviousAddresses() {
        Address previousAddressOne = AddressFactory.create(singletonList("221b Baker St."), "W4 1SH", null, null, "2007-09-27", "2007-09-28", true);
        Address previousAddressTwo = AddressFactory.create(singletonList("1 Goose Lane"), "M1 2FG", null, null, "2006-09-29", "2006-09-28", false);
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().withPreviousAddresses(asList(previousAddressOne, previousAddressTwo)).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createPreviousAddressesAttribute(asList(previousAddressOne, previousAddressTwo));
    }

    @Test
    void shouldHandleMissingAssertionSubjectsPreviousAddress() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aFullyPopulatedMatchingDataset().withPreviousAddresses(new ArrayList<>()).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createPreviousAddressesAttribute(ArgumentMatchers.anyList());
    }

    @Test
    void shouldTransformAssertionId() {
        String assertionId = "assertion-id";
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withId(assertionId).build();

        Assertion transformedAssertion = transformer.transform(assertion);

        assertThat(transformedAssertion.getID()).isEqualTo(assertionId);
    }

    @Test
    void shouldTransformAssertionIssuer() {
        String assertionIssuerId = "assertion issuer";
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withIssuer(assertionIssuerId).build();

        Assertion transformedAssertion = transformer.transform(assertion);

        assertThat(transformedAssertion.getIssuer().getValue()).isEqualTo(assertionIssuerId);
    }

    @Test
    void shouldTransformAssertionIssuerInstance() {
        Instant issueInstant = Instant.parse("2012-12-31T12:34:56Z");
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withIssueInstant(issueInstant).build();

        Assertion transformedAssertion = transformer.transform(assertion);

        assertThat(transformedAssertion.getIssueInstant()).isEqualTo(issueInstant);
    }

    @Test
    void shouldTransformLevelOfAssurance() {
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        IdentityProviderAuthnStatement authnStatement = anIdentityProviderAuthnStatement()
                .withAuthnContext(levelOfAssurance)
                .build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion()
                .withAuthnStatement(authnStatement)
                .build();

        transformer.transform(assertion);

        verify(identityProviderAuthnStatementToAuthnStatementTransformer).transform(authnStatement);
    }

    @Test
    void shouldTransformFraudDetailsEventId() {
        String reference = "reference";
        FraudAuthnDetails fraudAuthnDetails = new FraudAuthnDetails(reference, "IT01");
        IdentityProviderAssertion assertion = anIdentityProviderAssertion()
                .withAuthnStatement(anIdentityProviderAuthnStatement().withFraudDetails(fraudAuthnDetails).build())
                .build();

        transformer.transform(assertion);

        verify(attributeFactory).createIdpFraudEventIdAttribute(reference);
    }

    @Test
    void houldTransformFraudDetailsIndicatorIfPresent() {
        String indicator = "FI01";
        FraudAuthnDetails fraudAuthnDetails = new FraudAuthnDetails("ref", "FI01");
        IdentityProviderAssertion assertion = anIdentityProviderAssertion()
                .withAuthnStatement(anIdentityProviderAuthnStatement().withFraudDetails(fraudAuthnDetails).build())
                .build();

        transformer.transform(assertion);

        verify(attributeFactory).createGpg45StatusAttribute(indicator);
    }

    @Test
    void shouldTransformIpAddress() {
        String ipAddressValue = "9.9.8.8";
        IdentityProviderAssertion assertion = anIdentityProviderAssertion()
                .withAuthnStatement(anIdentityProviderAuthnStatement().withUserIpAddress(new IpAddress(ipAddressValue)).build())
                .build();
        final Attribute attribute = anIPAddress().withValue("4.5.6.7").build();
        when(attributeFactory.createUserIpAddressAttribute(ipAddressValue)).thenReturn(attribute);

        final Assertion transformedAssertion = transformer.transform(assertion);

        final Attribute ipAddressAttribute = transformedAssertion.getAttributeStatements().get(0).getAttributes().get(0);
        assertThat(ipAddressAttribute).isEqualTo(attribute);
    }

    private static List<SimpleMdsValue<String>> getSimpleMdsValues(final List<TransliterableMdsValue> transliterableMdsValues) {
        return transliterableMdsValues.stream().map(t -> (SimpleMdsValue<String>) t).collect(Collectors.toList());
    }
}
