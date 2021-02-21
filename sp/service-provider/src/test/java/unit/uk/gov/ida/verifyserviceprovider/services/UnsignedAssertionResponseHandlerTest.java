package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.eidas.CountrySamlResponse;
import stubidp.saml.extensions.extensions.eidas.EncryptedAssertionKeys;
import stubidp.saml.extensions.extensions.eidas.impl.CountrySamlResponseBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.EncryptedAssertionKeysBuilder;
import stubidp.saml.metadata.EidasValidatorFactory;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.SecretKeyDecryptorFactory;
import stubidp.saml.security.exception.SamlFailedToDecryptException;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.services.UnsignedAssertionsResponseHandler;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AssertionBuilder.anEidasAssertion;
import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.test.builders.ResponseBuilder.aResponse;

@ExtendWith(MockitoExtension.class)
public class UnsignedAssertionResponseHandlerTest extends OpenSAMLRunner {

    @Mock
    private EidasValidatorFactory eidasValidatorFactory;

    @Mock
    private StringToOpenSamlObjectTransformer<Response> stringToResponseTransformer;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private SecretKeyDecryptorFactory secretKeyDecryptorFactory;

    @Mock
    private Decrypter decrypter;

    @Mock
    private SamlAssertionsSignatureValidator hubAssertionSignatureValidator;

    private UnsignedAssertionsResponseHandler handler;
    private final List<String> singleKeyList = Collections.singletonList("aKey");
    private final String samlString = "eidasSaml";
    private final String inResponseTo = "inResponseTo";
    private ValidatedResponse validatedResponse;
    private Response eidasResponse;

    @BeforeEach
    public void setUp() throws Exception {
        handler = new UnsignedAssertionsResponseHandler(
                eidasValidatorFactory,
                stringToResponseTransformer,
                instantValidator,
                secretKeyDecryptorFactory,
                getEncryptionAlgorithmValidator(),
                hubAssertionSignatureValidator
        );
        eidasResponse = createEidasResponse();
        validatedResponse = new ValidatedResponse(eidasResponse);
    }

    @Test
    public void getValidatedResponseShouldValidateResponse() {
        List<Assertion> eidasSamlAssertion = Collections.singletonList(anEidasSamlAssertion(singleKeyList));

        when(hubAssertionSignatureValidator.validate(eidasSamlAssertion, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(new ValidatedAssertions(eidasSamlAssertion));
        when(stringToResponseTransformer.apply(samlString)).thenReturn(eidasResponse);
        when(eidasValidatorFactory.getValidatedResponse(eidasResponse)).thenReturn(validatedResponse);

        handler.getValidatedResponse(eidasSamlAssertion, inResponseTo);

        verify(hubAssertionSignatureValidator).validate(eidasSamlAssertion, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        verify(stringToResponseTransformer).apply(samlString);
        verify(eidasValidatorFactory).getValidatedResponse(eidasResponse);
        verify(instantValidator).validate(validatedResponse.getIssueInstant(), "Response IssueInstant");
    }

    @Test
    public void getValidatedResponseShouldThrowIfInResponseToIsNotExpected() {
        List<Assertion> eidasSamlAssertion = Collections.singletonList(anEidasSamlAssertion(singleKeyList));

        when(hubAssertionSignatureValidator.validate(eidasSamlAssertion, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(new ValidatedAssertions(eidasSamlAssertion));
        when(stringToResponseTransformer.apply(samlString)).thenReturn(eidasResponse);
        when(eidasValidatorFactory.getValidatedResponse(eidasResponse)).thenReturn(validatedResponse);

        assertThrows(SamlResponseValidationException.class,
                () -> handler.getValidatedResponse(eidasSamlAssertion, "thisIsNotTheResponseIdYouAreLookingFor"));
    }

    @Test
    public void decryptAssertionShouldDecryptWithCorrectKey() throws Exception {
        Assertion eidasSamlAssertion = anEidasSamlAssertion(singleKeyList);
        Assertion expectedAssertion = anEidasAssertion().buildUnencrypted();

        when(secretKeyDecryptorFactory.createDecrypter(singleKeyList.get(0))).thenReturn(decrypter);
        when(decrypter.decrypt(any(EncryptedAssertion.class))).thenReturn(expectedAssertion);
        List<Assertion> assertions = handler.decryptAssertion(validatedResponse, eidasSamlAssertion);

        assertThat(assertions.size()).isEqualTo(1);
        assertThat(assertions.get(0)).isEqualTo(expectedAssertion);
    }

    @Test
    public void decryptAssertionShouldTryMultipleKeys() throws Exception {
        Assertion eidasSamlAssertion = anEidasSamlAssertion(Arrays.asList("wrongKey", "anotherWrongKey", "theCorretKey"));
        Assertion expectedAssertion = anEidasAssertion().buildUnencrypted();

        when(secretKeyDecryptorFactory.createDecrypter("theCorretKey")).thenReturn(decrypter);
        when(decrypter.decrypt(any(EncryptedAssertion.class))).thenReturn(expectedAssertion);
        List<Assertion> assertions = handler.decryptAssertion(validatedResponse, eidasSamlAssertion);

        verify(secretKeyDecryptorFactory, times(3)).createDecrypter(any());

        assertThat(assertions.size()).isEqualTo(1);
        assertThat(assertions.get(0)).isEqualTo(expectedAssertion);
    }

    @Test
    public void decryptAssertionShouldThrowIfNoKeysCanDecrypt() throws Exception {
        Assertion eidasSamlAssertion = anEidasSamlAssertion(Arrays.asList("wrongKey", "anotherWrongKey", "ohNo!"));

        assertThrows(SamlFailedToDecryptException.class,
                () -> handler.decryptAssertion(validatedResponse, eidasSamlAssertion));

        verify(secretKeyDecryptorFactory, times(3)).createDecrypter(any());
    }

    @Test
    public void decryptAssertionShouldThrowIfWrongEncryptionAlgorithmUsed() throws Exception {
        handler = new UnsignedAssertionsResponseHandler(
                eidasValidatorFactory,
                stringToResponseTransformer,
                instantValidator,
                secretKeyDecryptorFactory,
                new EncryptionAlgorithmValidator(
                        Set.of(
                                EncryptionConstants.ALGO_ID_BLOCKCIPHER_TRIPLEDES
                        ),
                        Set.of(
                                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP
                        )
                ),
                hubAssertionSignatureValidator
        );
        Assertion eidasSamlAssertion = anEidasSamlAssertion(singleKeyList);

        assertThrows(SamlFailedToDecryptException.class,
                () -> handler.decryptAssertion(validatedResponse, eidasSamlAssertion));
    }

    private Assertion anEidasSamlAssertion(List<String> keys) {
        return anAssertion()
                .addAttributeStatement(
                        anAttributeStatement()
                                .addAttribute(createCountrySamlResponseAttribute(samlString))
                                .addAttribute(createEncryptedAssertionKeysAttribute(keys))
                                .build())
                .buildUnencrypted();
    }

    private Attribute createCountrySamlResponseAttribute(String countrySaml) {
        CountrySamlResponse attributeValue = new CountrySamlResponseBuilder().buildObject();
        attributeValue.setValue(countrySaml);

        Attribute attribute = (Attribute) XMLObjectSupport.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);
        attribute.setFriendlyName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.FRIENDLY_NAME);
        attribute.setNameFormat(Attribute.URI_REFERENCE);

        attribute.getAttributeValues().add(attributeValue);
        return attribute;
    }

    private Attribute createEncryptedAssertionKeysAttribute(List<String> keys) {
        List<EncryptedAssertionKeys> assertionKeysValues = new ArrayList<>();
        for (String key : keys) {
            EncryptedAssertionKeys attributeValue = new EncryptedAssertionKeysBuilder().buildObject();
            attributeValue.setValue(key);
            assertionKeysValues.add(attributeValue);
        }

        Attribute attribute = (Attribute) XMLObjectSupport.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.NAME);
        attribute.setFriendlyName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.FRIENDLY_NAME);
        attribute.setNameFormat(Attribute.URI_REFERENCE);

        attribute.getAttributeValues().addAll(assertionKeysValues);
        return attribute;
    }

    private Response createEidasResponse() throws Exception {
        return aResponse()
            .addEncryptedAssertion(
                    anEidasAssertion().build())
            .withInResponseTo(inResponseTo)
            .build();
    }

    private EncryptionAlgorithmValidator getEncryptionAlgorithmValidator() {
        return new EncryptionAlgorithmValidator(
                Set.of(
                        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128
                ),
                Set.of(
                        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP
                )
        );
    }
}
