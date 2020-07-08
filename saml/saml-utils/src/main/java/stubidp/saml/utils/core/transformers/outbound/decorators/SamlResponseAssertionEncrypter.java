package stubidp.saml.utils.core.transformers.outbound.decorators;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EncryptionCredentialResolver;
import stubidp.saml.security.EntityToEncryptForLocator;

import javax.inject.Inject;
import java.util.List;

public class SamlResponseAssertionEncrypter extends AbstractAssertionEncrypter<Response> {

    @Inject
    public SamlResponseAssertionEncrypter(
            EncryptionCredentialResolver credentialResolver,
            EncrypterFactory encrypterFactory,
            EntityToEncryptForLocator entityToEncryptForLocator) {
        super(encrypterFactory, entityToEncryptForLocator, credentialResolver);

    }

    @Override
    protected String getRequestId(final Response response) {
        return response.getInResponseTo();
    }

    @Override
    protected List<EncryptedAssertion> getEncryptedAssertions(final Response response) {
        return response.getEncryptedAssertions();
    }

    @Override
    protected List<Assertion> getAssertions(final Response response) {
        return response.getAssertions();
    }
}
