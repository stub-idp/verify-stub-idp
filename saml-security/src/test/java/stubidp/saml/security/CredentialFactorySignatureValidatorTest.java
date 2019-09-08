package stubidp.saml.security;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.security.saml.StringEncoding;
import stubidp.saml.security.saml.TestCredentialFactory;
import stubidp.saml.security.saml.builders.AssertionBuilder;
import stubidp.saml.security.saml.builders.SignatureBuilder;
import stubidp.saml.security.saml.deserializers.AuthnRequestUnmarshaller;
import stubidp.saml.security.saml.deserializers.SamlObjectParser;
import stubidp.saml.security.saml.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CredentialFactorySignatureValidatorTest extends OpenSAMLRunner {
    
    private final String issuerId = TestEntityIds.HUB_ENTITY_ID;
    private final SigningCredentialFactory credentialFactory = new SigningCredentialFactory(new HardCodedKeyStore(issuerId));
    private final CredentialFactorySignatureValidator credentialFactorySignatureValidator = new CredentialFactorySignatureValidator(credentialFactory);

    @Test
    public void shouldAcceptSignedAssertions() throws Exception {
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).build();
        assertThat(credentialFactorySignatureValidator.validate(assertion, issuerId, null)).isEqualTo(true);
    }

    @Test
    public void shouldNotAcceptUnsignedAssertions() throws Exception {
        assertThat(credentialFactorySignatureValidator.validate(AssertionBuilder.anAssertion().withoutSigning().build(), issuerId, null)).isEqualTo(false);
    }

    @Test
    public void shouldNotAcceptMissignedAssertions() throws Exception {
        Credential badSigningCredential = new TestCredentialFactory(TestCertificateStrings.UNCHAINED_PUBLIC_CERT, TestCertificateStrings.UNCHAINED_PRIVATE_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(badSigningCredential).build()).build();
        assertThat(credentialFactorySignatureValidator.validate(assertion, issuerId, null)).isEqualTo(false);
    }

    @Test
    public void shouldSupportAnEntityWithMultipleSigningCertificates() throws Exception {
        List<String> certificates = asList(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT);
        final Map<String, List<String>> publicKeys = Map.of(issuerId, certificates);
        final InjectableSigningKeyStore injectableSigningKeyStore = new InjectableSigningKeyStore(publicKeys);
        final CredentialFactorySignatureValidator credentialFactorySignatureValidator = new CredentialFactorySignatureValidator(new SigningCredentialFactory(injectableSigningKeyStore));

        Credential firstSigningCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        boolean validate = credentialFactorySignatureValidator.validate(AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(firstSigningCredential).build()).build(), issuerId, null);
        assertThat(validate).isEqualTo(true);

        Credential secondSigningCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SECONDARY_SIGNING_KEY).getSigningCredential();
        validate = credentialFactorySignatureValidator.validate(AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(secondSigningCredential).build()).build(), issuerId, null);
        assertThat(validate).isEqualTo(true);

        Credential thirdSigningCredential = new TestCredentialFactory(TestCertificateStrings.UNCHAINED_PUBLIC_CERT, TestCertificateStrings.UNCHAINED_PRIVATE_KEY).getSigningCredential();
        validate = credentialFactorySignatureValidator.validate(AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(thirdSigningCredential).build()).build(), issuerId, null);
        assertThat(validate).isEqualTo(false);
    }

    /*
     * Signature algorithm should be valid.
     */
    @Test
    public void shouldNotValidateBadSignatureAlgorithm() throws Exception {
        URL authnRequestUrl = getClass().getClassLoader().getResource("authnRequestBadAlgorithm.xml");
        String input = StringEncoding.toBase64Encoded(Resources.toString(authnRequestUrl, Charsets.UTF_8));
        AuthnRequest request = getStringtoOpenSamlObjectTransformer().apply(input);
        assertThat(credentialFactorySignatureValidator.validate(request, issuerId, null)).isFalse();
    }

    /*
     * Signature object should exist.
     */
    @Test
    public void shouldNotValidateMissingSignature() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoSignature.xml"));
    }

    /*
     * Signature must be an immediate child of the SAML object.
     */
    @Test
    public void shouldNotValidateSignatureNotImmediateChild() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNotImmediateChild.xml"));
    }

    /*
     * Signature should not contain more than one Reference.
     */
    @Test
    public void shouldNotValidateSignatureTooManyReferences() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestTooManyRefs.xml"));
    }

    /*
     * Reference requires a valid URI pointing to a fragment ID.
     */
    @Test
    public void shouldNotValidateSignatureBadReferenceURI() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestBadRefURI.xml"));
    }

    /*
     * Reference URI should point to parent SAML object.
     */
    @Test
    public void shouldNotValidateSignatureReferenceURINotParentID() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestRefURINotParentID.xml"));
    }

    /*
     * Root SAML object should have an ID.
     */
    @Test
    public void shouldNotValidateSignatureNoParentID() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoParentID.xml"));
    }

    /*
     * Signature must have Transforms defined.
     */
    @Test
    public void shouldNotValidateSignatureNoTransforms() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoTransforms.xml"));
    }

    /*
     * Signature should not have more than two Transforms.
     */
    @Test
    public void shouldNotValidateSignatureTooManyTransforms() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestTooManyTransforms.xml"));
    }

    /*
     * Signature must have enveloped-signature Transform.
     */
    @Test
    public void shouldNotValidateSignatureNoEnvelopeTransform() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoEnvTransform.xml"));
    }

    /*
     * Signature must have a valid enveloped-signature Transform.
     */
    @Test
    public void shouldNotValidateSignatureInvalidEnvelopeTransform() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestInvalidEnvTransform.xml"));
    }

    /*
     * Signature should not contain any Object children.
     */
    @Test
    public void shouldNotValidateSignatureContainingObject() throws Exception {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestSigContainsChildren.xml"));
    }

    private void validateAuthnRequestFile(String fileName) throws Exception {
        URL authnRequestUrl = getClass().getClassLoader().getResource(fileName);
        String input = StringEncoding.toBase64Encoded(Resources.toString(authnRequestUrl, Charsets.UTF_8));
        AuthnRequest request = getStringtoOpenSamlObjectTransformer().apply(input);
        credentialFactorySignatureValidator.validate(request, issuerId, null);
    }

    private StringToOpenSamlObjectTransformer getStringtoOpenSamlObjectTransformer() {
        return new StringToOpenSamlObjectTransformer(new AuthnRequestUnmarshaller(new SamlObjectParser()));
    }
}
