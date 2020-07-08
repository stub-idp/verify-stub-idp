package stubidp.saml.serializers.deserializers;

import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.serializers.deserializers.parser.SamlObjectParser;

import static stubidp.saml.serializers.errors.SamlTransformationErrorFactory.unableToDeserializeStringToOpenSaml;

public class OpenSamlXMLObjectUnmarshaller<TOutput extends XMLObject> {

    private final SamlObjectParser samlObjectParser;

    public OpenSamlXMLObjectUnmarshaller(SamlObjectParser samlObjectParser) {
        this.samlObjectParser = samlObjectParser;
    }

    public TOutput fromString(String input) {
        try {
            return samlObjectParser.getSamlObject(input);
        } catch (UnmarshallingException | XMLParserException  e) {
            SamlValidationSpecificationFailure failure = unableToDeserializeStringToOpenSaml(input);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), e, failure.getLogLevel());
        }
    }
}
