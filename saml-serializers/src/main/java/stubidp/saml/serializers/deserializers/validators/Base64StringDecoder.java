package stubidp.saml.serializers.deserializers.validators;

import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import java.util.Base64;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static stubidp.saml.serializers.errors.SamlTransformationErrorFactory.invalidBase64Encoding;

public class Base64StringDecoder {

    public String decode(String input) {
        final String withoutWhitespace = input.replaceAll("\\s", "");
        if (Pattern.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$", withoutWhitespace)) {
                return new String(Base64.getMimeDecoder().decode(input), UTF_8);
        } else {
            SamlValidationSpecificationFailure failure = invalidBase64Encoding(input);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }
}
