package stubidp.saml.serializers.deserializers;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.w3c.dom.Element;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.serializers.deserializers.parser.SamlObjectParser;

import java.util.function.Function;

import static stubidp.saml.serializers.errors.SamlTransformationErrorFactory.unableToUnmarshallElementToOpenSaml;

public class ElementToOpenSamlXMLObjectTransformer<TOutput extends XMLObject> implements Function<Element,TOutput> {
    private final SamlObjectParser samlObjectParser;

    public ElementToOpenSamlXMLObjectTransformer(SamlObjectParser samlObjectParser) {
        this.samlObjectParser = samlObjectParser;
    }

    @Override
    public TOutput apply(Element input) {
        try {
            return samlObjectParser.getSamlObject(input);
        } catch (UnmarshallingException e) {
            SamlValidationSpecificationFailure failure = unableToUnmarshallElementToOpenSaml(input.getLocalName());
            throw new SamlTransformationErrorException(failure.getErrorMessage(), e, failure.getLogLevel());
        }
    }

}
