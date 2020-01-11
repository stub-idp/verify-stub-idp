package stubsp.stubsp.saml.locators;

import stubidp.saml.security.EntityToEncryptForLocator;

public class StubIdpEntityToEncryptForLocator implements EntityToEncryptForLocator {

    private final String expectedEntityId;

    public StubIdpEntityToEncryptForLocator(String expectedEntityId) {
        this.expectedEntityId = expectedEntityId;
    }

    @Override
    public String fromRequestId(String requestId) {
        return expectedEntityId;
    }
}
