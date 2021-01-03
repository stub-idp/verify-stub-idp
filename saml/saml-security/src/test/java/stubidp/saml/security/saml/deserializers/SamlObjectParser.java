package stubidp.saml.security.saml.deserializers;

import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import stubidp.utils.common.xml.XmlUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class SamlObjectParser {

    public <T extends XMLObject> T getSamlObject(String xmlString) throws ParserConfigurationException, SAXException, IOException, UnmarshallingException {
        BasicParserPool ppMgr = new BasicParserPool();
        ppMgr.setNamespaceAware(true);

        Element samlRootElement = XmlUtils.convertToElement(xmlString);

        return getSamlObject(samlRootElement);
    }

    @SuppressWarnings("unchecked")
    private <T extends XMLObject> T getSamlObject(Element samlRootElement) throws UnmarshallingException {
        // Get appropriate unmarshaller
        UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlRootElement);

        // Unmarshall using the document root element
        return (T) unmarshaller.unmarshall(samlRootElement);
    }
}
