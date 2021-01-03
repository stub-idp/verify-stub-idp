package stubidp.saml.utils.core.transformers.outbound.decorators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.Credential;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.KeyStoreBackedEncryptionCredentialResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AssertionEncrypterTest {

    @Mock
    private KeyStoreBackedEncryptionCredentialResolver credentialFactory;
    @Mock
    private Credential credential;
    @Mock
    private EncrypterFactory encrypterFactory;
    @Mock
    private Encrypter encrypter;
    @Mock
    private Assertion assertion;
    @Mock
    private EncryptedAssertion expectedEncryptedAssertion;

    @Test
    void shouldEncryptAssertion() throws Exception {
        String entityId = "my-entity-id";
        when(credentialFactory.getEncryptingCredential(entityId)).thenReturn(credential);
        when(encrypterFactory.createEncrypter(credential)).thenReturn(encrypter);
        when(encrypter.encrypt(assertion)).thenReturn(expectedEncryptedAssertion);

        AssertionEncrypter assertionEncrypter = new AssertionEncrypter(encrypterFactory, credentialFactory);

        assertThat(assertionEncrypter.encrypt(assertion, entityId)).isEqualTo(expectedEncryptedAssertion);
    }

}
