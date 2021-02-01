package uk.gov.ida.rp.testrp.saml.locators;

import uk.gov.ida.saml.security.EntityToEncryptForLocator;

import javax.inject.Inject;
import javax.inject.Named;

public class TransactionHardCodedEntityToEncryptForLocator implements EntityToEncryptForLocator {

    private final String hubEntityId;

    @Inject
    public TransactionHardCodedEntityToEncryptForLocator(@Named("HubEntityId") String hubEntityId) {
        this.hubEntityId = hubEntityId;
    }

    @Override
    public String fromRequestId(String requestId) {
        return hubEntityId;
    }
}
