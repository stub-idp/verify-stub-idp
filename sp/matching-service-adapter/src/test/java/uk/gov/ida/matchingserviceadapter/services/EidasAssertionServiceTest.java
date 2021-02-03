package uk.gov.ida.matchingserviceadapter.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.metadata.MetadataResolverRepository;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.SigningCredentialFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.transformers.EidasMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.transformers.EidasUnsignedMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.transformers.inbound.Cycle3DatasetFactory;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import stubidp.test.devpki.TestEntityIds;
import stubidp.test.security.HardCodedKeyStore;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.validators.CountryConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.domain.assertions.AuthnContext.LEVEL_2;
import static stubidp.saml.test.builders.AssertionBuilder.aCycle3DatasetAssertion;
import static stubidp.saml.test.builders.AssertionBuilder.anEidasAssertion;
import static stubidp.test.devpki.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.matchingserviceadapter.services.AttributeQueryServiceTest.anEidasSignature;

@ExtendWith(MockitoExtension.class)
public class EidasAssertionServiceTest extends OpenSAMLRunner {

    private EidasAssertionService eidasAssertionService;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private SubjectValidator subjectValidator;

    @Mock
    private CountryConditionsValidator conditionsValidator;

    @Mock
    private SamlAssertionsSignatureValidator hubSignatureValidator;

    @Mock
    private MetadataResolverRepository metadataResolverRepository;

    @Mock
    private EidasUnsignedMatchingDatasetUnmarshaller eidasUnsignedMatchingDatasetUnmarshaller;

    @BeforeEach
    public void setUp() throws Exception {
        eidasAssertionService = new EidasAssertionService(
                instantValidator,
                subjectValidator,
                conditionsValidator,
                hubSignatureValidator,
                new Cycle3DatasetFactory(),
                metadataResolverRepository,
                Collections.singletonList(HUB_CONNECTOR_ENTITY_ID),
                HUB_ENTITY_ID,
                new EidasMatchingDatasetUnmarshaller(),
                eidasUnsignedMatchingDatasetUnmarshaller
        );
        when(metadataResolverRepository.getResolverEntityIds()).thenReturn(Collections.singletonList(STUB_COUNTRY_ONE));
    }

    @Test
    public void shouldTranslateEidasAssertion() {
        Assertion eidasAssertion = anEidasAssertion().buildUnencrypted();
        Assertion cycle3Assertion = aCycle3DatasetAssertion("NI", "123456").buildUnencrypted();
        List<Assertion> assertions = asList( eidasAssertion, cycle3Assertion);
        AssertionData assertionData = eidasAssertionService.translate(assertions);
        assertThat(assertionData.getLevelOfAssurance()).isEqualTo(LEVEL_2);
        assertThat(assertionData.getMatchingDatasetIssuer()).isEqualTo(STUB_COUNTRY_ONE);
        assertThat(assertionData.getCycle3Data().get().getAttributes().get("NI")).isEqualTo("123456");
        assertThat(assertionData.getMatchingDataset().getFirstNames().get(0).getValue()).isEqualTo("Joe");
        assertThat(assertionData.getMatchingDataset().getSurnames().get(0).getValue()).isEqualTo("Bloggs");
        assertThat(assertionData.getMatchingDataset().getPersonalId()).isEqualTo("JB12345");
        assertThat(assertionData.getMatchingDataset().getDateOfBirths().get(0).getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    public void shouldNotAttemptToValidateSignatureOnUnsignedAssertion() {
        doNothing().when(instantValidator).validate(any(), any());
        doNothing().when(subjectValidator).validate(any(), any());
        doNothing().when(conditionsValidator).validate(any(), any());
        Attribute unsignedAssertions = new OpenSamlXmlObjectFactory().createAttribute();
        unsignedAssertions.setName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);

        Assertion eidasUnsignedAssertion = anEidasAssertion()
                .withSignature(null)
                .addAttributeStatement(
                        anAttributeStatement()
                                .addAttribute(unsignedAssertions).build())
                .buildUnencrypted();
        eidasAssertionService.validate("bob", Collections.singletonList(eidasUnsignedAssertion));
        verify(metadataResolverRepository, never()).getSignatureTrustEngine(any(String.class));
    }

    @Test
    public void shouldNotAllowSignedAssertionContainingEidasSamlResponseAttribute() {
        doNothing().when(instantValidator).validate(any(), any());
        doNothing().when(subjectValidator).validate(any(), any());
        doNothing().when(conditionsValidator).validate(any(), any());
        Attribute unsignedAssertions = new OpenSamlXmlObjectFactory().createAttribute();
        unsignedAssertions.setName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);
        Assertion eidasUnsignedAssertion = anEidasAssertion()
                .addAttributeStatement(
                        anAttributeStatement()
                                .addAttribute(unsignedAssertions).build())
                .buildUnencrypted();
        assertThrows(SamlResponseValidationException.class,
                () -> eidasAssertionService.validate("bob", Collections.singletonList(eidasUnsignedAssertion)));
    }

    @Test
    public void shouldValidateSignatureOnSignedAssertion() {
        doNothing().when(instantValidator).validate(any(), any());
        doNothing().when(subjectValidator).validate(any(), any());
        doNothing().when(conditionsValidator).validate(any(), any());
        ExplicitKeySignatureTrustEngine trustEngine = getExplicitKeySignatureTrustEngine();
        when(metadataResolverRepository.getSignatureTrustEngine(TestEntityIds.STUB_COUNTRY_ONE)).thenReturn(Optional.of(trustEngine));
        Assertion eidasAssertion = AttributeQueryServiceTest.anEidasAssertion("requestId", TestEntityIds.STUB_COUNTRY_ONE, anEidasSignature());
        eidasAssertionService.validate("bob", Arrays.asList(eidasAssertion));
        verify(metadataResolverRepository).getSignatureTrustEngine(TestEntityIds.STUB_COUNTRY_ONE);
    }

    @Test
    public void shouldUseMatchingUnsignedDatasetUnmarshallerForUnsignedAssertions() {
        Attribute unsignedAssertions = new OpenSamlXmlObjectFactory().createAttribute();
        unsignedAssertions.setName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);

        List<Assertion> assertions = Collections.singletonList(anEidasAssertion().addAttributeStatement(
                anAttributeStatement()
                        .addAttribute(unsignedAssertions)
                        .build())
                .buildUnencrypted());
        eidasAssertionService.translate(assertions);
        verify(eidasUnsignedMatchingDatasetUnmarshaller).fromAssertion(any(Assertion.class));
    }

    @Test
    public void shouldUseEidasMatchingDatasetUnmarshallerForSignedAssertions() {
        EidasMatchingDatasetUnmarshaller eidasMatchingDatasetUnmarshaller = mock(EidasMatchingDatasetUnmarshaller.class);
        eidasAssertionService = new EidasAssertionService(
                instantValidator,
                subjectValidator,
                conditionsValidator,
                hubSignatureValidator,
                new Cycle3DatasetFactory(),
                metadataResolverRepository,
                Collections.singletonList(HUB_CONNECTOR_ENTITY_ID),
                HUB_ENTITY_ID,
                eidasMatchingDatasetUnmarshaller,
                eidasUnsignedMatchingDatasetUnmarshaller
        );

        List<Assertion> assertions = Collections.singletonList(anEidasAssertion().buildUnencrypted());
        eidasAssertionService.translate(assertions);
        verify(eidasMatchingDatasetUnmarshaller).fromAssertion(any(Assertion.class));
    }

    private ExplicitKeySignatureTrustEngine getExplicitKeySignatureTrustEngine() {
        List<Credential> credentials = new SigningCredentialFactory(new HardCodedKeyStore(TestEntityIds.HUB_ENTITY_ID)).getVerifyingCredentials(TestEntityIds.STUB_COUNTRY_ONE);
        CredentialResolver credResolver = new StaticCredentialResolver(credentials);
        KeyInfoCredentialResolver kiResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
        return new ExplicitKeySignatureTrustEngine(credResolver, kiResolver);
    }
}

