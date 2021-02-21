package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.impl.CollectionCredentialResolver;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.test.builders.ConditionsBuilder;
import stubidp.saml.test.builders.IssuerBuilder;
import stubidp.saml.test.builders.SubjectBuilder;
import stubidp.saml.test.support.PrivateKeyStoreFactory;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import stubidp.test.devpki.TestEntityIds;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.services.MatchingAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static stubidp.saml.extensions.extensions.IdaAuthnContext.LEVEL_2_AUTHN_CTX;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AudienceRestrictionBuilder.anAudienceRestriction;
import static stubidp.saml.test.builders.AuthnContextBuilder.anAuthnContext;
import static stubidp.saml.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static stubidp.saml.test.builders.ConditionsBuilder.aConditions;
import static stubidp.saml.test.builders.SignatureBuilder.aSignature;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static stubidp.test.devpki.TestCertificateStrings.TEST_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_PUBLIC_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.MatchingScenario.SUCCESS_MATCH;

public class MatchingAssertionTranslatorTest extends OpenSAMLRunner {

    private static final String IN_RESPONSE_TO = "_some-request-id";
    private static final String VERIFY_SERVICE_PROVIDER_ENTITY_ID = "default-entity-id";

    private final Credential testRpMsaSigningCredential = createMSSigningCredential();

    private MatchingAssertionTranslator msaAssertionTranslator;

    private Credential createMSSigningCredential() {
        Credential signingCredential = new TestCredentialFactory(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY).getSigningCredential();
        ((BasicCredential) signingCredential).setEntityId(TestEntityIds.TEST_RP_MS);
        return signingCredential;
    }

    @BeforeEach
    public void setUp() throws KeyException {
        PrivateKey privateKey = new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP).getEncryptionPrivateKeys().get(0);
        KeyPair keyPair = new KeyPair(KeySupport.derivePublicKey(privateKey), privateKey);
        List<KeyPair> keyPairs = asList(keyPair, keyPair);
        ResponseFactory responseFactory = new ResponseFactory(keyPairs);

        CollectionCredentialResolver resolver = new CollectionCredentialResolver(Collections.singletonList(testRpMsaSigningCredential));
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(resolver, DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());

        DateTimeComparator dateTimeComparator = new DateTimeComparator(Duration.ofSeconds(5));

        msaAssertionTranslator = responseFactory.createMsaAssertionTranslator(explicitKeySignatureTrustEngine, new SignatureValidatorFactory(), dateTimeComparator);
    }

    @Test
    public void shouldTranslateValidAssertion() {
        TranslatedMatchingResponseBody result = (TranslatedMatchingResponseBody) msaAssertionTranslator.translateSuccessResponse(List.of(
                anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).buildUnencrypted()
        ), IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID);
        assertThat(result).isEqualTo(new TranslatedMatchingResponseBody(
                SUCCESS_MATCH,
                "some-pid",
                LEVEL_2,
                null
        ));
    }

    @Test
    public void shouldAllowHigherLevelOfAssuranceThanRequested() {
        TranslatedMatchingResponseBody result = (TranslatedMatchingResponseBody) msaAssertionTranslator.translateSuccessResponse(List.of(
                anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).buildUnencrypted()
        ), IN_RESPONSE_TO, LEVEL_1, VERIFY_SERVICE_PROVIDER_ENTITY_ID);
        assertThat(result).isEqualTo(new TranslatedMatchingResponseBody(
                SUCCESS_MATCH,
                "some-pid",
                LEVEL_2,
                null
        ));
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsIsEmptyList() {
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> msaAssertionTranslator.translateSuccessResponse(emptyList(), IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID))
                .withMessage("Exactly one assertion is expected.");
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsIsNull() {
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> msaAssertionTranslator.translateSuccessResponse(null, IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID))
                .withMessage("Exactly one assertion is expected.");
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsListSizeIsLargerThanOne() {
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> msaAssertionTranslator.translateSuccessResponse(
                        List.of(
                                anAssertion().buildUnencrypted(),
                                anAssertion().buildUnencrypted()
                        ),
                        IN_RESPONSE_TO,
                        LEVEL_2,
                        VERIFY_SERVICE_PROVIDER_ENTITY_ID))
                .withMessage("Exactly one assertion is expected.");
    }

    @Test
    public void shouldThrowExceptionWhenAssertionIsNotSigned() {
        assertThatExceptionOfType(SamlTransformationErrorException.class)
                .isThrownBy(() -> msaAssertionTranslator.translateSuccessResponse(Collections.singletonList(
                        anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).withoutSigning().buildUnencrypted()),
                        IN_RESPONSE_TO,
                        LEVEL_2,
                        VERIFY_SERVICE_PROVIDER_ENTITY_ID
                ))
                .withMessageContaining("SAML Validation Specification: Message signature is not signed");
    }

    @Test
    public void shouldThrowExceptionWhenAssertionSignedByUnknownKey() {
        Credential unknownSigningCredential = new TestCredentialFactory(TEST_PUBLIC_CERT, TEST_PRIVATE_KEY).getSigningCredential();
        assertThatExceptionOfType(SamlTransformationErrorException.class)
                .isThrownBy(() -> msaAssertionTranslator.translateSuccessResponse(Collections.singletonList(
                        anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX)
                                .withSignature(aSignature().withSigningCredential(unknownSigningCredential).build())
                                .buildUnencrypted()),
                        IN_RESPONSE_TO,
                        LEVEL_2,
                        VERIFY_SERVICE_PROVIDER_ENTITY_ID
                ))
                .withMessageContaining("SAML Validation Specification: Signature was not valid.");
    }

    @Test
    public void shouldThrowExceptionWhenLevelOfAssuranceNotPresent() {
        AuthnStatement authnStatement = anAuthnStatement().withAuthnContext(anAuthnContext()
                .withAuthnContextClassRef(null)
                .build())
                .build();
        Assertion assertion = aSignedAssertion()
                .addAuthnStatement(authnStatement)
                .buildUnencrypted();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> msaAssertionTranslator.translateSuccessResponse(List.of(assertion), IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID))
                .withMessage("Expected a level of assurance.");
    }

    @Test
    public void shouldThrowExceptionWithUnknownLevelOfAssurance() {
        Assertion assertion = aSignedAssertion()
                .addAuthnStatement(anAuthnStatement()
                        .withAuthnContext(anAuthnContext()
                                .withAuthnContextClassRef(anAuthnContextClassRef()
                                        .withAuthnContextClasRefValue("unknown")
                                        .build())
                                .build())
                        .build())
                .buildUnencrypted();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> msaAssertionTranslator.translateSuccessResponse(List.of(assertion), IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID))
                .withMessage("Level of assurance 'unknown' is not supported.");
    }

    private AssertionBuilder aSignedAssertion() {
        Issuer issuer = IssuerBuilder.anIssuer().build();
        issuer.setValue(TestEntityIds.TEST_RP_MS);
        return anAssertion()
                .withIssuer(issuer)
                .withSubject(aValidSubject().build())
                .withConditions(aValidConditions().build())
                .withSignature(aSignature()
                        .withSigningCredential(testRpMsaSigningCredential)
                        .build());
    }

    private SubjectBuilder aValidSubject() {
        return aSubject()
                .withSubjectConfirmation(
                        aSubjectConfirmation()
                                .withSubjectConfirmationData(aSubjectConfirmationData()
                                        .withNotOnOrAfter(Instant.now().plus(15, ChronoUnit.MINUTES))
                                        .withInResponseTo(IN_RESPONSE_TO)
                                        .build())
                                .build());
    }

    private ConditionsBuilder aValidConditions() {
        return aConditions()
                .withoutDefaultAudienceRestriction()
                .addAudienceRestriction(anAudienceRestriction()
                        .withAudienceId(VERIFY_SERVICE_PROVIDER_ENTITY_ID)
                        .build());
    }

    private AssertionBuilder anAssertionWith(String pid, String levelOfAssurance) {
        return aSignedAssertion()
                .withSubject(aValidSubject().withPersistentId(pid).build())
                .withConditions(aValidConditions().build())
                .addAuthnStatement(anAuthnStatement()
                        .withAuthnContext(anAuthnContext()
                                .withAuthnContextClassRef(anAuthnContextClassRef()
                                        .withAuthnContextClasRefValue(levelOfAssurance).build())
                                .build())
                        .build());
    }
}