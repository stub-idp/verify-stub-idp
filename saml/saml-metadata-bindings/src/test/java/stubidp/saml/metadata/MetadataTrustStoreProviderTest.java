package stubidp.saml.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.metadata.exception.EmptyTrustStoreException;
import stubidp.saml.metadata.factories.MetadataTrustStoreProvider;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.utils.keystore.KeyStoreRule;
import stubidp.test.utils.keystore.builders.KeyStoreRuleBuilder;

import java.security.KeyStore;
import java.security.KeyStoreException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class MetadataTrustStoreProviderTest {

    @Mock
    KeyStoreLoader keyStoreLoader;

    @RegisterExtension
    public static KeyStoreRule emptyKeyStoreRule = KeyStoreRuleBuilder.aKeyStoreRule().build();

    @RegisterExtension
    public static KeyStoreRule keyStoreRule = KeyStoreRuleBuilder.aKeyStoreRule().withCertificate("hub", TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT).build();

    private String filePath ="file path";
    private final String password = "password";

    @Test
    public void shouldThrowExceptionIfTrustStoreContainsNoCertificates() throws KeyStoreException {
        Mockito.when(keyStoreLoader.load(filePath,password)).thenReturn(emptyKeyStoreRule.getKeyStore());
        MetadataTrustStoreProvider metadataTrustStoreProvider = new MetadataTrustStoreProvider(keyStoreLoader, filePath, password);

        Assertions.assertThrows(EmptyTrustStoreException.class, () -> metadataTrustStoreProvider.get());
    }

    @Test
    public void shouldPropagateExceptionIfKeystoreIsUninitialized() throws KeyStoreException {
        Mockito.when(keyStoreLoader.load(filePath,password)).thenReturn(KeyStore.getInstance(KeyStore.getDefaultType()));
        MetadataTrustStoreProvider metadataTrustStoreProvider = new MetadataTrustStoreProvider(keyStoreLoader, filePath, password);

        Assertions.assertThrows(RuntimeException.class, () -> metadataTrustStoreProvider.get());
    }

    @Test
    public void shouldReturnTrustStoreContainingCertificates() throws KeyStoreException {
        Mockito.when(keyStoreLoader.load(filePath, password)).thenReturn(keyStoreRule.getKeyStore());
        MetadataTrustStoreProvider metadataTrustStoreProvider = new MetadataTrustStoreProvider(keyStoreLoader, filePath, password);

        assertThat(metadataTrustStoreProvider.get().containsAlias("hub")).isTrue();
    }
}
