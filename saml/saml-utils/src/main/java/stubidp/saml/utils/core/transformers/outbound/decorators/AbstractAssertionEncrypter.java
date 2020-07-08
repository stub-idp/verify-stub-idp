package stubidp.saml.utils.core.transformers.outbound.decorators;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EncryptionCredentialResolver;
import stubidp.saml.security.EntityToEncryptForLocator;

import java.util.List;

public abstract class AbstractAssertionEncrypter<T> {
    protected final EncryptionCredentialResolver credentialResolver;
    protected final EncrypterFactory encrypterFactory;
    protected final EntityToEncryptForLocator entityToEncryptForLocator;

    public AbstractAssertionEncrypter(
            final EncrypterFactory encrypterFactory,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final EncryptionCredentialResolver credentialResolver) {

        this.encrypterFactory = encrypterFactory;
        this.entityToEncryptForLocator = entityToEncryptForLocator;
        this.credentialResolver = credentialResolver;
    }

    public T encryptAssertions(T samlMessage) {
        if (getAssertions(samlMessage).size() > 0) {
            String entityToEncryptFor = entityToEncryptForLocator.fromRequestId(getRequestId(samlMessage));
            Credential credential = credentialResolver.getEncryptingCredential(entityToEncryptFor);

            Encrypter samlEncrypter = encrypterFactory.createEncrypter(credential);

            for (Assertion assertion : getAssertions(samlMessage)) {
                try {
                    EncryptedAssertion encryptedAssertion = samlEncrypter.encrypt(assertion);
                    getEncryptedAssertions(samlMessage).add(encryptedAssertion);
                } catch (EncryptionException e) {
                    throw new RuntimeException(e);
                }
            }
            getAssertions(samlMessage).removeAll(getAssertions(samlMessage));
        }
        return samlMessage;
    }

    protected abstract String getRequestId(final T response);

    protected abstract List<EncryptedAssertion> getEncryptedAssertions(T response);

    protected abstract List<Assertion> getAssertions(T response);
}
