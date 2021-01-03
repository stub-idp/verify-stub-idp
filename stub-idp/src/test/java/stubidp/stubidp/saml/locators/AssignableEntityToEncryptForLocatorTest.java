package stubidp.stubidp.saml.locators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AssignableEntityToEncryptForLocatorTest {

    @Test
    void shouldStoreEntityIdInMapAgainstRequestId() {
        AssignableEntityToEncryptForLocator assignableEntityToEncryptForLocator = new AssignableEntityToEncryptForLocator();
        String requestId = "requestId";
        String entityId = "entityId";
        
        assignableEntityToEncryptForLocator.addEntityIdForRequestId(requestId, entityId);
        
        assertThat(assignableEntityToEncryptForLocator.fromRequestId(requestId)).isEqualTo(entityId);
    }

    @Test
    void shouldRemoveEntityIdInMapAgainstRequestId() {
        AssignableEntityToEncryptForLocator assignableEntityToEncryptForLocator = new AssignableEntityToEncryptForLocator();
        String requestId = "requestId";
        String entityId = "entityId";

        assignableEntityToEncryptForLocator.addEntityIdForRequestId(requestId, entityId);

        assertThat(assignableEntityToEncryptForLocator.fromRequestId(requestId)).isEqualTo(entityId);

        assignableEntityToEncryptForLocator.removeEntityIdForRequestId(requestId);

        Assertions.assertThrows(IllegalStateException.class, () -> assignableEntityToEncryptForLocator.fromRequestId(requestId));
    }
}
