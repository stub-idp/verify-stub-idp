package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import stubidp.saml.domain.matching.assertions.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.services.EidasAssertionTranslator;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.extensions.extensions.EidasAuthnContext.EIDAS_LOA_HIGH;
import static stubidp.test.devpki.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

@ExtendWith(MockitoExtension.class)
public class EidasAssertionTranslatorTest extends BaseEidasAssertionTranslatorTestBase {

    @BeforeEach
    public void setUp() throws Exception {
        assertionService = new EidasAssertionTranslator(
                getEidasAssertionTranslatorValidatorContainer(),
                eidasMatchingDatasetUnmarshaller,
                mdsMapper,
                metadataResolverRepository,
                signatureValidatorFactory,
                singletonList(HUB_CONNECTOR_ENTITY_ID),
                userIdHashFactory);
        doNothing().when(instantValidator).validate(any(), any());
        doNothing().when(subjectValidator).validate(any(), any());
        doNothing().when(conditionsValidator).validate(any(), any());
        doNothing().when(levelOfAssuranceValidator).validate(any(), any());
        when(metadataResolverRepository.getResolverEntityIds()).thenReturn(singletonList(STUB_COUNTRY_ONE));
        ExplicitKeySignatureTrustEngine mock = mock(ExplicitKeySignatureTrustEngine.class);
        when(metadataResolverRepository.getSignatureTrustEngine(same(STUB_COUNTRY_ONE))).thenReturn(Optional.of(mock));
        when(signatureValidatorFactory.getSignatureValidator(same(mock))).thenReturn(samlAssertionsSignatureValidator);
        when(samlAssertionsSignatureValidator.validate(any(), any())).thenReturn(null);
        when(mdsMapper.mapToNonMatchingAttributes(any())).thenReturn(mock(NonMatchingAttributes.class));
    }

    @Override
    @Test
    public void shouldCallValidatorsCorrectly() {
        List<Assertion> assertions = singletonList(
                anAssertionWithAuthnStatement(EIDAS_LOA_HIGH, "requestId").buildUnencrypted());

        assertionService.translateSuccessResponse(assertions, "requestId", LEVEL_2, null);
        verify(instantValidator, times(1)).validate(any(), any());
        verify(subjectValidator, times(1)).validate(any(), any());
        verify(conditionsValidator, times(1)).validate(any(), any());
        verify(levelOfAssuranceValidator, times(1)).validate(any(), any());
        verify(samlAssertionsSignatureValidator, times(1)).validate(any(), any());
    }
}
