package stubidp.saml.utils.core.transformers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.slf4j.event.Level;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.eidas.CountrySamlResponse;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.extensions.extensions.eidas.EncryptedAssertionKeys;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.metadata.EidasValidatorFactory;
import stubidp.saml.security.SecretKeyDecryptorFactory;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.utils.core.validation.assertion.ExceptionThrowingValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EidasUnsignedMatchingDatasetUnmarshallerTest {

    @InjectMocks
    private EidasUnsignedMatchingDatasetUnmarshaller unmarshaller;

    @Mock
    private SecretKeyDecryptorFactory secretKeyDecryptorFactory;

    @Mock
    private EidasValidatorFactory eidasValidatorFactory;

    @Mock
    private StringToOpenSamlObjectTransformer<Response> stringtoOpenSamlObjectTransformer;

    @Mock
    private Assertion unsignedAssertion;

    @Mock
    private Assertion eidasAssertion;

    @Mock
    private AttributeStatement unsignedAttributeStatement;

    @Mock
    private AttributeStatement eidasAttributeStatement;

    @Mock
    private Attribute attributeEncryptionKeys;

    @Mock
    private Attribute firstName;

    @Mock
    private Attribute pid;

    @Mock
    private Attribute attributeEidasResponse;

    @Mock
    private EncryptedAssertionKeys attributeValueEncryptionKeys;

    @Mock
    private CountrySamlResponse attributeValueEidasResponse;

    @Mock
    private PersonIdentifier personIdentifierValue;

    @Mock
    private CurrentGivenName firstNameValue;

    @Mock
    private Response response;

    @Mock
    private Decrypter decrypter;

    @Mock
    private EncryptedAssertion encryptedAssertion;

    @Mock
    private EncryptedData encryptedData;

    @Mock
    private ValidatedResponse validatedResponse;

    @Mock
    private ExceptionThrowingValidator<Assertion> validator;

    @Test
    void whenAssertionHasNoAttributeStatementsThenMatchingDatasetIsNull() {
        MatchingDataset matchingDataset = unmarshaller.fromAssertion(unsignedAssertion);
        assertThat(matchingDataset).isNull();
        verify(unsignedAssertion).getAttributeStatements();
        verifyNoMoreInteractions(stringtoOpenSamlObjectTransformer, secretKeyDecryptorFactory);
    }

    @Test
    void whenNoEncryptionKeysAttributeThenMatchingDatasetIsNull() {
        when(unsignedAssertion.getAttributeStatements()).thenReturn(List.of(unsignedAttributeStatement));
        when(unsignedAttributeStatement.getAttributes()).thenReturn(List.of(attributeEncryptionKeys, attributeEidasResponse));
        when(attributeEncryptionKeys.getName()).thenReturn("no matching key");
        when(attributeEidasResponse.getName()).thenReturn(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);
        MatchingDataset matchingDataset = unmarshaller.fromAssertion(unsignedAssertion);
        assertThat(matchingDataset).isNull();
        verify(attributeEncryptionKeys, times(2)).getName();
        verify(attributeEidasResponse, times(2)).getName();
        verify(attributeEidasResponse).getAttributeValues();
        verifyNoMoreInteractions(attributeEncryptionKeys, attributeEidasResponse);
        verifyNoMoreInteractions(stringtoOpenSamlObjectTransformer, secretKeyDecryptorFactory);
    }

    @Test
    void whenNoEidasResponseAttributeThenMatchingDatasetIsNull() {
        when(unsignedAssertion.getAttributeStatements()).thenReturn(List.of(unsignedAttributeStatement));
        when(unsignedAttributeStatement.getAttributes()).thenReturn(List.of(attributeEncryptionKeys, attributeEidasResponse));
        when(attributeEncryptionKeys.getName()).thenReturn(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.NAME);
        when(attributeEidasResponse.getName()).thenReturn("no matching key");
        MatchingDataset matchingDataset = unmarshaller.fromAssertion(unsignedAssertion);
        assertThat(matchingDataset).isNull();
        verify(attributeEncryptionKeys, times(2)).getName();
        verify(attributeEidasResponse, times(2)).getName();
        verify(attributeEncryptionKeys).getAttributeValues();
        verifyNoMoreInteractions(attributeEncryptionKeys, attributeEidasResponse);
        verifyNoMoreInteractions(stringtoOpenSamlObjectTransformer, secretKeyDecryptorFactory);
    }

    @Test
    void shouldDelegateToMatchingDatasetUnmarshallerToUnpackEidasAssertions() throws Exception {

        when(unsignedAssertion.getAttributeStatements()).thenReturn(List.of(unsignedAttributeStatement));
        when(eidasAssertion.getAttributeStatements()).thenReturn(List.of(eidasAttributeStatement));
        when(unsignedAttributeStatement.getAttributes()).thenReturn(List.of(attributeEncryptionKeys, attributeEidasResponse));
        when(eidasAttributeStatement.getAttributes()).thenReturn(List.of(firstName, pid));
        when(attributeEncryptionKeys.getName()).thenReturn(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.NAME);
        when(attributeEidasResponse.getName()).thenReturn(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);
        when(firstName.getName()).thenReturn(IdaConstants.Eidas_Attributes.FirstName.NAME);
        when(pid.getName()).thenReturn(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME);
        when(attributeEncryptionKeys.getAttributeValues()).thenReturn(List.of(attributeValueEncryptionKeys));
        when(attributeEidasResponse.getAttributeValues()).thenReturn(List.of(attributeValueEidasResponse));
        when(firstName.getAttributeValues()).thenReturn(List.of(firstNameValue));
        when(firstNameValue.isLatinScript()).thenReturn(true);
        when(pid.getAttributeValues()).thenReturn(List.of(personIdentifierValue));
        when(personIdentifierValue.getPersonIdentifier()).thenReturn("It's a me, Mario");
        when(attributeValueEncryptionKeys.getValue()).thenReturn("an encrypted  key string");
        when(attributeValueEidasResponse.getValue()).thenReturn("an eidas response string");
        when(stringtoOpenSamlObjectTransformer.apply("an eidas response string")).thenReturn(response);
        when(secretKeyDecryptorFactory.createDecrypter("an encrypted  key string")).thenReturn(decrypter);
        when(eidasValidatorFactory.getValidatedResponse(response)).thenReturn(validatedResponse);
        when(validatedResponse.getEncryptedAssertions()).thenReturn(List.of(encryptedAssertion));
        when(encryptedAssertion.getEncryptedData()).thenReturn(encryptedData);
        when(decrypter.decryptData(encryptedData)).thenReturn(eidasAssertion);

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(unsignedAssertion);

        assertThat(matchingDataset).isNotNull();
        verify(firstNameValue).getFirstName();
        verify(personIdentifierValue).getPersonIdentifier();
        verify(stringtoOpenSamlObjectTransformer).apply("an eidas response string");
        verify(attributeValueEncryptionKeys).getValue();
        verify(attributeValueEidasResponse).getValue();
        verify(validator).apply(eidasAssertion);
    }

    @Test
    void shouldNotProvideAMatchingDataSetWhenResponseSignatureValidationThrowsException() {
        when(unsignedAssertion.getAttributeStatements()).thenReturn(List.of(unsignedAttributeStatement));
        when(unsignedAttributeStatement.getAttributes()).thenReturn(List.of(attributeEncryptionKeys, attributeEidasResponse));
        when(attributeEncryptionKeys.getName()).thenReturn(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.NAME);
        when(attributeEncryptionKeys.getAttributeValues()).thenReturn(List.of(attributeValueEncryptionKeys));
        when(attributeEidasResponse.getAttributeValues()).thenReturn(List.of(attributeValueEidasResponse));
        when(attributeEidasResponse.getName()).thenReturn(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);
        when(attributeValueEncryptionKeys.getValue()).thenReturn("an encrypted  key string");
        when(attributeValueEidasResponse.getValue()).thenReturn("an eidas response string");
        when(stringtoOpenSamlObjectTransformer.apply("an eidas response string")).thenReturn(response);
        when(eidasValidatorFactory.getValidatedResponse(response)).thenThrow(new SamlTransformationErrorException("an error message", Level.ERROR));
        MatchingDataset matchingDataset = unmarshaller.fromAssertion(unsignedAssertion);
        assertThat(matchingDataset).isNull();
        verify(stringtoOpenSamlObjectTransformer).apply("an eidas response string");
        verifyNoMoreInteractions(secretKeyDecryptorFactory, decrypter);
    }

    @Test
    void shouldNotProvideAMatchingDataSetWhenValidatorOfEidasAssertionThrowsException() throws Exception {
        when(unsignedAssertion.getAttributeStatements()).thenReturn(List.of(unsignedAttributeStatement));
        when(unsignedAttributeStatement.getAttributes()).thenReturn(List.of(attributeEncryptionKeys, attributeEidasResponse));
        when(attributeEncryptionKeys.getName()).thenReturn(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.NAME);
        when(attributeEncryptionKeys.getAttributeValues()).thenReturn(List.of(attributeValueEncryptionKeys));
        when(attributeEidasResponse.getAttributeValues()).thenReturn(List.of(attributeValueEidasResponse));
        when(attributeEidasResponse.getName()).thenReturn(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);
        when(attributeValueEncryptionKeys.getValue()).thenReturn("an encrypted  key string");
        when(attributeValueEidasResponse.getValue()).thenReturn("an eidas response string");
        when(stringtoOpenSamlObjectTransformer.apply("an eidas response string")).thenReturn(response);
        when(secretKeyDecryptorFactory.createDecrypter("an encrypted  key string")).thenReturn(decrypter);
        when(eidasValidatorFactory.getValidatedResponse(response)).thenReturn(validatedResponse);
        when(validatedResponse.getEncryptedAssertions()).thenReturn(List.of(encryptedAssertion));
        when(encryptedAssertion.getEncryptedData()).thenReturn(encryptedData);
        when(decrypter.decryptData(encryptedData)).thenReturn(eidasAssertion);
        doThrow(new ExceptionThrowingValidator.ValidationException("", new RuntimeException())).when(validator).apply(eidasAssertion);
        MatchingDataset matchingDataset = unmarshaller.fromAssertion(unsignedAssertion);
        assertThat(matchingDataset).isNull();
        verify(decrypter).decryptData(encryptedData);
        verify(validator).apply(eidasAssertion);
    }

} 