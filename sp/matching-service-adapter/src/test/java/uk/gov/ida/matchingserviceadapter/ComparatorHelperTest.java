package uk.gov.ida.matchingserviceadapter;

import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.assertions.TransliterableMdsValue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComparatorHelperTest {

    @Test
    public void comparatorByVerifiedThenCurrent() {
        List<TransliterableMdsValue> firstNames = new ArrayList<>();
        firstNames.add(buildFirstName("historical unverified: expected seventh", LocalDate.now(), false));
        firstNames.add(buildFirstName("current unverified: expected fifth", null, false));
        firstNames.add(buildFirstName("historical verified: expected third", LocalDate.now(), true));
        firstNames.add(buildFirstName("current verified: expected first", null, true));
        firstNames.add(buildFirstName("historical unverified: expected eighth", LocalDate.now(), false));
        firstNames.add(buildFirstName("current unverified: expected sixth", null, false));
        firstNames.add(buildFirstName("historical verified: expected fourth", LocalDate.now(), true));
        firstNames.add(buildFirstName("current verified: expected second", null, true));
        firstNames.sort(ComparatorHelper.comparatorByVerifiedThenCurrent());
        assertThat(firstNames.get(0).getValue()).isEqualTo("current verified: expected first");
        assertThat(firstNames.get(1).getValue()).isEqualTo("current verified: expected second");
        assertThat(firstNames.get(2).getValue()).isEqualTo("historical verified: expected third");
        assertThat(firstNames.get(3).getValue()).isEqualTo("historical verified: expected fourth");
        assertThat(firstNames.get(4).getValue()).isEqualTo("current unverified: expected fifth");
        assertThat(firstNames.get(5).getValue()).isEqualTo("current unverified: expected sixth");
        assertThat(firstNames.get(6).getValue()).isEqualTo("historical unverified: expected seventh");
        assertThat(firstNames.get(7).getValue()).isEqualTo("historical unverified: expected eighth");
    }

    @Test
    public void attributeComparatorByVerified() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(buildAddress("historical unverified: expected fifth", LocalDate.now(), false));
        addresses.add(buildAddress("current unverified: expected sixth", null, false));
        addresses.add(buildAddress("historical verified: expected first", LocalDate.now(), true));
        addresses.add(buildAddress("current verified: expected second", null, true));
        addresses.add(buildAddress("historical unverified: expected seventh", LocalDate.now(), false));
        addresses.add(buildAddress("current unverified: expected eighth", null, false));
        addresses.add(buildAddress("historical verified: expected third", LocalDate.now(), true));
        addresses.add(buildAddress("current verified: expected fourth", null, true));
        addresses.sort(ComparatorHelper.attributeComparatorByVerified());
        assertThat(addresses.get(0).getLines().get(0)).isEqualTo("historical verified: expected first");
        assertThat(addresses.get(1).getLines().get(0)).isEqualTo("current verified: expected second");
        assertThat(addresses.get(2).getLines().get(0)).isEqualTo("historical verified: expected third");
        assertThat(addresses.get(3).getLines().get(0)).isEqualTo("current verified: expected fourth");
        assertThat(addresses.get(4).getLines().get(0)).isEqualTo("historical unverified: expected fifth");
        assertThat(addresses.get(5).getLines().get(0)).isEqualTo("current unverified: expected sixth");
        assertThat(addresses.get(6).getLines().get(0)).isEqualTo("historical unverified: expected seventh");
        assertThat(addresses.get(7).getLines().get(0)).isEqualTo("current unverified: expected eighth");
    }

    private TransliterableMdsValue buildFirstName(String name, LocalDate to, boolean verified) {
        SimpleMdsValue<String> simpleValue = new SimpleMdsValue<>(name, null, to, verified);
        return new TransliterableMdsValue(simpleValue);
    }

    private Address buildAddress(String simpleLine, LocalDate to, boolean verified) {
        return new Address(Collections.singletonList(simpleLine), "post cod", null, null, null, to, verified);
    }
}
