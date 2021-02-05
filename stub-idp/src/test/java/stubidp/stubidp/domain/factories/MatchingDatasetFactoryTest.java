package stubidp.stubidp.domain.factories;

import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.utils.core.domain.AddressFactory;
import stubidp.stubidp.domain.DatabaseIdpUser;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MatchingDatasetFactoryTest {

    private static final Address currentAddress = AddressFactory.create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", LocalDate.now(), null, true);
    private static final Address previousAddress = AddressFactory.create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", LocalDate.now(), LocalDate.now(), true);
    public static final DatabaseIdpUser completeUser = new DatabaseIdpUser(
            "idpuser-complete",
            UUID.randomUUID().toString(),
            "bar",
            asList(new SimpleMdsValue<>("Jack", LocalDate.now(), LocalDate.now(), true), new SimpleMdsValue<>("Spud", LocalDate.now(), LocalDate.now(), true)),
            asList(new SimpleMdsValue<>("Cornelius", LocalDate.now(), LocalDate.now(), true), new SimpleMdsValue<>("Aurelius", LocalDate.now(), LocalDate.now(), true)),
            asList(new SimpleMdsValue<>("Bauer", LocalDate.now(), LocalDate.now(), true), new SimpleMdsValue<>("Superman", LocalDate.now().minusDays(5), LocalDate.now().minusDays(3), true)),
            Optional.of(new SimpleMdsValue<>(Gender.MALE, LocalDate.now(), LocalDate.now(), true)),
            asList(new SimpleMdsValue<>(LocalDate.parse("1984-02-29"), LocalDate.now(), LocalDate.now(), true), new SimpleMdsValue<>(LocalDate.parse("1984-03-01"), LocalDate.now(), LocalDate.now(), true)),
            asList(previousAddress, currentAddress),
            AuthnContext.LEVEL_2);

    @Test
    void shouldSplitAddressesIntoCurrentAndPrevious() {
        final MatchingDataset matchingDataset = MatchingDatasetFactory.create(completeUser);
        assertThat(matchingDataset.getCurrentAddresses().size()).isEqualTo(1);
        assertThat(matchingDataset.getPreviousAddresses().size()).isEqualTo(1);
        assertThat(matchingDataset.getCurrentAddresses().get(0)).isEqualTo(currentAddress);
        assertThat(matchingDataset.getPreviousAddresses().get(0)).isEqualTo(previousAddress);
    }
}
