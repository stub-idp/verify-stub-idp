package stubidp.saml.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SignatureFactoryTest extends OpenSAMLRunner {

    @Mock
    private IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever;

    @Mock
    private SignatureAlgorithm signatureAlgorithm;

    @Mock
    private DigestAlgorithm digestAlgorithm;

    @Test
    void shouldThrowExceptionWhenNoSigningCerts() {
        SignatureFactory signatureFactory = new SignatureFactory(true, idaKeyStoreCredentialRetriever, signatureAlgorithm, digestAlgorithm);

        when(idaKeyStoreCredentialRetriever.getSigningCredential()).thenReturn(null);
        when(idaKeyStoreCredentialRetriever.getSigningCertificate()).thenReturn(null);

        final Exception exception = Assertions.assertThrows(Exception.class, signatureFactory::createSignature);
        assertThat(exception.getMessage()).isEqualTo("Unable to generate key info without a signing certificate");
    }
}
