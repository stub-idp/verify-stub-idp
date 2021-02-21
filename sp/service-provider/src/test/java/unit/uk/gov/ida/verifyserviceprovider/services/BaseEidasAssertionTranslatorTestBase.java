package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Subject;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.metadata.EidasMetadataResolverRepository;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.utils.core.transformers.EidasMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import stubidp.saml.utils.hub.factories.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TestTranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.services.BaseEidasAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.EidasAssertionTranslatorValidatorContainer;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.extensions.extensions.EidasAuthnContext.EIDAS_LOA_SUBSTANTIAL;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.test.builders.AuthnContextBuilder.anAuthnContext;
import static stubidp.saml.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static stubidp.saml.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static stubidp.test.devpki.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public abstract class BaseEidasAssertionTranslatorTestBase extends OpenSAMLRunner {

    protected BaseEidasAssertionTranslator assertionService;

    @Mock(lenient = true)
    SubjectValidator subjectValidator;
    @Mock(lenient = true)
    EidasMatchingDatasetUnmarshaller eidasMatchingDatasetUnmarshaller;
    @Mock(lenient = true)
    MatchingDatasetToNonMatchingAttributesMapper mdsMapper;
    @Mock(lenient = true)
    InstantValidator instantValidator;
    @Mock(lenient = true)
    ConditionsValidator conditionsValidator;
    @Mock(lenient = true)
    LevelOfAssuranceValidator levelOfAssuranceValidator;
    @Mock(lenient = true)
    EidasMetadataResolverRepository metadataResolverRepository;
    @Mock(lenient = true)
    SignatureValidatorFactory signatureValidatorFactory;
    @Mock(lenient = true)
    SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;
    @Mock(lenient = true)
    UserIdHashFactory userIdHashFactory;

    protected abstract void shouldCallValidatorsCorrectly();

    @Test
    public void shouldTranslateEidasAssertion() {
        Assertion eidasAssertion = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, "requestId").buildUnencrypted();
        assertionService.translateSuccessResponse(singletonList(eidasAssertion), "requestId", LEVEL_2, null);

        verify(eidasMatchingDatasetUnmarshaller, times(1)).fromAssertion(eidasAssertion);
    }

    @Test
    public void shouldCorrectlyExtractLevelOfAssurance() {
        Assertion eidasAssertion = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, "requestId").buildUnencrypted();

        LevelOfAssurance loa = assertionService.extractLevelOfAssuranceFrom(eidasAssertion);

        assertThat(loa).isEqualTo(LEVEL_2);
    }

    @Test
    public void expectedHashContainedInResponseBodyWhenUserIdFactoryIsCalledOnce() {
        String requestId = "requestId";
        String expectedHashed = "a5fbea969c3837a712cbe9e188804796828f369106478e623a436fa07e8fd298";
        TestTranslatedNonMatchingResponseBody expectedNonMatchingResponseBody = new TestTranslatedNonMatchingResponseBody(NonMatchingScenario.IDENTITY_VERIFIED, expectedHashed, LEVEL_2, null);

        Assertion eidasAssertion = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, requestId).buildUnencrypted();

        final String nameId = eidasAssertion.getSubject().getNameID().getValue();
        final String issuerId = eidasAssertion.getIssuer().getValue();

        when(userIdHashFactory.hashId(eq(issuerId), eq(nameId), eq(Optional.of(AuthnContext.LEVEL_2))))
                .thenReturn(expectedHashed);

        TranslatedNonMatchingResponseBody responseBody = assertionService.translateSuccessResponse(List.of(eidasAssertion), "requestId", LEVEL_2, "default-entity-id");

        verify(userIdHashFactory, times(1)).hashId(issuerId,nameId, Optional.of(AuthnContext.LEVEL_2));
        assertThat(responseBody.toString()).contains(expectedNonMatchingResponseBody.getPid());
    }

    @Test
    public void shouldThrowAnExceptionIfMultipleAssertionsReceived() {
        Assertion eidasAssertion1 = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, "requestId").buildUnencrypted();
        Assertion eidasAssertion2 = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, "requestId").buildUnencrypted();
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> assertionService.translateSuccessResponse(asList(eidasAssertion1, eidasAssertion2), "requestId", LEVEL_2, null));
    }

    @Test
    public void shouldCorrectlyIdentifyCountryAssertions() {
        List<String> resolverEntityIds = asList("ID1", "ID2");
        when(metadataResolverRepository.getResolverEntityIds()).thenReturn(resolverEntityIds);

        Assertion countryAssertion = anAssertion().withIssuer(anIssuer().withIssuerId("ID1").build()).buildUnencrypted();
        Assertion idpAssertion = anAssertion().withIssuer(anIssuer().withIssuerId("ID3").build()).buildUnencrypted();

        assertThat(assertionService.isCountryAssertion(countryAssertion)).isTrue();
        assertThat(assertionService.isCountryAssertion(idpAssertion)).isFalse();
    }


    protected static AssertionBuilder anAssertionWithAuthnStatement(String authnContext, String inResponseTo) {
        return anAssertion()
                .addAuthnStatement(
                        anAuthnStatement()
                                .withAuthnContext(
                                        anAuthnContext()
                                                .withAuthnContextClassRef(
                                                        anAuthnContextClassRef()
                                                                .withAuthnContextClasRefValue(authnContext)
                                                                .build())
                                                .build())
                                .build())
                .withSubject(anAssertionSubject(inResponseTo))
                .withIssuer(anIssuer().withIssuerId(STUB_COUNTRY_ONE).build())
                .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().build()).build());
    }

    private static Subject anAssertionSubject(final String inResponseTo) {
        return aSubject()
                .withSubjectConfirmation(
                        aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withNotOnOrAfter(Instant.now())
                                                .withInResponseTo(inResponseTo)
                                                .build()
                                ).build()
                ).build();
    }

    EidasAssertionTranslatorValidatorContainer getEidasAssertionTranslatorValidatorContainer() {
        return new EidasAssertionTranslatorValidatorContainer(
                subjectValidator,
                instantValidator,
                conditionsValidator,
                levelOfAssuranceValidator
        );
    }
}
