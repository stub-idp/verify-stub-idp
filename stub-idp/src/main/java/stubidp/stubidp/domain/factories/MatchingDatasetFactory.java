package stubidp.stubidp.domain.factories;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.assertions.TransliterableMdsValue;
import stubidp.stubidp.domain.DatabaseIdpUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MatchingDatasetFactory {

    private MatchingDatasetFactory() {
    }

    public static MatchingDataset create(final DatabaseIdpUser user) {
        return new MatchingDataset(
                fromTransliterable(user.getFirstnames()),
                from(user.getMiddleNames()),
                fromTransliterable(user.getSurnames()),
                user.getGender().map(MatchingDatasetFactory::from),
                from(user.getDateOfBirths()),
                getCurrentAddresses(user.getAddresses()),
                getPreviousAddresses(user.getAddresses()),
                user.getPersistentId()
        );
    }

    private static List<TransliterableMdsValue> fromTransliterable(List<SimpleMdsValue<String>> input) {
        return from(input).stream().map(TransliterableMdsValue::new).collect(Collectors.toList());
    }

    private static <T> List<SimpleMdsValue<T>> from(List<SimpleMdsValue<T>> input) {
        return input.stream().map(MatchingDatasetFactory::from).collect(Collectors.toList());
    }

    private static <T> SimpleMdsValue<T> from(SimpleMdsValue<T> input) {
        return new SimpleMdsValue<>(input.getValue(), input.getFrom(), input.getTo(), input.isVerified());
    }

    private static List<Address> getPreviousAddresses(List<Address> addresses) {
        List<Address> previousAddresses = new ArrayList<>();
        for (Address address : addresses) {
            if (Objects.nonNull(address.getTo())) {
                previousAddresses.add(address);
            }
        }
        return previousAddresses;
    }

    private static List<Address> getCurrentAddresses(List<Address> addresses) {
        List<Address> currentAddresses = new ArrayList<>();
        for (Address address : addresses) {
            if (Objects.isNull(address.getTo())) {
                currentAddresses.add(address);
            }
        }
        return currentAddresses;
    }
}
