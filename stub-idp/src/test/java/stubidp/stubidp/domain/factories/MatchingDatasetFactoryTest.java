package stubidp.stubidp.domain.factories;

import org.junit.jupiter.api.Test;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.utils.core.domain.Address;
import stubidp.saml.utils.core.domain.AddressFactory;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.core.domain.Gender;
import stubidp.saml.utils.core.domain.MatchingDataset;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.MatchingDatasetValue;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MatchingDatasetFactoryTest {

    private static final Address currentAddress = new AddressFactory().create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", Instant.now(), null, true);
    private static final Address previousAddress = new AddressFactory().create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), true);
    public static final DatabaseIdpUser completeUser = new DatabaseIdpUser(
            "idpuser-complete",
            UUID.randomUUID().toString(),
            "bar",
            asList(new MatchingDatasetValue<>("Jack", Instant.now(), Instant.now(), true), new MatchingDatasetValue<>("Spud", Instant.now(), Instant.now(), true)),
            asList(new MatchingDatasetValue<>("Cornelius", Instant.now(), Instant.now(), true), new MatchingDatasetValue<>("Aurelius", Instant.now(), Instant.now(), true)),
            asList(new MatchingDatasetValue<>("Bauer", Instant.now(), Instant.now(), true), new MatchingDatasetValue<>("Superman", Instant.now().atZone(ZoneId.of("UTC")).minusDays(5).toInstant(), Instant.now().atZone(ZoneId.of("UTC")).minusDays(3).toInstant(), true)),
            Optional.of(new MatchingDatasetValue<>(Gender.MALE, Instant.now(), Instant.now(), true)),
            asList(new MatchingDatasetValue<>(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1984-02-29"), Instant.now(), Instant.now(), true), new MatchingDatasetValue<>(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1984-03-01"), Instant.now(), Instant.now(), true)),
            asList(previousAddress, currentAddress),
            AuthnContext.LEVEL_2);

    @Test
    public void shouldSplitAddressesIntoCurrentAndPrevious() {
        final MatchingDataset matchingDataset = MatchingDatasetFactory.create(completeUser);
        assertThat(matchingDataset.getCurrentAddresses().size()).isEqualTo(1);
        assertThat(matchingDataset.getPreviousAddresses().size()).isEqualTo(1);
        assertThat(matchingDataset.getCurrentAddresses().get(0)).isEqualTo(currentAddress);
        assertThat(matchingDataset.getPreviousAddresses().get(0)).isEqualTo(previousAddress);
    }
}
