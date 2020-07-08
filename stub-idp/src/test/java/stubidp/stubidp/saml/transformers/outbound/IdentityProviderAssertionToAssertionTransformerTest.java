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
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.MatchingDatasetBuilder;
import stubidp.saml.test.builders.SimpleMdsValueBuilder;
import stubidp.saml.test.builders.TransliterableMdsValueBuilder;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.AddressFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.hub.factories.AttributeFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;
import static stubidp.saml.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static stubidp.saml.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;

@ExtendWith(MockitoExtension.class)
public class IdentityProviderAssertionToAssertionTransformerTest extends OpenSAMLRunner {

    private IdentityProviderAssertionToAssertionTransformer transformer;
    @Mock
    private AttributeFactory attributeFactory;
    @Mock
    private IdentityProviderAuthnStatementToAuthnStatementTransformer identityProviderAuthnStatementToAuthnStatementTransformer;
    @Mock
    private OutboundAssertionToSubjectTransformer outboundAssertionToSubjectTransformer;

    private final Address currentAddress = new AddressFactory().create(singletonList("subject-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", "1999-03-15", "2000-02-09", true);
    private final Address previousAddress = new AddressFactory().create(singletonList("subject-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", "1999-03-15", "2000-02-09", true);
    private final TransliterableMdsValue previousSurname = TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-previousSurname").withVerifiedStatus(true).build();
    private final TransliterableMdsValue currentSurname = TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-currentSurname").withVerifiedStatus(true).build();

    @BeforeEach
    public void setup() {
        OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        transformer = new IdentityProviderAssertionToAssertionTransformer(
                openSamlXmlObjectFactory,
                attributeFactory,
                identityProviderAuthnStatementToAuthnStatementTransformer,
                outboundAssertionToSubjectTransformer);
    }

    @Test
    public void shouldTransformAssertionSubjects() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().build();

        transformer.transform(assertion);

        verify(outboundAssertionToSubjectTransformer).transform(assertion);
    }

    @Test
    public void shouldTransformAssertionSubjectsFirstName() {
        TransliterableMdsValue firstname = TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("Bob").build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(
                MatchingDatasetBuilder.aMatchingDataset().addFirstname(firstname).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createFirstnameAttribute(getSimpleMdsValues(assertion.getMatchingDataset().get().getFirstNames()));
    }

    @Test
    public void shouldHandleMissingAssertionSubjectsFirstname() {
        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset()
                .addMiddleNames(TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-middlename").withVerifiedStatus(true).build())
                .withSurnameHistory(asList(previousSurname, currentSurname))
                .withGender(SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).withVerifiedStatus(true).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<Instant>aSimpleMdsValue().withValue(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-02-09")).withVerifiedStatus(true).build())
                .withCurrentAddresses(singletonList(currentAddress))
                .withPreviousAddresses(singletonList(previousAddress))
                .build();

        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createFirstnameAttribute(ArgumentMatchers.any());
    }

    @Test
    public void shouldTransformAssertionSubjectsMiddleNames() {
        SimpleMdsValue<String> middleNames = SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("archibald ferdinand").build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().addMiddleNames(middleNames).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createMiddlenamesAttribute(assertion.getMatchingDataset().get().getMiddleNames());
    }

    @Test
    public void shouldHandleMissingAssertionSubjectsMiddleNames() {
        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset()
                .addFirstname(TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-firstname").withVerifiedStatus(true).build())
                .withSurnameHistory(asList(previousSurname, currentSurname))
                .withGender(SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).withVerifiedStatus(true).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<Instant>aSimpleMdsValue().withValue(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-02-09")).withVerifiedStatus(true).build())
                .withCurrentAddresses(singletonList(currentAddress))
                .withPreviousAddresses(singletonList(previousAddress))
                .build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createMiddlenamesAttribute(ArgumentMatchers.any());
    }

    @Test
    public void shouldTransformAssertionSubjectsSurname() {
        TransliterableMdsValue surname = TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("Cratchit").build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().addSurname(surname).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createSurnameAttribute(getSimpleMdsValues(assertion.getMatchingDataset().get().getSurnames()));
    }

    @Test
    public void shouldHandleMissingAssertionSubjectsSurname() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aFullyPopulatedMatchingDataset().withoutSurname().build()).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createSurnameAttribute(ArgumentMatchers.any());
    }

    @Test
    public void shouldTransformAssertionSubjectsGender() {
        SimpleMdsValue<Gender> gender = SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().withGender(gender).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createGenderAttribute(gender);
    }

    @Test
    public void shouldHandleMissingAssertionSubjectsGender() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aFullyPopulatedMatchingDataset().withGender(null).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createGenderAttribute(ArgumentMatchers.any());
    }

    @Test
    public void shouldTransformAssertionSubjectsDateOfBirth() {
        SimpleMdsValue<Instant> dateOfBirth = SimpleMdsValueBuilder.<Instant>aSimpleMdsValue().withValue(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1986-12-05")).build();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().addDateOfBirth(dateOfBirth).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createDateOfBirthAttribute(assertion.getMatchingDataset().get().getDateOfBirths());
    }

    @Test
    public void shouldHandleMissingAssertionSubjectsDateOfBirth() {
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
    public void shouldTransformAssertionSubjectsCurrentAddress() {
        List<Address> address = singletonList(new AddressFactory().create(singletonList("221b Baker St."), "W4 1SH", "A 1", "4536789", "2007-09-28", "2007-10-29", true));
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().withCurrentAddresses(address).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createCurrentAddressesAttribute(address);
    }

    @Test
    public void shouldHandleMissingAssertionSubjectsCurrentAddress() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aFullyPopulatedMatchingDataset().withCurrentAddresses(new ArrayList<>()).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createCurrentAddressesAttribute(ArgumentMatchers.<Address>anyList());
    }

    @Test
    public void shouldTransformAssertionSubjectsPreviousAddresses() {
        Address previousAddressOne = new AddressFactory().create(singletonList("221b Baker St."), "W4 1SH", null, null, "2007-09-27", "2007-09-28", true);
        Address previousAddressTwo = new AddressFactory().create(singletonList("1 Goose Lane"), "M1 2FG", null, null, "2006-09-29", "2006-09-28", false);
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().withPreviousAddresses(asList(previousAddressOne, previousAddressTwo)).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory).createPreviousAddressesAttribute(asList(previousAddressOne, previousAddressTwo));
    }

    @Test
    public void shouldHandleMissingAssertionSubjectsPreviousAddress() {
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aFullyPopulatedMatchingDataset().withPreviousAddresses(new ArrayList<>()).build()).build();

        transformer.transform(assertion);

        verify(attributeFactory, never()).createPreviousAddressesAttribute(ArgumentMatchers.<Address>anyList());
    }

    @Test
    public void shouldTransformAssertionId() {
        String assertionId = "assertion-id";
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withId(assertionId).build();

        Assertion transformedAssertion = transformer.transform(assertion);

        assertThat(transformedAssertion.getID()).isEqualTo(assertionId);
    }

    @Test
    public void shouldTransformAssertionIssuer() {
        String assertionIssuerId = "assertion issuer";
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withIssuer(assertionIssuerId).build();

        Assertion transformedAssertion = transformer.transform(assertion);

        assertThat(transformedAssertion.getIssuer().getValue()).isEqualTo(assertionIssuerId);
    }

    @Test
    public void shouldTransformAssertionIssuerInstance() {
        Instant issueInstant = Instant.parse("2012-12-31T12:34:56Z");
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withIssueInstant(issueInstant).build();

        Assertion transformedAssertion = transformer.transform(assertion);

        assertThat(transformedAssertion.getIssueInstant()).isEqualTo(issueInstant);
    }

    @Test
    public void shouldTransformLevelOfAssurance() {
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
    public void shouldTransformFraudDetailsEventId() {
        String reference = "reference";
        FraudAuthnDetails fraudAuthnDetails = new FraudAuthnDetails(reference, "IT01");
        IdentityProviderAssertion assertion = anIdentityProviderAssertion()
                .withAuthnStatement(anIdentityProviderAuthnStatement().withFraudDetails(fraudAuthnDetails).build())
                .build();

        transformer.transform(assertion);

        verify(attributeFactory).createIdpFraudEventIdAttribute(reference);
    }

    @Test
    public void houldTransformFraudDetailsIndicatorIfPresent() {
        String indicator = "FI01";
        FraudAuthnDetails fraudAuthnDetails = new FraudAuthnDetails("ref", "FI01");
        IdentityProviderAssertion assertion = anIdentityProviderAssertion()
                .withAuthnStatement(anIdentityProviderAuthnStatement().withFraudDetails(fraudAuthnDetails).build())
                .build();

        transformer.transform(assertion);

        verify(attributeFactory).createGpg45StatusAttribute(indicator);
    }

    @Test
    public void shouldTransformIpAddress() {
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
