package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.support.EncrypterFactory;
import stubidp.test.devpki.TestCertificateStrings;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public class EncryptedAssertionBuilder {
    private static final TestCredentialFactory credentialFactory = new TestCredentialFactory(TestCertificateStrings.TEST_PUBLIC_CERT, null);

    private String id = "some-assertion-id";
    private Optional<Credential> credential = ofNullable(credentialFactory.getEncryptingCredential());

    private EncryptedAssertionBuilder() {}

    public static EncryptedAssertionBuilder anEncryptedAssertionBuilder() {
        return new EncryptedAssertionBuilder();
    }

    public EncryptedAssertion build() {
        Assertion assertion = AssertionBuilder.anAssertion().withId(id).buildUnencrypted();
        Encrypter encrypter = new EncrypterFactory().createEncrypter(credential.get());
        try {
            return encrypter.encrypt(assertion);
        } catch (EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    public EncryptedAssertionBuilder withEncrypterCredential(Credential credential) {
        this.credential = ofNullable(credential);
        return this;
    }

    public EncryptedAssertionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public EncryptedAssertionBuilder withPublicEncryptionCert(String cert) {
        this.credential = ofNullable(new TestCredentialFactory(cert, null).getEncryptingCredential());
        return this;
    }
}
