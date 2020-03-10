package stubidp.saml.serializers.deserializers.validators;

import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.codec.DecodingException;
import org.apache.commons.codec.binary.StringUtils;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import java.util.regex.Pattern;

import static stubidp.saml.serializers.errors.SamlTransformationErrorFactory.invalidBase64Encoding;

public class Base64StringDecoder {

    public String decode(String input) {
        final String withoutWhitespace = input.replaceAll("\\s", "");
        if (Pattern.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$", withoutWhitespace)) {
            try {
                return StringUtils.newStringUtf8(Base64Support.decode(input));
            } catch (DecodingException e) {
                // fall through and throw a SamlTransformationErrorException exception
            }
        }
        SamlValidationSpecificationFailure failure = invalidBase64Encoding(input);
        throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
    }

}
