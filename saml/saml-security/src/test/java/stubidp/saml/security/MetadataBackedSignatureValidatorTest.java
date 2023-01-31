package stubidp.saml.security;

import io.dropwizard.util.Resources;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import stubidp.saml.test.support.StringEncoding;
import stubidp.saml.security.saml.deserializers.AuthnRequestUnmarshaller;
import stubidp.saml.security.saml.deserializers.SamlObjectParser;
import stubidp.saml.security.saml.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.test.builders.EntitiesDescriptorBuilder;
import stubidp.saml.test.builders.EntityDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.builders.KeyInfoBuilder;
import stubidp.saml.test.builders.SPSSODescriptorBuilder;
import stubidp.saml.test.builders.SignatureBuilder;
import stubidp.saml.test.builders.X509CertificateBuilder;
import stubidp.saml.test.builders.X509DataBuilder;
import stubidp.saml.test.metadata.EntityDescriptorFactory;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.security.security.verification.CertificateChainValidator;
import stubidp.utils.security.security.verification.CertificateValidity;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataBackedSignatureValidatorTest extends OpenSAMLRunner {
    private static final String issuerId = TestEntityIds.HUB_ENTITY_ID;
    private static final KeyInfoCredentialResolver keyInfoResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();

    @Test
    void shouldValidateSignatureUsingTrustedCredentials() throws Exception {
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator();
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).buildUnencrypted();
        assertThat(metadataBackedSignatureValidator.validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(true);
    }

    @Test
    void shouldFailIfCertificatesHaveTheWrongUsage() throws Exception {
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidatorWithWrongUsageCertificates();
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).buildUnencrypted();
        assertThat(metadataBackedSignatureValidator.validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(false);
    }

    @Test
    void shouldFailValidationIfKeyInfoNotPresentInMetadata() throws Exception {
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator();
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT, TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY).getSigningCredential();
        Signature signature = createSignatureWithKeyInfo(signingCredential, TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(signature).buildUnencrypted();
        assertThat(metadataBackedSignatureValidator.validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(false);
    }

    @Test
    void shouldFailValidationIfCertificateDoesNotChainWithATrustedRoot() throws Exception {
        CertificateChainValidator invalidCertificateChainMockValidator = createCertificateChainValidator(CertificateValidity.invalid(new CertPathValidatorException()));
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidatorWithChainValidation(invalidCertificateChainMockValidator);
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).buildUnencrypted();

        boolean validationResult = metadataBackedSignatureValidator.validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        assertThat(validationResult).as("Assertion was expected to be invalid due to an invalid certificate chain").isEqualTo(false);
    }

    private Signature createSignatureWithKeyInfo(Credential signingCredential, String certificateString) {
        Signature signature = SignatureBuilder.aSignature().withSigningCredential(signingCredential).build();
        org.opensaml.xmlsec.signature.X509Certificate certificate = X509CertificateBuilder.aX509Certificate().withCert(certificateString).build();
        X509Data x509 = X509DataBuilder.aX509Data().withX509Certificate(certificate).build();
        signature.setKeyInfo(KeyInfoBuilder.aKeyInfo().withX509Data(x509).build());
        return signature;
    }

    private MetadataBackedSignatureValidator createMetadataBackedSignatureValidator() throws ComponentInitializationException {
        return MetadataBackedSignatureValidator.withoutCertificateChainValidation(getExplicitKeySignatureTrustEngine());
    }

    private MetadataBackedSignatureValidator createMetadataBackedSignatureValidatorWithWrongUsageCertificates() throws ComponentInitializationException {
        return MetadataBackedSignatureValidator.withoutCertificateChainValidation(getExplicitKeySignatureTrustEngineEncryptionOnly());
    }

    private MetadataBackedSignatureValidator createMetadataBackedSignatureValidatorWithChainValidation(CertificateChainValidator certificateChainValidator) throws ComponentInitializationException {
        ExplicitKeySignatureTrustEngine signatureTrustEngine = getExplicitKeySignatureTrustEngine();
        CertificateChainEvaluableCriterion certificateChainEvaluableCriterion = new CertificateChainEvaluableCriterion(certificateChainValidator, null);
        return MetadataBackedSignatureValidator.withCertificateChainValidation(signatureTrustEngine, certificateChainEvaluableCriterion);
    }

    private String loadMetadata() {
        final SPSSODescriptor spssoDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor()
                .addSupportedProtocol("urn:oasis:names:tc:SAML:2.0:protocol")
                .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor()
                        .withX509ForSigning(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT).build())
                .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor()
                        .withX509ForSigning(TestCertificateStrings.METADATA_SIGNING_B_PUBLIC_CERT).build())
                .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor()
                        .withX509ForEncryption(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT).build())
                .build();

        try {
            final EntityDescriptor entityDescriptor = EntityDescriptorBuilder.anEntityDescriptor()
                    .withId("0a2bf940-e6fe-4f32-833d-022dfbfc77c5")
                    .withEntityId("https://signin.service.gov.uk")
                    .withValidUntil(Instant.now().atZone(ZoneId.of("UTC")).plusYears(100).toInstant())
                    .withCacheDuration(Duration.ofMillis(6000000L))
                    .addSpServiceDescriptor(spssoDescriptor)
                    .build();

            return new MetadataFactory().metadata(EntitiesDescriptorBuilder.anEntitiesDescriptor()
                    .withEntityDescriptors(Collections.singletonList(entityDescriptor)).build());
        } catch (MarshallingException | SignatureException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ExplicitKeySignatureTrustEngine getExplicitKeySignatureTrustEngine() throws ComponentInitializationException {
        StringBackedMetadataResolver metadataResolver = new StringBackedMetadataResolver(loadMetadata());
        MetadataCredentialResolver metadataCredentialResolver = getMetadataCredentialResolver(metadataResolver);
        return new ExplicitKeySignatureTrustEngine(metadataCredentialResolver, keyInfoResolver);
    }

    private ExplicitKeySignatureTrustEngine getExplicitKeySignatureTrustEngineEncryptionOnly() throws ComponentInitializationException {
        MetadataFactory metadataFactory = new MetadataFactory();
        final EntityDescriptorFactory entityDescriptorFactory = new EntityDescriptorFactory();
        String metadataContainingWrongUsage = metadataFactory.metadata(
                Collections.singletonList(entityDescriptorFactory.hubEntityDescriptorWithWrongUsageCertificates()));

        StringBackedMetadataResolver metadataResolver = new StringBackedMetadataResolver(metadataContainingWrongUsage);
        MetadataCredentialResolver metadataCredentialResolver = getMetadataCredentialResolver(metadataResolver);
        return new ExplicitKeySignatureTrustEngine(metadataCredentialResolver, keyInfoResolver);
    }

    private MetadataCredentialResolver getMetadataCredentialResolver(StringBackedMetadataResolver metadataResolver) throws ComponentInitializationException {
        BasicParserPool basicParserPool = new BasicParserPool();
        basicParserPool.initialize();
        metadataResolver.setParserPool(basicParserPool);
        metadataResolver.setRequireValidMetadata(true);
        metadataResolver.setId("arbitrary id");
        metadataResolver.initialize();

        PredicateRoleDescriptorResolver predicateRoleDescriptorResolver = new PredicateRoleDescriptorResolver(metadataResolver);
        predicateRoleDescriptorResolver.initialize();

        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolver();
        metadataCredentialResolver.setRoleDescriptorResolver(predicateRoleDescriptorResolver);
        metadataCredentialResolver.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
        metadataCredentialResolver.initialize();
        return metadataCredentialResolver;
    }

    private CertificateChainValidator createCertificateChainValidator(CertificateValidity validity) {
        CertificateChainValidator certificateChainValidator = mock(CertificateChainValidator.class);
        when(certificateChainValidator.validate(any(X509Certificate.class), eq(null))).thenReturn(validity);
        return certificateChainValidator;
    }

    /* ******************************************************************************************* *
     * Tests below this point were lifted from SignatureValidatorTest to check that
     * MetadataBackedSignatureValidator has equivalent behaviour.
     * These test cover mostly OpenSAML code.
     * ******************************************************************************************* */

    @Test
    void shouldAcceptSignedAssertions() throws Exception {
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).buildUnencrypted();
        assertThat(createMetadataBackedSignatureValidator().validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(true);
    }

    @Test
    void shouldNotAcceptUnsignedAssertions() throws Exception {
        assertThat(createMetadataBackedSignatureValidator().validate(AssertionBuilder.anAssertion().withoutSigning().buildUnencrypted(), issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(false);
    }

    @Test
    void shouldNotAcceptMissignedAssertions() throws Exception {
        Credential badSigningCredential = new TestCredentialFactory(TestCertificateStrings.UNCHAINED_PUBLIC_CERT, TestCertificateStrings.UNCHAINED_PRIVATE_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(badSigningCredential).build()).buildUnencrypted();
        assertThat(createMetadataBackedSignatureValidator().validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(false);
    }

    /*
     * Signature algorithm should be valid.
     */
    @Test
    void shouldNotValidateBadSignatureAlgorithm() throws Exception {
        InputStream authnRequestUrl = getClass().getClassLoader().getResourceAsStream("authnRequestNormal.xml");//sha1 authnrequest
        String input = StringEncoding.toBase64Encoded(new String(authnRequestUrl.readAllBytes(), UTF_8));
        //md5 authnrequests throw an exception here as they are not allowed to be unmarshalled
        AuthnRequest request = getStringToOpenSamlObjectTransformer().apply(input);
        assertThat(createMetadataBackedSignatureValidator().validate(request, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isFalse();
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
        AuthnRequest request = getStringToOpenSamlObjectTransformer().apply(input);
        createMetadataBackedSignatureValidator().validate(request, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    private StringToOpenSamlObjectTransformer getStringToOpenSamlObjectTransformer() {
        return new StringToOpenSamlObjectTransformer(new AuthnRequestUnmarshaller(new SamlObjectParser()));
    }
}
