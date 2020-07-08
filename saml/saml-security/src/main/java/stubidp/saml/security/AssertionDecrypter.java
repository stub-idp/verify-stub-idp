package stubidp.saml.security;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import stubidp.saml.security.errors.SamlTransformationErrorFactory;
import stubidp.saml.security.exception.SamlFailedToDecryptException;
import stubidp.saml.security.validators.ValidatedEncryptedAssertionContainer;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;

import java.util.ArrayList;
import java.util.List;

public class AssertionDecrypter {

    private final EncryptionAlgorithmValidator encryptionAlgorithmValidator;
    private final Decrypter decrypter;

    public AssertionDecrypter(EncryptionAlgorithmValidator encryptionAlgorithmValidator, Decrypter decrypter) {
        this.encryptionAlgorithmValidator = encryptionAlgorithmValidator;
        this.decrypter = decrypter;
    }

    public List<Assertion> decryptAssertions(ValidatedEncryptedAssertionContainer container) {
        final List<EncryptedAssertion> encryptedAssertions = container.getEncryptedAssertions();
        final List<Assertion> assertions = new ArrayList<>();

        if (encryptedAssertions.isEmpty()) return List.of();

        decrypter.setRootInNewDocument(true);

        for (EncryptedAssertion encryptedAssertion : encryptedAssertions) {
            try {
                encryptionAlgorithmValidator.validate(encryptedAssertion);
                assertions.add(decrypter.decrypt(encryptedAssertion));
            } catch (DecryptionException e) {
                String message = "Problem decrypting assertion " + encryptedAssertion + ".";
                throw new SamlFailedToDecryptException(SamlTransformationErrorFactory.unableToDecrypt(message), e);
            }
        }

        return List.copyOf(assertions);
    }
}
