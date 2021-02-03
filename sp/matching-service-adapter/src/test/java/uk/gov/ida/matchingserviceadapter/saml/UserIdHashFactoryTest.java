package uk.gov.ida.matchingserviceadapter.saml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.PersistentId;
import uk.gov.ida.matchingserviceadapter.exceptions.AuthnContextMissingException;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.ida.matchingserviceadapter.builders.PersistentIdBuilder.aPersistentId;

@ExtendWith(MockitoExtension.class)
public class UserIdHashFactoryTest {

    private static final String MSA_ENTITY_ID = "entity";

    private UserIdHashFactory userIdHashFactory = new UserIdHashFactory(MSA_ENTITY_ID);

    @Test
    public void createHashedId_shouldCallMessageDigest() {
        final PersistentId persistentId = aPersistentId().build();
        final String issuerId = "partner";

        final String hashedId = userIdHashFactory.hashId(issuerId, persistentId.getNameId(), Optional.of(AuthnContext.LEVEL_2));

        assertThat(hashedId).isEqualTo("a5fbea969c3837a712cbe9e188804796828f369106478e623a436fa07e8fd298");
    }

    @Test
    public void createHashedId_shouldGenerateADifferentHashForEveryAuthnContext(){
        final PersistentId persistentId = aPersistentId().build();
        final String partnerEntityId = "partner";

        final long numberOfUniqueGeneratedHashedPids = Arrays.stream(AuthnContext.values())
                .map(authnContext -> userIdHashFactory.hashId(partnerEntityId, persistentId.getNameId(), Optional.of(authnContext)))
                .distinct()
                .count();

        assertThat(numberOfUniqueGeneratedHashedPids).isEqualTo(5);
    }

    @Test
    public void shouldThrowIfAuthnContextAbsent() {
        assertThatExceptionOfType(AuthnContextMissingException.class)
                .isThrownBy(() -> userIdHashFactory.hashId("", "pid", Optional.empty()))
                .withMessage(String.format("Authn context absent for persistent id %s", "pid"));
    }
}
