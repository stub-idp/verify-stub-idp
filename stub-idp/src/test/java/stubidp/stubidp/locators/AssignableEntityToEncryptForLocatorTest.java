package stubidp.stubidp.locators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stubidp.stubidp.saml.locators.AssignableEntityToEncryptForLocator;

import static org.assertj.core.api.Assertions.assertThat;

public class AssignableEntityToEncryptForLocatorTest {

    @Test
    public void shouldStoreEntityIdInMapAgainstRequestId() throws Exception {
        AssignableEntityToEncryptForLocator assignableEntityToEncryptForLocator = new AssignableEntityToEncryptForLocator();
        String requestId = "requestId";
        String entityId = "entityId";
        
        assignableEntityToEncryptForLocator.addEntityIdForRequestId(requestId, entityId);
        
        assertThat(assignableEntityToEncryptForLocator.fromRequestId(requestId)).isEqualTo(entityId);
    }

    @Test
    public void shouldRemoveEntityIdInMapAgainstRequestId() throws Exception {
        AssignableEntityToEncryptForLocator assignableEntityToEncryptForLocator = new AssignableEntityToEncryptForLocator();
        String requestId = "requestId";
        String entityId = "entityId";

        assignableEntityToEncryptForLocator.addEntityIdForRequestId(requestId, entityId);

        assertThat(assignableEntityToEncryptForLocator.fromRequestId(requestId)).isEqualTo(entityId);

        assignableEntityToEncryptForLocator.removeEntityIdForRequestId(requestId);

        Assertions.assertThrows(IllegalStateException.class, () -> assignableEntityToEncryptForLocator.fromRequestId(requestId));
    }
}
