package stubidp.saml.hub.core.validators.assertion;

import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.hub.exception.SamlValidationException;
import stubidp.saml.hub.hub.validators.authnrequest.IdExpirationCache;

import javax.inject.Inject;
import java.time.Instant;

public class DuplicateAssertionValidatorImpl implements DuplicateAssertionValidator {

    private final IdExpirationCache<String> idExpirationCache;

    @Inject
    public DuplicateAssertionValidatorImpl(IdExpirationCache<String> idExpirationCache) {
        this.idExpirationCache = idExpirationCache;
    }

    @Override
    public void validateAuthnStatementAssertion(Assertion assertion) {
        if (!valid(assertion))
            throw new SamlValidationException(SamlTransformationErrorFactory.authnStatementAlreadyReceived(assertion.getID()));
    }

    @Override
    public void validateMatchingDataSetAssertion(Assertion assertion, String responseIssuerId) {
        if (!valid(assertion))
            throw new SamlValidationException(SamlTransformationErrorFactory.duplicateMatchingDataset(assertion.getID(), responseIssuerId));
    }

    private boolean valid(Assertion assertion) {
        if (isDuplicateNonExpired(assertion))
            return false;

        Instant expire = assertion.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData().getNotOnOrAfter();
        idExpirationCache.setExpiration(assertion.getID(), expire);
        return true;
    }

    private boolean isDuplicateNonExpired(Assertion assertion) {
        return idExpirationCache.contains(assertion.getID())
                && idExpirationCache.getExpiration(assertion.getID()).isAfter(Instant.now());
    }
}
