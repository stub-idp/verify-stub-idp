package stubidp.saml.security;

import io.dropwizard.util.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.test.support.StringEncoding;
import stubidp.saml.security.saml.deserializers.AuthnRequestUnmarshaller;
import stubidp.saml.security.saml.deserializers.SamlObjectParser;
import stubidp.saml.security.saml.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.test.builders.SignatureBuilder;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CredentialFactorySignatureValidatorTest extends OpenSAMLRunner {
    private final String issuerId = TestEntityIds.HUB_ENTITY_ID;
    private final SigningCredentialFactory credentialFactory = new SigningCredentialFactory(new HardCodedKeyStore(issuerId));
    private final CredentialFactorySignatureValidator credentialFactorySignatureValidator = new CredentialFactorySignatureValidator(credentialFactory);

    @Test
    void shouldAcceptSignedAssertions() throws Exception {
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).buildUnencrypted();
        assertThat(credentialFactorySignatureValidator.validate(assertion, issuerId, null)).isEqualTo(true);
    }

    @Test
    void shouldNotAcceptUnsignedAssertions() throws Exception {
        assertThat(credentialFactorySignatureValidator.validate(AssertionBuilder.anAssertion().withoutSigning().buildUnencrypted(), issuerId, null)).isEqualTo(false);
    }

    @Test
    void shouldNotAcceptMissignedAssertions() throws Exception {
        Credential badSigningCredential = new TestCredentialFactory(TestCertificateStrings.UNCHAINED_PUBLIC_CERT, TestCertificateStrings.UNCHAINED_PRIVATE_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(badSigningCredential).build()).buildUnencrypted();
        assertThat(credentialFactorySignatureValidator.validate(assertion, issuerId, null)).isEqualTo(false);
    }

    @Test
    void shouldSupportAnEntityWithMultipleSigningCertificates() throws Exception {
        List<String> certificates = asList(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT);
        final Map<String, List<String>> publicKeys = Map.of(issuerId, certificates);
        final InjectableSigningKeyStore injectableSigningKeyStore = new InjectableSigningKeyStore(publicKeys);
        final CredentialFactorySignatureValidator credentialFactorySignatureValidator = new CredentialFactorySignatureValidator(new SigningCredentialFactory(injectableSigningKeyStore));

        Credential firstSigningCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        boolean validate = credentialFactorySignatureValidator.validate(AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(firstSigningCredential).build()).buildUnencrypted(), issuerId, null);
        assertThat(validate).isEqualTo(true);

        Credential secondSigningCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SECONDARY_SIGNING_KEY).getSigningCredential();
        validate = credentialFactorySignatureValidator.validate(AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(secondSigningCredential).build()).buildUnencrypted(), issuerId, null);
        assertThat(validate).isEqualTo(true);

        Credential thirdSigningCredential = new TestCredentialFactory(TestCertificateStrings.UNCHAINED_PUBLIC_CERT, TestCertificateStrings.UNCHAINED_PRIVATE_KEY).getSigningCredential();
        validate = credentialFactorySignatureValidator.validate(AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(thirdSigningCredential).build()).buildUnencrypted(), issuerId, null);
        assertThat(validate).isEqualTo(false);
    }

    /*
     * Signature algorithm should be valid.
     */
    @Test
    void shouldNotValidateBadSignatureAlgorithm() throws Exception {
        InputStream authnRequestUrl = getClass().getClassLoader().getResourceAsStream("authnRequestNormal.xml");//sha1 authnrequest
        String input = StringEncoding.toBase64Encoded(new String(authnRequestUrl.readAllBytes(), UTF_8));
        //md5 authnrequests throw an exception here as they are not allowed to be unmarshalled
        AuthnRequest request = getStringtoOpenSamlObjectTransformer().apply(input);
        assertThat(credentialFactorySignatureValidator.validate(request, issuerId, null)).isFalse();
    }

    /*
     * Signature object should exist.
     */
    @Test
    void shouldNotValidateMissingSignature() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoSignature.xml"));
    }

    /*
     * Signature must be an immediate child of the SAML object.
     */
    @Test
    void shouldNotValidateSignatureNotImmediateChild() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNotImmediateChild.xml"));
    }

    /*
     * Signature should not contain more than one Reference.
     */
    @Test
    void shouldNotValidateSignatureTooManyReferences() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestTooManyRefs.xml"));
    }

    /*
     * Reference requires a valid URI pointing to a fragment ID.
     */
    @Test
    void shouldNotValidateSignatureBadReferenceURI() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestBadRefURI.xml"));
    }

    /*
     * Reference URI should point to parent SAML object.
     */
    @Test
    void shouldNotValidateSignatureReferenceURINotParentID() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestRefURINotParentID.xml"));
    }

    /*
     * Root SAML object should have an ID.
     */
    @Test
    void shouldNotValidateSignatureNoParentID() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoParentID.xml"));
    }

    /*
     * Signature must have Transforms defined.
     */
    @Test
    void shouldNotValidateSignatureNoTransforms() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoTransforms.xml"));
    }

    /*
     * Signature should not have more than two Transforms.
     */
    @Test
    void shouldNotValidateSignatureTooManyTransforms() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestTooManyTransforms.xml"));
    }

    /*
     * Signature must have enveloped-signature Transform.
     */
    @Test
    void shouldNotValidateSignatureNoEnvelopeTransform() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoEnvTransform.xml"));
    }

    /*
     * Signature must have a valid enveloped-signature Transform.
     */
    @Test
    void shouldNotValidateSignatureInvalidEnvelopeTransform() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestInvalidEnvTransform.xml"));
    }

    /*
     * Signature should not contain any Object children.
     */
    @Test
    void shouldNotValidateSignatureContainingObject() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestSigContainsChildren.xml"));
    }

    private void validateAuthnRequestFile(String fileName) throws Exception {
        InputStream authnRequestUrl = getClass().getClassLoader().getResourceAsStream(fileName);
        String input = StringEncoding.toBase64Encoded(new String(authnRequestUrl.readAllBytes(), UTF_8));
        AuthnRequest request = getStringtoOpenSamlObjectTransformer().apply(input);
        credentialFactorySignatureValidator.validate(request, issuerId, null);
    }

    private StringToOpenSamlObjectTransformer getStringtoOpenSamlObjectTransformer() {
        return new StringToOpenSamlObjectTransformer(new AuthnRequestUnmarshaller(new SamlObjectParser()));
    }
}
