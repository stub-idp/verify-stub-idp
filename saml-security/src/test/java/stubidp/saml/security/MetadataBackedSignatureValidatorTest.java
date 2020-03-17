package stubidp.saml.security;

import com.google.common.io.Resources;
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
import stubidp.saml.security.saml.EntityDescriptorFactory;
import stubidp.saml.security.saml.MetadataFactory;
import stubidp.saml.security.saml.StringEncoding;
import stubidp.saml.security.saml.TestCredentialFactory;
import stubidp.saml.security.saml.builders.AssertionBuilder;
import stubidp.saml.security.saml.builders.EntitiesDescriptorBuilder;
import stubidp.saml.security.saml.builders.EntityDescriptorBuilder;
import stubidp.saml.security.saml.builders.KeyDescriptorBuilder;
import stubidp.saml.security.saml.builders.KeyInfoBuilder;
import stubidp.saml.security.saml.builders.SPSSODescriptorBuilder;
import stubidp.saml.security.saml.builders.SignatureBuilder;
import stubidp.saml.security.saml.builders.X509CertificateBuilder;
import stubidp.saml.security.saml.builders.X509DataBuilder;
import stubidp.saml.security.saml.deserializers.AuthnRequestUnmarshaller;
import stubidp.saml.security.saml.deserializers.SamlObjectParser;
import stubidp.saml.security.saml.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.security.security.verification.CertificateChainValidator;
import stubidp.utils.security.security.verification.CertificateValidity;

import java.net.URL;
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

    private final String issuerId = TestEntityIds.HUB_ENTITY_ID;

    private KeyInfoCredentialResolver keyInfoResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();

    @Test
    public void shouldValidateSignatureUsingTrustedCredentials() throws Exception {
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator();
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).build();
        assertThat(metadataBackedSignatureValidator.validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(true);
    }

    @Test
    public void shouldFailIfCertificatesHaveTheWrongUsage() throws Exception {
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidatorWithWrongUsageCertificates();
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).build();
        assertThat(metadataBackedSignatureValidator.validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(false);
    }

    @Test
    public void shouldFailValidationIfKeyInfoNotPresentInMetadata() throws Exception {
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator();
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT, TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY).getSigningCredential();
        Signature signature = createSignatureWithKeyInfo(signingCredential, TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(signature).build();
        assertThat(metadataBackedSignatureValidator.validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(false);
    }

    @Test
    public void shouldFailValidationIfCertificateDoesNotChainWithATrustedRoot() throws Exception {
        CertificateChainValidator invalidCertificateChainMockValidator = createCertificateChainValidator(CertificateValidity.invalid(new CertPathValidatorException()));
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidatorWithChainValidation(invalidCertificateChainMockValidator);
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).build();

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
    public void shouldAcceptSignedAssertions() throws Exception {
        Credential signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build()).build();
        assertThat(createMetadataBackedSignatureValidator().validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(true);
    }

    @Test
    public void shouldNotAcceptUnsignedAssertions() throws Exception {
        assertThat(createMetadataBackedSignatureValidator().validate(AssertionBuilder.anAssertion().withoutSigning().build(), issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(false);
    }

    @Test
    public void shouldNotAcceptMissignedAssertions() throws Exception {
        Credential badSigningCredential = new TestCredentialFactory(TestCertificateStrings.UNCHAINED_PUBLIC_CERT, TestCertificateStrings.UNCHAINED_PRIVATE_KEY).getSigningCredential();
        final Assertion assertion = AssertionBuilder.anAssertion().withSignature(SignatureBuilder.aSignature().withSigningCredential(badSigningCredential).build()).build();
        assertThat(createMetadataBackedSignatureValidator().validate(assertion, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isEqualTo(false);
    }

    /*
     * Signature algorithm should be valid.
     */
    @Test
    public void shouldNotValidateBadSignatureAlgorithm() throws Exception {
        URL authnRequestUrl = getClass().getClassLoader().getResource("authnRequestNormal.xml");//sha1 authnrequest
        String input = StringEncoding.toBase64Encoded(Resources.toString(authnRequestUrl, UTF_8));
        //md5 authnrequests throw an exception here as they are not allowed to be unmarshalled
        AuthnRequest request = getStringtoOpenSamlObjectTransformer().apply(input);
        assertThat(createMetadataBackedSignatureValidator().validate(request, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).isFalse();
    }

    /*
     * Signature object should exist.
     */
    @Test
    public void shouldNotValidateMissingSignature() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoSignature.xml"));
    }

    /*
     * Signature must be an immediate child of the SAML object.
     */
    @Test
    public void shouldNotValidateSignatureNotImmediateChild() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNotImmediateChild.xml"));
    }

    /*
     * Signature should not contain more than one Reference.
     */
    @Test
    public void shouldNotValidateSignatureTooManyReferences() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestTooManyRefs.xml"));
    }

    /*
     * Reference requires a valid URI pointing to a fragment ID.
     */
    @Test
    public void shouldNotValidateSignatureBadReferenceURI() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestBadRefURI.xml"));
    }

    /*
     * Reference URI should point to parent SAML object.
     */
    @Test
    public void shouldNotValidateSignatureReferenceURINotParentID() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestRefURINotParentID.xml"));
    }

    /*
     * Root SAML object should have an ID.
     */
    @Test
    public void shouldNotValidateSignatureNoParentID() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoParentID.xml"));
    }

    /*
     * Signature must have Transforms defined.
     */
    @Test
    public void shouldNotValidateSignatureNoTransforms() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoTransforms.xml"));
    }

    /*
     * Signature should not have more than two Transforms.
     */
    @Test
    public void shouldNotValidateSignatureTooManyTransforms() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestTooManyTransforms.xml"));
    }

    /*
     * Signature must have enveloped-signature Transform.
     */
    @Test
    public void shouldNotValidateSignatureNoEnvelopeTransform() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestNoEnvTransform.xml"));
    }

    /*
     * Signature must have a valid enveloped-signature Transform.
     */
    @Test
    public void shouldNotValidateSignatureInvalidEnvelopeTransform() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestInvalidEnvTransform.xml"));
    }

    /*
     * Signature should not contain any Object children.
     */
    @Test
    public void shouldNotValidateSignatureContainingObject() {
        Assertions.assertThrows(SignatureException.class, () -> validateAuthnRequestFile("authnRequestSigContainsChildren.xml"));
    }

    private void validateAuthnRequestFile(String fileName) throws Exception {
        URL authnRequestUrl = getClass().getClassLoader().getResource(fileName);
        String input = StringEncoding.toBase64Encoded(Resources.toString(authnRequestUrl, UTF_8));
        AuthnRequest request = getStringtoOpenSamlObjectTransformer().apply(input);
        createMetadataBackedSignatureValidator().validate(request, issuerId, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    private StringToOpenSamlObjectTransformer getStringtoOpenSamlObjectTransformer() {
        return new StringToOpenSamlObjectTransformer(new AuthnRequestUnmarshaller(new SamlObjectParser()));
    }
}
