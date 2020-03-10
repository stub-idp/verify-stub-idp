package stubidp.saml.serializers.serializers;

import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.codec.EncodingException;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.apache.commons.codec.binary.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.slf4j.event.Level;
import org.w3c.dom.Element;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;

import java.util.function.Function;

public class XmlObjectToBase64EncodedStringTransformer<TInput extends XMLObject> implements Function<TInput, String> {

    @Override
    public String apply(XMLObject signableXMLObject) {
        Element signedElement = marshallToElement(signableXMLObject);
        String node = SerializeSupport.nodeToString(signedElement);
        try {
            return Base64Support.encode(StringUtils.getBytesUtf8(node), Base64Support.UNCHUNKED);
        } catch (EncodingException e) {
            throw new SamlTransformationErrorException(e.getMessage(), Level.ERROR);
        }
    }

    private static Element marshallToElement(XMLObject rootObject) {
        return new XmlObjectToElementTransformer<>().apply(rootObject);
    }

}
