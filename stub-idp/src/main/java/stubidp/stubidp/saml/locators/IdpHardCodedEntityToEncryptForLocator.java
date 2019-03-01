package stubidp.stubidp.saml.locators;

import stubidp.saml.security.EntityToEncryptForLocator;

import javax.inject.Inject;
import javax.inject.Named;

import static stubidp.stubidp.StubIdpModule.HUB_ENTITY_ID;

public class IdpHardCodedEntityToEncryptForLocator implements EntityToEncryptForLocator {

    private final String hubEntityId;

    @Inject
    public IdpHardCodedEntityToEncryptForLocator(@Named(HUB_ENTITY_ID) String hubEntityId) {
        this.hubEntityId = hubEntityId;
    }

    @Override
    public String fromRequestId(String requestId) {
        return hubEntityId;
    }
}
