package stubidp.saml.serializers.serializers;

import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.opensaml.core.xml.XMLObject;
import org.w3c.dom.Element;

import java.util.Base64;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

public class XmlObjectToBase64EncodedStringTransformer<TInput extends XMLObject> implements Function<TInput, String> {

    @Override
    public String apply(XMLObject signableXMLObject) {
        Element signedElement = marshallToElement(signableXMLObject);
        String node = SerializeSupport.nodeToString(signedElement);
        return Base64.getEncoder().encodeToString(node.getBytes(UTF_8));
    }

    private static Element marshallToElement(XMLObject rootObject) {
        return new XmlObjectToElementTransformer<>().apply(rootObject);
    }

}
