package uk.gov.ida.matchingserviceadapter.domain;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Cycle3Dataset;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;
import stubidp.saml.extensions.extensions.impl.AddressImpl;
import stubidp.saml.extensions.extensions.impl.PersonNameImpl;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.MatchingDatasetBuilder;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.ADDRESS_HISTORY;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CYCLE_3;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME_VERIFIED;

public class UserAccountCreationAttributeExtractorTest extends OpenSAMLRunner {

    private final AttributeQueryAttributeFactory attributeQueryAttributeFactory1 = new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory());
    private final AttributeQueryAttributeFactory attributeQueryAttributeFactory = attributeQueryAttributeFactory1;
    private final UserAccountCreationAttributeExtractor userAccountCreationAttributeExtractor = new UserAccountCreationAttributeExtractor();

    @Test
    public void shouldReturnCurrentSurnamesWhenMatchingDatasetHasListOfSurnames() {
        List<Attribute> accountCreationAttributes = Stream.of(SURNAME)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        SimpleMdsValue<String> currentSurname = new SimpleMdsValue<>("CurrentSurname", null, null, true);
        SimpleMdsValue<String> oldSurname1 = new SimpleMdsValue<>("OldSurname1", LocalDate.of(2000, 1, 30), LocalDate.of(2010, 1, 30), true);
        SimpleMdsValue<String> oldSurname2 = new SimpleMdsValue<>("OldSurname2", LocalDate.of(1990, 1, 30), LocalDate.of(2000, 1, 29), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withSurnameHistory(Arrays.asList(oldSurname1, oldSurname2, currentSurname)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset).orElse(null), null);

        List<Attribute> surnames = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());
        PersonNameImpl personName = (PersonNameImpl) surnames.get(0).getAttributeValues().get(0);

        assertThat(surnames).hasSize(1);
        assertThat(personName.getValue()).isEqualTo("CurrentSurname");
    }

    @Test
    public void shouldReturnCurrentAddressWhenMatchingDatasetHasListOfAddresses() {
        List<Attribute> accountCreationAttributes = Stream.of(UserAccountCreationAttribute.CURRENT_ADDRESS)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());


        Address currentAddress = new Address(Arrays.asList("line1", "line2", "line3"), "postCode", "internationalPostCode", "uprn", null, null, true);
        Address oldAddress = new Address(Arrays.asList("old_line1", "old_line2", "old_line3"), "old_postCode", "old_internationalPostCode", "old_uprn", LocalDate.of(1990, 1, 30), LocalDate.of(2000, 1, 29), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withCurrentAddresses(singletonList(currentAddress)).withPreviousAddresses(Arrays.asList(oldAddress)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset).orElse(null), null);

        List<Attribute> addresses = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("currentaddress")).collect(toList());
        AddressImpl addressName = (AddressImpl) addresses.get(0).getAttributeValues().get(0);

        assertThat(addresses).hasSize(1);
        assertThat(addressName.getPostCode().getValue()).isEqualTo("postCode");
    }

    @Test
    public void shouldReturnCurrentSurnameWhetherItIsVerifiedOrNot() {
        List<Attribute> accountCreationAttributes = Stream.of(SURNAME)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        SimpleMdsValue<String> currentSurname = new SimpleMdsValue<>("CurrentSurname", null, null, false);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withSurnameHistory(singletonList(currentSurname)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset).orElse(null), null);

        List<Attribute> surnames = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());
        PersonNameImpl personName = (PersonNameImpl) surnames.get(0).getAttributeValues().get(0);

        assertThat(surnames).hasSize(1);
        assertThat(personName.getValue()).isEqualTo("CurrentSurname");
    }

    @Test
    public void shouldReturnCurrentAddressWhetherItIsVerifiedOrNot() {
        List<Attribute> accountCreationAttributes = Stream.of(UserAccountCreationAttribute.CURRENT_ADDRESS)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());


        Address currentAddress = new Address(Arrays.asList("line1", "line2", "line3"), "postCode", "internationalPostCode", "uprn", null, null, false);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withCurrentAddresses(singletonList(currentAddress)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset).orElse(null), null);

        List<Attribute> addresses = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("currentaddress")).collect(toList());
        AddressImpl addressName = (AddressImpl) addresses.get(0).getAttributeValues().get(0);

        assertThat(addresses).hasSize(1);
        assertThat(addressName.getPostCode().getValue()).isEqualTo("postCode");
    }

    @Test
    public void shouldNotReturnAttributesWhenNoCurrentValueExistsInMatchingDataSet() {
        List<Attribute> accountCreationAttributes = Stream.of(SURNAME)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        SimpleMdsValue<String> oldSurname1 = new SimpleMdsValue<>("OldSurname1", LocalDate.of(2000, 1, 30), LocalDate.of(2010, 1, 30), true);
        SimpleMdsValue<String> oldSurname2 = new SimpleMdsValue<>("OldSurname2", LocalDate.of(1990, 1, 30), LocalDate.of(2000, 1, 29), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withSurnameHistory(Arrays.asList(oldSurname1, oldSurname2)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset).orElse(null), null);

        List<Attribute> surnames = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());

        assertThat(surnames).isEmpty();
    }

    @Test
    public void shouldReturnVerifiedIfAllCurrentAttributeValuesAreVerified() {
        List<Attribute> accountCreationAttributes = Stream.of(SURNAME, SURNAME_VERIFIED)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());


        SimpleMdsValue<String> currentSurname = new SimpleMdsValue<>("CurrentSurname", null, null, true);
        SimpleMdsValue<String> oldSurname1 = new SimpleMdsValue<>("OldSurname1", LocalDate.of(2000, 1, 30), LocalDate.of(2010, 1, 30), true);
        SimpleMdsValue<String> oldSurname2 = new SimpleMdsValue<>("OldSurname2", LocalDate.of(1990, 1, 30), LocalDate.of(2000, 1, 29), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withSurnameHistory(Arrays.asList(oldSurname1, oldSurname2, currentSurname)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset).orElse(null), null);

        List<Attribute> surnames = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());
        List<Attribute>  verified = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname_verified")).collect(toList());
        PersonNameImpl personName = (PersonNameImpl) surnames.get(0).getAttributeValues().get(0);

        assertThat(surnames).hasSize(1);
        assertThat(personName.getValue()).isEqualTo("CurrentSurname");
        assertThat(verified).hasSize(1);
        assertThat(verified.get(0).getName()).isEqualTo("surname_verified");
    }

    @Test
    public void shouldReturnFullAddressHistoryIncludingWhetherTheyAreVerified() throws Exception {
        List<Attribute> accountCreationAttributes = Stream.of(ADDRESS_HISTORY)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        Address currentAddress = new Address(Arrays.asList("line1", "line2", "line3"), "postCode", "internationalPostCode", "uprn", null, null, true);
        Address oldAddress = new Address(Arrays.asList("old_line1", "old_line2", "old_line3"), "old_postCode", "old_internationalPostCode", "old_uprn", LocalDate.of(1990, 1, 30), LocalDate.of(2000, 1, 29), false);
        Address oldAddress2 = new Address(Arrays.asList("old_line1", "old_line2", "old_line3"), "old_postCode_2", "old_internationalPostCode_2", "old_uprn", LocalDate.of(2000, 1, 30), LocalDate.of(2010, 1, 29), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withCurrentAddresses(singletonList(currentAddress)).withPreviousAddresses(Arrays.asList(oldAddress, oldAddress2)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset).orElse(null), null);

        Attribute addressHistoryAttribute = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("addresshistory")).collect(toList()).get(0);
        List<AddressImpl> addresses = addressHistoryAttribute.getAttributeValues().stream().map(v -> (AddressImpl) v).collect(toList());

        AddressImpl firstAddress = addresses.stream().filter(a -> a.getPostCode().getValue().equals("postCode")).findFirst().get();
        AddressImpl secondAddress = addresses.stream().filter(a -> a.getPostCode().getValue().equals("old_postCode")).findFirst().get();
        AddressImpl thirdAddress = addresses.stream().filter(a -> a.getPostCode().getValue().equals("old_postCode_2")).findFirst().get();

        assertThat(addresses).hasSize(3);
        assertThat(firstAddress.getVerified()).isTrue();
        assertThat(secondAddress.getVerified()).isFalse();
        assertThat(thirdAddress.getVerified()).isTrue();
    }

    @Test
    public void shouldReturnRequiredCycle3AttributesWhenValuesExistInCycle3Dataset(){
        List<Attribute> accountCreationAttributes = Stream.of(CYCLE_3)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        Map<String, String> build = Map.of("cycle3Key", "cycle3Value");
        Cycle3Dataset cycle3Dataset = Cycle3Dataset.createFromData(build);

        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, mock(MatchingDataset.class), Optional.of(cycle3Dataset));

        List<Attribute> cycle_3 = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("cycle_3")).collect(toList());
        StringBasedMdsAttributeValue personName = (StringBasedMdsAttributeValue) cycle_3.get(0).getAttributeValues().get(0);

        assertThat(cycle_3).hasSize(1);
        assertThat(personName.getValue()).isEqualTo("cycle3Value");
    }

    @Test
    public void shouldReturnRequiredAttributesForAccountCreationWithoutAttributeWhichIsMissingInMatchingDataSet(){
        List<Attribute> accountCreationAttributes = Stream.of(SURNAME, FIRST_NAME)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        SimpleMdsValue<String> surname = new SimpleMdsValue<>("CurrentSurname", null, null, true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().addSurname(surname).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset).orElse(null), null);

        List<Attribute> attributes = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());

        assertThat(attributes).hasSize(1);
    }

    @Test
    public void willThrowExceptionIfWeHaveNoMatchingDataset(){
    //Added this test to highlight the dangerous use of .get on Optional.

        List<Attribute> accountCreationAttributes = Stream.of(FIRST_NAME)
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        assertThrows(IllegalArgumentException.class, () -> userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, null, null));
    }
}
