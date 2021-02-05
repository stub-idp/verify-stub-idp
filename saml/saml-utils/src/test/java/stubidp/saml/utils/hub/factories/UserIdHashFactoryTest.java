package stubidp.saml.utils.hub.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.PersistentId;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static stubidp.saml.test.builders.PersistentIdBuilder.aPersistentId;

@ExtendWith(MockitoExtension.class)
class UserIdHashFactoryTest {

    private static final String HASHING_ENTITY_ID = "entity";
    private static final UserIdHashFactory USER_ID_HASH_FACTORY = new UserIdHashFactory(HASHING_ENTITY_ID);

    @Test
    void shouldPerformHashing() {
        final PersistentId persistentId = aPersistentId().build();
        final String issuerId = "partner";

        final String hashedId = USER_ID_HASH_FACTORY.hashId(issuerId, persistentId.getNameId(), Optional.of(AuthnContext.LEVEL_2));

        assertThat(hashedId).isEqualTo("a5fbea969c3837a712cbe9e188804796828f369106478e623a436fa07e8fd298");
    }

    @Test
    void shouldGenerateADifferentHashForEveryLevelOfAssurance() {
        final PersistentId persistentId = aPersistentId().build();
        final String partnerEntityId = "partner";

        final long numberOfUniqueGeneratedHashedPids = Arrays.stream(AuthnContext.values())
                .map(authnContext -> USER_ID_HASH_FACTORY.hashId(partnerEntityId, persistentId.getNameId(), Optional.of(authnContext)))
                .distinct()
                .count();

        assertThat(numberOfUniqueGeneratedHashedPids).isEqualTo(5);
    }

    @Test
    void shouldThrowErrorWhenAuthnContextAbsent() {
        final UserIdHashFactory.AuthnContextMissingException e = assertThrows(UserIdHashFactory.AuthnContextMissingException.class,
                () -> USER_ID_HASH_FACTORY.hashId("", "pid", Optional.empty()));

        assertThat(e.getMessage()).isEqualTo(String.format("Authn context absent for persistent id %s", "pid"));
    }
}