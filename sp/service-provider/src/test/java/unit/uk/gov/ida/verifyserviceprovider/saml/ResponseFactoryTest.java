package unit.uk.gov.ida.verifyserviceprovider.saml;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;

import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ResponseFactoryTest {

    private final StringToOpenSamlObjectTransformer<Response> stringToResponseTransformer = ResponseFactory.createStringToResponseTransformer();

    @Test
    public void createStringToResponseTransformerShouldNotAllowNullSamlResponse() {
        assertThatExceptionOfType(SamlTransformationErrorException.class)
                .isThrownBy(() -> stringToResponseTransformer.apply(null))
                .withMessageContaining("SAML Validation Specification: Missing SAML message.");
    }

    @Test
    public void createStringToResponseTransformerMustContainBase64EncodedSamlResponse() {
        assertThatExceptionOfType(SamlTransformationErrorException.class)
                .isThrownBy(() -> stringToResponseTransformer.apply("not-encoded-string"))
                .withMessageContaining("SAML Validation Specification: SAML is not base64 encoded in message body. start> not-encoded-string <end");
    }

    @Test
    public void createStringToResponseTransformerShouldNotAllowTooLongSamlMessages() {
        String longString = String.join("", Collections.nCopies(50001, "a"));
        String longBase64EncodedString = Base64.getEncoder().encodeToString(longString.getBytes());

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> stringToResponseTransformer.apply(longBase64EncodedString))
                .withMessage("SAML Response is too long.");
    }
}