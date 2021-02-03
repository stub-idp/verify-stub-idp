package uk.gov.ida.integrationtest.interfacetests;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.saml.saml2.core.AttributeQuery;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.test.builders.AttributeQueryBuilder;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static stubidp.saml.test.builders.AddressAttributeBuilder_1_1.anAddressAttribute;
import static stubidp.saml.test.builders.AddressAttributeValueBuilder_1_1.anAddressAttributeValue;
import static stubidp.saml.test.builders.DateAttributeBuilder_1_1.aDate_1_1;
import static stubidp.saml.test.builders.DateAttributeValueBuilder.aDateValue;
import static stubidp.saml.test.builders.GenderAttributeBuilder_1_1.aGender_1_1;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static stubidp.saml.test.builders.PersonNameAttributeValueBuilder.aPersonNameValue;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aMatchingDatasetAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAuthnStatementAssertion;

@ExtendWith(DropwizardExtensionsSupport.class)
public class LegacyExampleSchemaTests extends BaseTestToolInterfaceTest {
    private static final String REQUEST_ID = "default-match-id";
    private static final String PID = "default-pid";

    public static final MatchingServiceAdapterAppExtension appRule = new MatchingServiceAdapterAppExtension(false, configRules);

    @Override
    protected MatchingServiceAdapterAppExtension getAppRule() { return appRule; }

    @Test
    public void shouldProduceLoA2SimpleCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(List.of(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(List.of(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(true)
                            .withFrom(LocalDate.of(2010, 1, 20))
                            .withTo(null)
                            .build())
                        .buildAsSurname(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(List.of("10 George Street"))
                            .withFrom(LocalDate.of(2005, 5, 14))
                            .withInternationalPostcode("GB1 2PF")
                            .withPostcode("GB1 2PF")
                            .withUprn("833F1187-9F33-A7E27B3F211E")
                            .withVerified(true)
                            .withTo(null)
                            .build())
                        .buildCurrentAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/LoA2-Minimum_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA1SimpleCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(List.of(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_1_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(List.of(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(true)
                            .withFrom(LocalDate.of(2015, 5, 14))
                            .withTo(null)
                            .build())
                        .buildAsSurname(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(List.of("10 George Street"))
                            .withFrom(LocalDate.of(2005, 5, 14))
                            .withInternationalPostcode("GB1 2PF")
                            .withPostcode("GB1 2PF")
                            .withUprn("833F1187-9F33-A7E27B3F211E")
                            .withVerified(true)
                            .withTo(null)
                            .build())
                        .buildCurrentAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/LoA1-Minimum_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA1ExtensiveCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(List.of(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_1_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(List.of(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Bob Rob")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsMiddlename(),
                    aPersonName_1_1()
                        .addValue(
                            aPersonNameValue()
                                .withValue("Fred")
                                .withVerified(false)
                                .withFrom(LocalDate.of(1980, 5, 24))
                                .withTo(LocalDate.of(1987, 1, 20))
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("Dou")
                                .withVerified(false)
                                .withFrom(getDateReplacement(yesterday))
                                .withTo(null)
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("John")
                                .withVerified(true)
                                .withFrom(LocalDate.of(2003, 5, 24))
                                .withTo(LocalDate.of(2004, 1, 20))
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("Joe")
                                .withVerified(true)
                                .withFrom(LocalDate.of(2005, 5, 24))
                                .withTo(getDateReplacement(inRange405to100))
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("Simon")
                                .withVerified(false)
                                .withFrom(getDateReplacement(inRange405to101))
                                .withTo(getDateReplacement(inRange405to200))
                                .build()
                        )
                        .buildAsSurname(),
                    aGender_1_1().withValue("Male").withVerified(false).withFrom(null).withTo(null).build(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(List.of("2323 George Street"))
                            .withFrom(getDateReplacement(yesterday))
                            .withInternationalPostcode("GB1 5PP")
                            .withPostcode("GB1 2PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .withTo(null)
                            .build())
                        .buildCurrentAddress(),
                    anAddressAttribute()
                        .addAddress(
                            anAddressAttributeValue()
                                .addLines(List.of("10 George Street"))
                                .withFrom(LocalDate.of(2005, 5, 14))
                                .withTo(LocalDate.of(2007, 5, 14))
                                .withPostcode("GB1 2PF")
                                .withInternationalPostcode("GB1 2PF")
                                .withUprn("833F1187-9F33-A7E27B3F211E")
                                .withVerified(true)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(List.of("344 George Street"))
                                .withFrom(LocalDate.of(2009, 5, 24))
                                .withTo(getDateReplacement(inRange405to100))
                                .withPostcode("GB1 2PP")
                                .withInternationalPostcode("GB1 2PP")
                                .withUprn("7D68E096-5510-B3844C0BA3FD")
                                .withVerified(true)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(List.of("67676 George Street"))
                                .withFrom(getDateReplacement(inRange405to101))
                                .withTo(getDateReplacement(inRange405to200))
                                .withPostcode("GB1 2PP")
                                .withInternationalPostcode("GB1 3PP")
                                .withUprn("7D68E096-5510-B3844C0BA3FD")
                                .withVerified(false)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(List.of("46244 George Street"))
                                .withFrom(LocalDate.of(1980, 5, 24))
                                .withTo(LocalDate.of(1987, 5, 24))
                                .withPostcode("GB1 2PP")
                                .withInternationalPostcode("GB1 3PP")
                                .withUprn("7D68E096-5510-B3844C0BA3FD")
                                .withVerified(false)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(List.of("Flat , Alberta Court", "36 Harrods Road", "New Berkshire", "Berkshire", "Cambria", "Europe"))
                                .withFrom(LocalDate.of(1987, 5, 24))
                                .withTo(LocalDate.of(1989, 5, 24))
                                .withPostcode(null)
                                .withInternationalPostcode(null)
                                .withUprn(null)
                                .withVerified(false)
                                .build()
                        ).buildPreviousAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/LoA1-Extended_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA2ExtensiveCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(List.of(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(List.of(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Bob Rob")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsMiddlename(),
                    aPersonName_1_1()
                        .addValue(
                            aPersonNameValue()
                                .withValue("Fred")
                                .withVerified(false)
                                .withFrom(LocalDate.of(1980, 5, 24))
                                .withTo(LocalDate.of(1987, 1, 20))
                                .build()
                        ).addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(false)
                            .withFrom(getDateReplacement(yesterday))
                            .withTo(null)
                            .build()
                    ).addValue(
                        aPersonNameValue()
                            .withValue("John")
                            .withVerified(true)
                            .withFrom(LocalDate.of(2003, 5, 24))
                            .withTo(LocalDate.of(2004, 1, 20))
                            .build()
                    ).addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .withFrom(LocalDate.of(2005, 5, 24))
                            .withTo(getDateReplacement(inRange180to100))
                            .build()
                    ).addValue(
                        aPersonNameValue()
                            .withValue("Simon")
                            .withVerified(false)
                            .withFrom(getDateReplacement(inRange180to101))
                            .withTo(getDateReplacement(inRange180to150))
                            .build()
                    )
                        .buildAsSurname(),
                    aGender_1_1().withValue("Male").withVerified(false).withFrom(null).withTo(null).build(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(List.of("2323 George Street"))
                            .withFrom(getDateReplacement(yesterday))
                            .withInternationalPostcode("GB1 5PP")
                            .withPostcode("GB1 2PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .withTo(null)
                            .build())
                        .buildCurrentAddress(),
                    anAddressAttribute()
                        .addAddress(
                            anAddressAttributeValue()
                                .addLines(List.of("10 George Street"))
                                .withFrom(LocalDate.of(2005, 5, 14))
                                .withTo(LocalDate.of(2007, 5, 14))
                                .withPostcode("GB1 2PF")
                                .withInternationalPostcode("GB1 2PF")
                                .withUprn("833F1187-9F33-A7E27B3F211E")
                                .withVerified(true)
                                .build()
                        ).addAddress(
                        anAddressAttributeValue()
                            .addLines(List.of("344 George Street"))
                            .withFrom(LocalDate.of(2009, 5, 24))
                            .withTo(getDateReplacement(inRange405to100))
                            .withPostcode("GB1 2PP")
                            .withInternationalPostcode("GB1 2PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(true)
                            .build()
                    ).addAddress(
                        anAddressAttributeValue()
                            .addLines(List.of("67676 George Street"))
                            .withFrom(getDateReplacement(inRange405to101))
                            .withTo(getDateReplacement(inRange405to200))
                            .withPostcode("GB1 2PP")
                            .withInternationalPostcode("GB1 3PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .build()
                    ).addAddress(
                        anAddressAttributeValue()
                            .addLines(List.of("56563 George Street"))
                            .withFrom(LocalDate.of(1980, 5, 24))
                            .withTo(LocalDate.of(1987, 5, 24))
                            .withPostcode("GB1 2PP")
                            .withInternationalPostcode("GB1 3PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .build()
                    ).addAddress(
                        anAddressAttributeValue()
                            .addLines(List.of("Flat , Alberta Court", "36 Harrods Road", "New Berkshire", "Berkshire", "Cambria", "Europe"))
                            .withFrom(LocalDate.of(1987, 5, 24))
                            .withTo(LocalDate.of(1989, 5, 24))
                            .withPostcode(null)
                            .withInternationalPostcode(null)
                            .withUprn(null)
                            .withVerified(false)
                            .build()
                    ).buildPreviousAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/LoA2-Extended_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceUserAccountCreationJson() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withAttributes(List.of())
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(List.of(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_1_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(List.of(), false, REQUEST_ID)
            ), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path filePath = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/user_account_creation.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, filePath, UNKNOWN_USER_URI);
    }

}
